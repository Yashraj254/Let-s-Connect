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
import android.widget.ImageButton
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.letsconnect.*
import com.example.letsconnect.adapters.AllPostsFirestoreAdapter
import com.example.letsconnect.databinding.FragmentProfileBinding
import com.example.letsconnect.models.Post
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.firebase.ui.firestore.ObservableSnapshotArray
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint
import java.io.ByteArrayOutputStream
import java.io.FileNotFoundException
import javax.inject.Inject

@AndroidEntryPoint
class ProfileFragment : Fragment(), AllPostsFirestoreAdapter.OnPostItemClicked {

    private var _binding: FragmentProfileBinding? = null
    private lateinit var encodedImage: ByteArray
    private lateinit var imageUri: Uri
    private lateinit var bitmap: Bitmap
    private lateinit var navBar: BottomNavigationView
    private val viewModel: ProfileViewModel by activityViewModels()
    private lateinit var adapter: AllPostsFirestoreAdapter
    private lateinit var arr: ObservableSnapshotArray<Post>

    @Inject
    lateinit var currentUser: String

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
        requireActivity().title = "Profile"

        if (selectedUser != null) {
            checkIfFollowing(selectedUser)
            showCurrentUser(selectedUser)
            navBar.visibility = View.GONE

            if (selectedUser.toString() != currentUser)
                binding.btnFollow.visibility = View.VISIBLE
        } else {
            checkIfFollowing(currentUser)
            showCurrentUser(currentUser)
            binding.ivProfileImage.setOnClickListener {
                val intent =
                    Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                pickImage.launch(intent)
            }
        }
        binding.btnFollow.setOnClickListener { followUser(selectedUser.toString()) }
        binding.apply {
            val bundle = Bundle()
            bundle.putString("userId", selectedUser)
            tvFollowers.setOnClickListener {
                bundle.putString("fragment", "Followers")
                Navigation.findNavController(view)
                    .navigate(R.id.action_navigation_profile_to_navigation_search_users, bundle)
            }
            tvFollowing.setOnClickListener {
                bundle.putString("fragment", "Following")
                Navigation.findNavController(view)
                    .navigate(R.id.action_navigation_profile_to_navigation_search_users, bundle)
            }
            retryButton.setOnClickListener {
                viewModel.getCurrentUserDetails(selectedUser.toString())
                viewModel.getCurrentUserPosts(selectedUser.toString())
            }
        }
    }

    private fun checkIfFollowing(userId: String) {
        viewModel.checkIfFollowing(userId)
        lifecycleScope.launchWhenCreated {
            viewModel.following.collect() {
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
    }

    private fun followUser(userId: String) {
        lifecycleScope.launchWhenCreated {
            viewModel.currentUserDetails.collect{
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
                            if (profileImage != null)
                                user[KEY_PROFILE_IMAGE] = profileImage.toString()
                            viewModel.followUser(userId, user)
                            showCurrentUser(userId)

                            binding.apply {
                                btnFollow.visibility = View.GONE
                                tvStatus.visibility = View.VISIBLE
                            }

                        }
                    }
                }

            }
        }
    }


    private fun showCurrentUser(userId: String) {
        viewModel.getCurrentUserDetails(userId)
        lifecycleScope.launchWhenCreated {
            viewModel.currentUserDetails.collect {
                when (it) {
                    is Resource.Error -> {
                        showSnackBar(message = it.message!!)
                        binding.apply {
                            statusBox.isVisible = true
                            detailsLayout.isVisible = false
                            pbLoading.isVisible = false
                        }
                    }
                    is Resource.Loading -> {
                        binding.apply {
                            statusBox.isVisible = false
                            detailsLayout.isVisible = false
                            pbLoading.isVisible = true
                        }
                    }
                    is Resource.Success -> {
                        binding.apply {
                            statusBox.isVisible = false
                            pbLoading.isVisible = false
                        }
                        if (it.data!!.exists()) {

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
        }

        viewModel.getCurrentUserPosts(userId)
        lifecycleScope.launchWhenCreated {
            viewModel.currentUserPosts.collect {
                when (it) {
                    is Resource.Error -> {
                        showSnackBar(message = it.message!!)
                        binding.apply {
                            statusBox.isVisible = true
                            rvMyPosts.isVisible = false
                            pbLoading.isVisible = false
                        }
                    }
                    is Resource.Loading -> {
                        binding.apply {
                            statusBox.isVisible = false
                            rvMyPosts.isVisible = false
                            pbLoading.isVisible = true
                        }
                    }
                    is Resource.Success -> {
                        binding.apply {
                            statusBox.isVisible = false
                            pbLoading.isVisible = false
                            binding.detailsLayout.isVisible = true
                        }
                        if (!it.data!!.isEmpty) {
                            binding.lottieNoPost.isVisible = false
                            binding.tvNoPost.isVisible = false

                            binding.rvMyPosts.isVisible = true
                            val options = FirestoreRecyclerOptions.Builder<Post>()
                                .setQuery(it.data.query, Post::class.java).build()
                            binding.rvMyPosts.layoutManager = LinearLayoutManager(context)
                            arr = options.snapshots
                            adapter = AllPostsFirestoreAdapter(options, this@ProfileFragment)
                            binding.rvMyPosts.adapter = adapter
                            adapter.startListening()
                        } else {
                            binding.lottieNoPost.isVisible = true
                            binding.tvNoPost.isVisible = true
                        }
                    }
                }
            }
        }
    }


    override fun onStop() {
        super.onStop()
        binding.lottieNoPost.isVisible = false
        binding.tvNoPost.isVisible = false
    }

    override fun onDestroyView() {
        super.onDestroyView()

        if (this::adapter.isInitialized) adapter.stopListening()
        navBar.visibility = View.VISIBLE
        _binding = null
    }

    private fun uploadImage() {
        val pd = ProgressDialog(context)
        pd.setMessage("Uploading File...")
        pd.setCancelable(false)
        pd.show()
        viewModel.uploadProfilePic(encodedImage)
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
                                uploadImage()
                            } catch (e: FileNotFoundException) {
                                e.printStackTrace()
                            }
                        }
                    }
                }
            })

    override fun onLikeClicked(position: Int, imageButton: ImageButton) {
        val list = arr[position].likedBy
        viewModel.likePost(arr[position])
        if (list.contains(currentUser)) {
            imageButton.setImageResource(R.drawable.ic_like)
        } else {
            imageButton.setImageResource(R.drawable.ic_liked_post)
        }
    }

    override fun onCommentClicked(position: Int) {
        val bundle = Bundle()
        bundle.putString("selected_postId", arr[position].postId)
        Navigation.findNavController(requireView())
            .navigate(R.id.action_navigation_profile_to_postFragment,
                bundle)
    }

    override fun onUsernameClicked(position: Int) {
    }

    override fun onEmailClicked(position: Int) {
    }

}