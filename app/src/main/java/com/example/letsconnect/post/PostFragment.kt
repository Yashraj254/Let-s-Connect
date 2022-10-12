package com.example.letsconnect.post

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.letsconnect.*
import com.example.letsconnect.adapters.AllCommentsFirestoreAdapter
import com.example.letsconnect.databinding.FragmentPostBinding
import com.example.letsconnect.models.Comment
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay

@AndroidEntryPoint
class PostFragment : Fragment() {


    private var _binding: FragmentPostBinding? = null
    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseFirestore.getInstance()
    private val userRef = database.collection(KEY_ALL_POSTS)
    private lateinit var adapter: AllCommentsFirestoreAdapter
    private lateinit var postId: String
    private lateinit var navBar: BottomNavigationView
    private val viewModel: PostViewModel by activityViewModels()
    private lateinit var options: FirestoreRecyclerOptions<Comment>

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentPostBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        postId = arguments?.getString("selected_postId").toString()
        setRecyclerView()
        showCurrentPost(postId)
        binding.btnComment.setOnClickListener {
            val message = binding.etComment.text.toString()
            if (message.isNotEmpty())
                postComment(postId, message)
        }

        navBar = requireActivity().findViewById(R.id.nav_view)
        navBar.visibility = View.GONE

        binding.lifecycleOwner = this
    }

    private fun showCurrentPost(postId: String) {

        viewModel.getCurrentPost(postId)
        viewModel.post.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Error -> {}
                is Resource.Loading -> {}
                is Resource.Success -> {
                    if (it.data!!.exists()) {
                        binding.apply {
                            tvUsername.text = it.data.getString(KEY_USER_NAME)
                            tvEmail.text = it.data.getString(KEY_EMAIL)
                            tvPostMessage.text = it.data.getString(KEY_POST_MESSAGE)
                            tvUploadTime.text = Utils.getTimeAgo(it.data.getLong(KEY_UPLOAD_TIME)!!)
                            tvTotalLikes.text = it.data.getLong(KEY_TOTAL_LIKES).toString()

//                            tvTotalComments.text = it.data.getLong(KEY_TOTAL_COMMENTS).toString()
                            val image = it.data.getString(KEY_PROFILE_IMAGE)
                            if (image != null)
                                Glide.with(requireContext()).load(image).into(imageView)
                        }
                    }
                }
            }
        }

    }

    private fun postComment(
        postId: String,
        message: String,
    ) {
        viewModel.addNewComment(postId, message).invokeOnCompletion {
            binding.etComment.text = null
            scrollToLast()
        }

    }

    private fun setRecyclerView() {
        viewModel.getAllComments(postId)
        viewModel.allComments.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Error -> {}
                is Resource.Loading -> {

                }
                is Resource.Success -> {

                    options =
                        FirestoreRecyclerOptions.Builder<Comment>()
                            .setQuery(it.data!!.query, Comment::class.java).build()
                    binding.rvAllComments.layoutManager = LinearLayoutManager(context)
                    val navHostFragment =
                        requireActivity().supportFragmentManager.findFragmentById(R.id.container_fragment) as NavHostFragment
                    adapter =
                        AllCommentsFirestoreAdapter(options, navHostFragment)
                    adapter.startListening()
                    binding.rvAllComments.adapter = adapter
                    binding.adapter = adapter
//                    binding.tvTotalComments.text = viewModel.allComments.value!!.data!!.documents.size.toString()
                    scrollToLast()
                }
            }


        }
    }

    private fun scrollToLast() {
        if (adapter.itemCount != 0)
            binding.rvAllComments.smoothScrollToPosition(adapter.itemCount)
    }

    override fun onStop() {
        super.onStop()
        if (this::adapter.isInitialized)
            adapter.stopListening()
        navBar.visibility = View.VISIBLE

    }
}