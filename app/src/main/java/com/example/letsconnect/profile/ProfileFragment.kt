package com.example.letsconnect.profile

import android.app.Activity
import android.app.ProgressDialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.*
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
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
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import java.io.ByteArrayOutputStream
import java.io.FileNotFoundException
import javax.inject.Inject

@AndroidEntryPoint
class ProfileFragment : Fragment(), MenuProvider, AllPostsFirestoreAdapter.OnPostItemClicked {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var encodedImage: ByteArray
    private lateinit var imageUri: Uri
    private lateinit var bitmap: Bitmap
    private var menu: Menu? = null
    private lateinit var navBar: BottomNavigationView
    private val viewModel: ProfileViewModel by activityViewModels()
    private lateinit var adapter: AllPostsFirestoreAdapter
    private lateinit var arr: ObservableSnapshotArray<Post>
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var menuBtnEdit: MenuItem
    private lateinit var menuBtnSave: MenuItem
    private var selectedUser: String? = null

    @Inject
    lateinit var auth: FirebaseAuth
    private lateinit var currentUser: String


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentProfileBinding.inflate(layoutInflater, container, false)

        //   val user = auth.currentUser.uid
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)
        currentUser = auth.currentUser!!.uid

        selectedUser = arguments?.getString("selected_userId")
        navBar = requireActivity().findViewById(R.id.nav_view)
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(requireContext(), gso)
        val actionBar = requireActivity().findViewById<MaterialToolbar>(R.id.materialToolbar);
        actionBar.title = "Profile"
        actionBar.isVisible = true
        navBar.visibility = View.GONE

        if (selectedUser != null) {

            checkIfFollowing(selectedUser!!)

            if (selectedUser.toString() != currentUser)
                binding.btnFollow.visibility = View.VISIBLE
        } else {

            selectedUser = currentUser
            showCurrentUser(selectedUser!!)
            binding.ivProfileImage.setOnClickListener {
                val intent =
                    Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                pickImage.launch(intent)
            }
        }
        binding.btnFollow.setOnClickListener {
            followUser(selectedUser.toString())
        }
        binding.btnUnfollow.setOnClickListener {
            unfollowUser(selectedUser.toString())
        }
        binding.apply {
            val bundle = Bundle()

            tvFollowers.setOnClickListener {
                bundle.putString("fragment", "Followers")
                bundle.putString("userId", selectedUser)
                Navigation.findNavController(view)
                    .navigate(R.id.action_navigation_profile_to_followerFollowingFragment, bundle)
            }
            tvFollowing.setOnClickListener {
                bundle.putString("fragment", "Following")
                bundle.putString("userId", selectedUser)
                Navigation.findNavController(view)
                    .navigate(R.id.action_navigation_profile_to_followerFollowingFragment, bundle)
            }
            retryButton.setOnClickListener {
                viewModel.getCurrentUserDetails(selectedUser.toString())
                viewModel.getCurrentUserPosts(selectedUser.toString())
            }
        }
    }

    private fun isValid(): Boolean {

        if (binding.etName.text.toString().trim().isEmpty()) {
            showToast("Enter name")
            return false
        } else if (binding.etUsername.text.toString().trim().isEmpty()) {
            showToast("Enter username")
            return false
        } else {
            return true
        }
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        this.menu = menu
        menuInflater.inflate(R.menu.login_menu, menu)
        menuBtnEdit = menu.findItem(R.id.menu_btn_edit)
        menuBtnSave = menu.findItem(R.id.menu_btn_save)
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {

//        val menuBtnLogout = menu?.getItem(R.id.menu_btn_logout)
        when (menuItem.itemId) {
            R.id.menu_btn_edit -> {
                binding.apply {
                    ivAddImage.isVisible = true
                    etUsername.setText(tvUsername.text.toString())
                    etName.setText(tvName.text.toString())
                    etLayoutName.isVisible = true
                    etLayoutUsername.isVisible = true
                    tvName.isVisible = false
                    tvUsername.isVisible = false
                    menuBtnEdit.isVisible = false
                    menuBtnSave.isVisible = true
                }
            }
            R.id.menu_btn_save -> {
                binding.apply {
                    if (isValid()) {
                        val name = etName.text.toString()
                        val username = etUsername.text.toString()
                        viewModel.checkUsername(username).observe(viewLifecycleOwner) {
                            when (it) {
                                is Response.Loading -> {
                                    binding.pbLoading.isVisible = true
                                }
                                is Response.Failure -> {
                                    binding.pbLoading.isVisible = false

                                    showSnackBar("Error: " + it.errorMessage)
                                }
                                is Response.Success -> {
                                    binding.pbLoading.isVisible = false
                                    ivAddImage.isVisible = false
                                    if (it.data) {
                                        showSnackBar("Username already taken")
                                    } else {
                                        navBar.visibility = View.VISIBLE
                                        viewModel.updateProfile(name, username)
                                            .observe(viewLifecycleOwner) { profile ->
                                                when (profile) {
                                                    is Response.Failure -> {}
                                                    is Response.Loading -> {}
                                                    is Response.Success -> {
                                                        tvName.text = etName.text.toString()
                                                        tvUsername.text = etUsername.text.toString()
                                                    }
                                                }
                                            }
                                        menuBtnEdit.isVisible = true
                                        menuBtnSave.isVisible = false
                                        etLayoutName.isVisible = false
                                        etLayoutUsername.isVisible = false
                                        tvName.isVisible = true
                                        tvUsername.isVisible = true
                                    }
                                }
                            }
                        }

                    }
                }

            }
            R.id.menu_btn_logout -> {
                val alertDialog = AlertDialog.Builder(requireContext())
                alertDialog.setTitle("Logout")
                alertDialog.setMessage("Are you sure?")
                alertDialog.setIcon(R.drawable.ic_logout)
                alertDialog.setPositiveButton("Logout", DialogInterface.OnClickListener { _, _ ->
                    navBar.isVisible = false
                    viewModel.userSignOut()
                    auth.signOut()
                    googleSignInClient.signOut()
                    Navigation.findNavController(requireView())
                        .navigate(R.id.action_navigation_profile_to_signInFragment)
                }).setNegativeButton("Cancel", DialogInterface.OnClickListener { _, _ ->
                })
                alertDialog.setCancelable(false).create().show()
            }
        }


        return false
    }

    private fun checkIfFollowing(userId: String) {

        viewModel.checkIfFollowing(userId).observe(viewLifecycleOwner) {
            when (it) {
                is Response.Failure -> {
                    showSnackBar(it.errorMessage)
                    binding.apply {
                        statusBox.isVisible = true
                        detailsLayout.isVisible = false
                        pbLoading.isVisible = false
                        rvMyPosts.isVisible = false
                    }
                    menuBtnEdit.isVisible = false
                }
                is Response.Loading -> {
                    binding.apply {
                        statusBox.isVisible = false
                        detailsLayout.isVisible = false
                        pbLoading.isVisible = true
                        rvMyPosts.isVisible = false
                    }
                }
                is Response.Success -> {
                    if (it.data) {
                        binding.btnUnfollow.isVisible = true
                    } else
                        binding.btnFollow.isVisible = true

                    showCurrentUser(userId)
                }
            }
        }

    }

    private fun unfollowUser(userId: String) {
        viewModel.unfollowUser(userId).observe(viewLifecycleOwner) {
            when (it) {
                is Response.Failure -> {}
                is Response.Loading -> {}
                is Response.Success -> {
                    binding.apply {
                        btnFollow.isVisible = true
                        btnUnfollow.isVisible = false
                    }
                }
            }
        }
    }

    private fun followUser(userId: String) {
        viewModel.followUser(userId).observe(viewLifecycleOwner) {
            when (it) {
                is Response.Failure -> {}
                is Response.Loading -> {}
                is Response.Success -> {
                    binding.apply {
                        btnFollow.isVisible = false
                        btnUnfollow.isVisible = true
                    }
                }
            }
        }
    }

    private fun showCurrentUser(userId: String) {
        viewModel.getCurrentUserDetails(userId)
        viewLifecycleOwner.lifecycleScope.launchWhenCreated {
            viewModel.currentUserDetails.collect { it ->
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
                        if (auth.currentUser!!.uid == userId)
                            menuBtnEdit.isVisible = true
                        if (it.data!!.exists()) {
                            val followers = it.data.get(KEY_FOLLOWERS) as ArrayList<String>
                            val totalFollowers = followers.size
                            val following = it.data.get(KEY_FOLLOWING) as ArrayList<String>
                            val totalFollowing = following.size

                            val image = it.data.getString(KEY_PROFILE_IMAGE)
                            val username = it.data.getString(KEY_USER_NAME)
                            if (username == "No username") {
                                navBar.visibility = View.GONE
                                binding.apply {
                                    tvName.isVisible = false
                                    tvUsername.isVisible = false
                                    etLayoutName.isVisible = true
                                    etLayoutUsername.isVisible = true
                                    menuBtnEdit.isVisible = false
                                    menuBtnSave.isVisible = true
                                }
                            } else {
                                navBar.visibility = View.VISIBLE

                            }
                            binding.apply {
                                tvName.text = it.data.getString("name")
                                if (auth.currentUser!!.uid == selectedUser)
                                    tvEmail.text = it.data.getString(KEY_EMAIL)
                                else {
                                    tvEmail.visibility = View.GONE
                                    navBar.visibility = View.GONE
                                }
                                etName.setText(tvName.text)

                                tvUsername.text = username
                                tvFollowers.text = "Followers: $totalFollowers"
                                tvFollowing.text = "Following: $totalFollowing"
                                if (image != null) {
                                    Glide.with(requireContext()).load(image).circleCrop()
                                        .into(ivProfileImage)
                                }

                                viewModel.getCurrentUserPosts(userId)
                                viewLifecycleOwner.lifecycleScope.launchWhenCreated {
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
                                                    detailsLayout.isVisible = true
                                                }
                                                if (!it.data!!.isEmpty) {
                                                    binding.lottieNoPost.isVisible = false
                                                    binding.tvNoPost.isVisible = false

                                                    binding.rvMyPosts.isVisible = true
                                                    val options =
                                                        FirestoreRecyclerOptions.Builder<Post>()
                                                            .setQuery(it.data.query,
                                                                Post::class.java).build()
                                                    binding.rvMyPosts.layoutManager =
                                                        LinearLayoutManager(context)
                                                    arr = options.snapshots
                                                    adapter = AllPostsFirestoreAdapter(options,
                                                        this@ProfileFragment)
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
                        }
                    }
                }
            }
        }

    }

    private fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }


    override fun onDestroyView() {
        super.onDestroyView()

        if (this::adapter.isInitialized) adapter.stopListening()
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
        val previewBitmap =
            Bitmap.createScaledBitmap(bitmap, previewWidth, previewHeight, false)
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

    override fun onLongClick(position: Int): Boolean {
        val alertDialog = AlertDialog.Builder(requireContext())
        alertDialog.setTitle("Delete")
        alertDialog.setMessage("Are you sure?")
        alertDialog.setIcon(R.drawable.ic_delete)
        alertDialog.setPositiveButton("Delete", DialogInterface.OnClickListener { _, _ ->
            viewModel.deletePost(arr[position].postId)
        }).setNegativeButton("Cancel", DialogInterface.OnClickListener { _, _ ->
        })
        alertDialog.setCancelable(false).create().show()
        return false
    }
}