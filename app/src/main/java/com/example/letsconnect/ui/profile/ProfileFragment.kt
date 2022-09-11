package com.example.letsconnect.ui.profile

import android.app.Activity
import android.app.ProgressDialog
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.letsconnect.*
import com.example.letsconnect.adapters.AllPostsFirestoreAdapter
import com.example.letsconnect.adapters.MyPostsFirestoreAdapter
import com.example.letsconnect.databinding.FragmentProfileBinding
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.storage.StorageReference
import java.io.ByteArrayOutputStream
import java.io.FileNotFoundException
import kotlin.collections.HashMap


class ProfileFragment : Fragment() {

    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseFirestore.getInstance()
    private val userRef = database.collection("users")
    private var _binding: FragmentProfileBinding? = null
    private lateinit var encodedImage: ByteArray
    private lateinit var imageUri: Uri
    private lateinit var bitmap: Bitmap
    private lateinit var storageReference: StorageReference

    private lateinit var adapter: MyPostsFirestoreAdapter


    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentProfileBinding.inflate(layoutInflater, container, false)

        //   val user = auth.currentUser.uid
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val selectedUser = arguments?.getString("selected_userId")

        if (selectedUser != null) {
            checkIfFollowing(selectedUser)
            showCurrentUser(selectedUser)
            binding.ivProfileImage.setOnClickListener { uploadImage(selectedUser) }
            if(selectedUser.toString() != auth.currentUser!!.uid)
            binding.btnFollow.visibility = View.VISIBLE
        } else {
            checkIfFollowing(auth.currentUser!!.uid)
            showCurrentUser(auth.currentUser!!.uid)
            binding.ivProfileImage.setOnClickListener { uploadImage(auth.currentUser!!.uid) }
        }

        binding.btnFollow.setOnClickListener {
            following(selectedUser.toString())
        }

    }
    private fun checkIfFollowing(userId:String){
        database.collection(KEY_COLLECTION_USERS).document(userId).collection("following")
            .get().addOnSuccessListener(OnSuccessListener<QuerySnapshot>(){
                if(!it.isEmpty){
                    val list = it.documents
                    binding.tvFollowing.text = "Following: ${list.size}"
                    Log.d("Profile", "Total Following: ${list.size}")

                    for(i in list){

                        if( i.getString("userId") == userId){
                            binding.apply {
                                tvStatus.visibility = View.VISIBLE
                              btnFollow.visibility = View.GONE
                            }
                        }
                    }
                }
                else
                {
                    binding.tvFollowing.text = "Following: 0"
                }
            })

          database.collection(KEY_COLLECTION_USERS).document(userId).collection("followers")
            .get().addOnSuccessListener(OnSuccessListener<QuerySnapshot>(){
                if(!it.isEmpty){
                    val list = it.documents
                    binding.tvFollowers.text = "Followers: ${list.size}"
                }
                  else{
                    binding.tvFollowers.text = "Followers: 0"
                }
            })



    }

    private fun following(userId: String){
        database.collection(KEY_COLLECTION_USERS).document(userId).get().addOnSuccessListener {
            val name = it.getString(KEY_USER_NAME)
            val email = it.getString(KEY_EMAIL)
            val followers = it.getLong(KEY_FOLLOWERS)!!.toInt()
            val following = it.getLong(KEY_FOLLOWING)!!.toInt()
            val post = HashMap<String, Any>()
            post[KEY_USER_ID] = userId
            post[KEY_USER_NAME] = name.toString()
            post[KEY_EMAIL] = email.toString()
            post[KEY_FOLLOWERS] = followers
            post[KEY_FOLLOWING] = following
            database.collection(KEY_COLLECTION_USERS).document(auth.currentUser!!.uid).collection("following")
                .document(userId).set(post)

              database.collection(KEY_COLLECTION_USERS).document(userId).collection("followers")
                .document(auth.currentUser!!.uid).set(post)

            binding.apply {
                btnFollow.visibility = View.GONE
                tvStatus.visibility = View.VISIBLE
            }
        }
    }
   private fun showCurrentUser(userId: String) {
        database.collection(KEY_COLLECTION_USERS).document(userId).get().addOnSuccessListener {
            val name = it.getString(KEY_USER_NAME)
            val email = it.getString(KEY_EMAIL)
            val followers = it.getLong(KEY_FOLLOWERS)!!.toInt()
            val following = it.getLong(KEY_FOLLOWING)!!.toInt()
            // val image = it.getString(KEY_PROFILE_IMAGE)
            binding.apply {
                tvName.text = name
                tvUsername.text = email

//                if(image!=null)
//                Glide.with(requireContext()).load(image).into(ivProfileImage)
            }
        }
        val query = database.collection(KEY_COLLECTION_USERS).document(userId)
            .collection("myPosts")
        setRecyclerView(query)
    }

    private fun setRecyclerView(query: Query) {
        val options: FirestoreRecyclerOptions<Post> =
            FirestoreRecyclerOptions.Builder<Post>().setQuery(query, Post::class.java).build()
        binding.rvMyPosts.layoutManager = LinearLayoutManager(context)
        val navHostFragment =
            requireActivity().supportFragmentManager.findFragmentById(R.id.container_fragment) as NavHostFragment
        adapter = MyPostsFirestoreAdapter(options,
            navHostFragment)

        binding.rvMyPosts.adapter = adapter
    }

    override fun onStart() {
        super.onStart()
        adapter.startListening()

    }

    override fun onStop() {
        super.onStop()
        adapter.stopListening()
    }
    private fun uploadImage(userId:String) {
        val pd = ProgressDialog(context)
        pd.setMessage("Uploading File...")
        pd.setCancelable(false)
        pd.show()
        storageReference.putBytes(encodedImage).addOnSuccessListener {
            pd.dismiss()
            storageReference.downloadUrl.addOnSuccessListener {
                val obj = mutableMapOf<String, String>()
                obj[KEY_PROFILE_IMAGE] = it.toString()
                userRef.document(userId).update(obj as Map<String,Any>).addOnSuccessListener {
                    Toast.makeText(context,"Profile Picture Uploaded", Toast.LENGTH_SHORT).show()
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
                                binding.ivProfileImage.setImageBitmap(bitmap)
                                encodedImage = encodeImage(bitmap)
                            } catch (e: FileNotFoundException) {
                                e.printStackTrace()
                            }
                        }
                    }
                }
            })

}