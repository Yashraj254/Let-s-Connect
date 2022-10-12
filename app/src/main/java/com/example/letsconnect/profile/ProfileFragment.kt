package com.example.letsconnect.profile

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.Navigation
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.letsconnect.*
import com.example.letsconnect.adapters.MyPostsFirestoreAdapter
import com.example.letsconnect.databinding.FragmentProfileBinding
import com.example.letsconnect.models.Post
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.StorageReference
import dagger.hilt.android.AndroidEntryPoint
import java.io.ByteArrayOutputStream
import java.io.FileNotFoundException

@AndroidEntryPoint
class ProfileFragment : Fragment() {

    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseFirestore.getInstance()
    private val userRef = database.collection("users")
    private var _binding: FragmentProfileBinding? = null
    private lateinit var encodedImage: ByteArray
    private lateinit var imageUri: Uri
    private lateinit var bitmap: Bitmap
    private lateinit var storageReference: StorageReference
    private lateinit var navBar: BottomNavigationView
    private val viewModel: ProfileViewModel by activityViewModels()
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
        navBar = requireActivity().findViewById(R.id.nav_view)
        requireActivity().title = "Profile "

        if (selectedUser != null) {
            checkIfFollowing(selectedUser)
            showCurrentUser(selectedUser)
            navBar.visibility = View.GONE

            // binding.ivProfileImage.setOnClickListener { uploadImage(selectedUser) }
            if (selectedUser.toString() != auth.currentUser!!.uid)
                binding.btnFollow.visibility = View.VISIBLE
        } else {
            checkIfFollowing(auth.currentUser!!.uid)
            showCurrentUser(auth.currentUser!!.uid)
            binding.ivProfileImage.setOnClickListener {
                val intent =
                    Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                pickImage.launch(intent)
            }
        }
        binding.apply {
            val bundle = Bundle()
            bundle.putString("userId",selectedUser)
            btnFollow.setOnClickListener { followUser(selectedUser.toString()) }
            tvFollowers.setOnClickListener {
                bundle.putString("fragment","Followers")
                Navigation.findNavController(view).navigate(R.id.action_navigation_profile_to_navigation_search_users,bundle)
            }
            tvFollowing.setOnClickListener {
                bundle.putString("fragment","Following")
                Navigation.findNavController(view).navigate(R.id.action_navigation_profile_to_navigation_search_users,bundle)
            }
        }
    }

    private fun checkIfFollowing(userId: String) {
        viewModel.checkIfFollowing(userId)
        viewModel.following.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Error -> {
                    showSnackBar(message = it.message!!)
                }
                is Resource.Loading -> {}
                is Resource.Success -> {
                    if (!it.data!!.isEmpty) {
                        for (i in it.data.documents) {
                            if (i.getString("userId") == userId) {
                                binding.apply {
                                    tvStatus.visibility = View.VISIBLE
                                    btnFollow.visibility = View.GONE
                                }
                            }
                        }
                    }
                }
            }
        }

    }

    private fun followUser(userId: String) {


        viewModel.currentUserDetails.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Error -> {
                    showSnackBar(message = it.message!!)
                }
                is Resource.Loading -> {}
                is Resource.Success -> {
                    if (it.data!!.exists()) {

                        val name = it.data.getString(KEY_USER_NAME)
                        val email = it.data.getString(KEY_EMAIL)
                        val profileImage = it.data.getString(KEY_PROFILE_IMAGE)
                        val followers = it.data.getLong(KEY_FOLLOWERS)!!.toInt()
                        val following = it.data.getLong(KEY_FOLLOWING)!!.toInt()
                        val user = HashMap<String, Any>()
                        user[KEY_USER_ID] = userId
                        user[KEY_USER_NAME] = name.toString()
                        user[KEY_EMAIL] = email.toString()
                        user[KEY_FOLLOWERS] = followers
                        user[KEY_FOLLOWING] = following
                        user[KEY_PROFILE_IMAGE] = profileImage.toString()
                        viewModel.followUser(userId, user)
                        viewModel.getCurrentUserDetails(userId)

                        binding.apply {
                            btnFollow.visibility = View.GONE
                            tvStatus.visibility = View.VISIBLE
                        }

                    }
                }
            }

        }
    }


    private fun showCurrentUser(userId: String) {
        viewModel.getCurrentUserDetails(userId)
        viewModel.getCurrentUserPosts(userId)
        viewModel.currentUserDetails.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Error -> {
                    showSnackBar(message = it.message!!)
                }
                is Resource.Loading -> {}
                is Resource.Success -> {
                    if (it.data!!.exists()) {
//                        binding.emptyResultListView.isVisible = true
//                        binding.myResultRecyclerview.isVisible = false
                        val followers = it.data.getLong(KEY_FOLLOWERS)!!.toInt()
                        val following = it.data.getLong(KEY_FOLLOWING)!!.toInt()
                        val image = it.data.getString(KEY_PROFILE_IMAGE)
                        binding.apply {
                            tvName.text = it.data.getString(KEY_USER_NAME)
                            tvUsername.text = it.data.getString(KEY_EMAIL)
                            tvFollowers.text = "Followers: $followers"
                            tvFollowing.text = "Following: $following"
                            if (image != null) {
                                Glide.with(requireContext()).load(image).circleCrop()
                                    .into(ivProfileImage)
                            }
                        }
                    }
                }
            }
        }

        viewModel.currentUserPosts.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Error -> {
                    showSnackBar(message = it.message!!)
                }
                is Resource.Loading -> {}
                is Resource.Success -> {
                    if (!it.data!!.isEmpty) {

                        val options = FirestoreRecyclerOptions.Builder<Post>()
                            .setQuery(it.data.query, Post::class.java).build()
                        binding.rvMyPosts.layoutManager = LinearLayoutManager(context)
                        val navHostFragment =
                            requireActivity().supportFragmentManager.findFragmentById(R.id.container_fragment) as NavHostFragment
                        adapter = MyPostsFirestoreAdapter(options, navHostFragment)
                        binding.rvMyPosts.adapter = adapter
                        adapter.startListening()
                    }
                }
            }
        }
    }


    override fun onStop() {
        super.onStop()
        if (this::adapter.isInitialized) adapter.stopListening()
        navBar.visibility = View.VISIBLE
    }

    private fun uploadImage(userId: String) {
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
                                binding.ivProfileImage.setImageBitmap(bitmap)
                                encodedImage = encodeImage(bitmap)
                                uploadImage(auth.currentUser!!.uid)
                            } catch (e: FileNotFoundException) {
                                e.printStackTrace()
                            }
                        }
                    }
                }
            })

}