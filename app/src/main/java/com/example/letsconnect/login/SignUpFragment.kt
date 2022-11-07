package com.example.letsconnect.login

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.Navigation
import com.example.letsconnect.*
import com.example.letsconnect.databinding.FragmentSignUpBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import dagger.hilt.android.AndroidEntryPoint
import java.io.ByteArrayOutputStream
import java.io.FileNotFoundException
import javax.inject.Inject
import kotlin.collections.HashMap
import kotlin.collections.Map
import kotlin.collections.mutableMapOf
import kotlin.collections.set


@AndroidEntryPoint
class SignUpFragment : Fragment() {
    private val TAG = "SignUpFragment"
    private lateinit var binding: FragmentSignUpBinding
    private lateinit var encodedImage: ByteArray
    @Inject
    lateinit var auth:FirebaseAuth
    // private lateinit var preferenceManager:
    private lateinit var navBar: BottomNavigationView
    private val viewModel: LoginViewModel by activityViewModels()


    private val database = FirebaseFirestore.getInstance()
    private lateinit var imageUri: Uri
    private val userRef = database.collection("users")
    private lateinit var userId: String
    private lateinit var bitmap: Bitmap
    private lateinit var storageReference: StorageReference
    private lateinit var googleSignInClient: GoogleSignInClient
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentSignUpBinding.inflate(layoutInflater)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth.signOut()
        navBar = requireActivity().findViewById(R.id.nav_view)
        navBar.isVisible = false
        val actionBar = requireActivity().findViewById<MaterialToolbar>(R.id.materialToolbar)
        actionBar.title = "Sign Up"
        setListeners()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id)).requestEmail().build()

        googleSignInClient = GoogleSignIn.getClient(requireContext(), gso)
        setGoogleSignUpButtonText(binding.googleSignUp,"Sign up")
        binding.googleSignUp.setOnClickListener {
            val signInIntent = googleSignInClient.signInIntent
            launcher.launch(signInIntent)
        }
    }
    private fun setGoogleSignUpButtonText(signInButton: SignInButton, buttonText: String?) {
        // Find the TextView that is inside of the SignInButton and set its text
        for (i in 0 until signInButton.childCount) {
            val v = signInButton.getChildAt(i)
            if (v is TextView) {
                v.text = buttonText
                return
            }
        }
    }
    private fun setListeners() {
        binding.textSignIn.setOnClickListener {

            Navigation.findNavController(requireView())
                .navigate(R.id.action_signUpFragment_to_signInFragment)
            //   startActivity(Intent(requireActivity(), MainActivity::class.java))
        }

        binding.buttonSignUp.setOnClickListener {
            if (isValidSignUpDetails()) {
                signUp()
            }
        }
        binding.layouImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            pickImage.launch(intent)
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    private val launcher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                try {
                    val googleSignInAccount = task.getResult(ApiException::class.java)
                    googleSignInAccount?.apply {
                        idToken?.let { idToken ->
                            signUpWithGoogle(idToken)
                        }
                    }
                } catch (e: ApiException) {
                    showSnackBar(e.message.toString())
                }
            }
        }

    private fun signUpWithGoogle(idToken: String) {
        viewModel.signInWithGoogle(idToken).observe(this) { response ->
            when (response) {
                is Response.Loading -> {
                    showSnackBar("Loading")
                }
                is Response.Success<*> -> {
                    val isNewUser = response.data
                    if (isNewUser as Boolean) {
                        viewModel.deleteAccount(idToken)
                        viewModel.createUser().observe(this){
                            when(it){
                                is Response.Loading -> { }
                                is Response.Success -> {
                                    Navigation.findNavController(requireView())
                                        .navigate(R.id.action_signUpFragment_to_navigation_profile)
                                }
                                is Response.Failure -> { }
                            }
                        }
                    } else {
                     showSnackBar("Account already Exists")

                    }
                }
                is Response.Failure -> {
                    showSnackBar(response.errorMessage)

                }
            }
        }
    }

    private fun signUp() {
        loading(true)
        val database = FirebaseFirestore.getInstance()
        val user = HashMap<String, Any>()
        val followers = ArrayList<String>()
        val following = ArrayList<String>()
        user[KEY_USER_NAME] = binding.inputName.text.toString()
        user[KEY_EMAIL] = binding.inputEmail.text.toString()
        user[KEY_FOLLOWERS] = followers
        user[KEY_FOLLOWING] = following


        auth.createUserWithEmailAndPassword(binding.inputEmail.text.toString(),
            binding.inputPassword.text.toString()).addOnSuccessListener {
                user[KEY_USER_ID] = auth.currentUser!!.uid
                userId = auth.currentUser!!.uid
                storageReference =
                    FirebaseStorage.getInstance().reference.child("$userId/profilePhoto")
                if (this::encodedImage.isInitialized) uploadImage()
                database.collection(KEY_COLLECTION_USERS).document(user[KEY_USER_ID] as String)
                    .set(user).addOnSuccessListener {
                        loading(false)
                    }.addOnFailureListener {
                        loading(false)
                        it.message?.let { it1 -> showToast(it1) }
                    }
                showToast("Account Created")
                startActivity(Intent(requireActivity(), MainActivity::class.java))
            }.addOnFailureListener {
                showSnackBar("SignUp Failed")
                loading(false)

            }

    }

    private fun uploadImage() {
        val pd = ProgressDialog(context)
        pd.setMessage("Uploading File...")
        pd.setCancelable(false)
        pd.show()
        storageReference.putBytes(encodedImage).addOnSuccessListener {
            pd.dismiss()
            storageReference.downloadUrl.addOnSuccessListener {
                val obj = mutableMapOf<String, String>()
                obj[KEY_PROFILE_IMAGE] = it.toString()
                userRef.document(userId).update(obj as Map<String, Any>).addOnSuccessListener {
                    Toast.makeText(context, "Profile Picture Uploaded", Toast.LENGTH_SHORT).show()
                }
            }
        }.addOnFailureListener {
            pd.dismiss()
            Toast.makeText(context, "Failed to upload image", Toast.LENGTH_SHORT).show()
        }
    }


    private fun encodeImage(bitmap: Bitmap): ByteArray {
        val previewWidth = 150;
        val previewHeight = bitmap.height * previewWidth / bitmap.width
        val previewBitmap = Bitmap.createScaledBitmap(bitmap, previewWidth, previewHeight, false)
        val baos = ByteArrayOutputStream()
        previewBitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos)
        return baos.toByteArray()
    }


    private val pickImage =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult(),
            ActivityResultCallback { result ->
                run {
                    if (result.resultCode == Activity.RESULT_OK) {
                        if (result.data != null) {
                            imageUri = result.data!!.data!!
                            try {
                                val inputStream = imageUri.let {
                                    context?.contentResolver?.openInputStream(it)
                                }
                                bitmap = BitmapFactory.decodeStream(inputStream)
                                binding.imageProfile.setImageBitmap(bitmap)
                                encodedImage = encodeImage(bitmap)
                            } catch (e: FileNotFoundException) {
                                e.printStackTrace()
                            }
                        }
                    }
                }
            })

    private fun isValidSignUpDetails(): Boolean {
        if (binding.inputName.text.toString().trim().isEmpty()) {
            showToast("Enter name")
            return false
        } else if (binding.inputEmail.text.toString().trim().isEmpty()) {
            showToast("Enter email")
            return false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(binding.inputEmail.text.toString()).matches()) {
            showToast("Enter valid mail")
            return false
        } else if (binding.inputPassword.text.toString().trim().isEmpty()) {
            showToast("Enter password")
            return false
        } else {
            return true
        }
    }

    private fun loading(isLoading: Boolean) {
        if (isLoading) {
            binding.buttonSignUp.visibility = View.GONE
            binding.progressBar.visibility = View.VISIBLE
        } else {
            binding.progressBar.visibility = View.GONE
            binding.buttonSignUp.visibility = View.VISIBLE
        }
    }

}