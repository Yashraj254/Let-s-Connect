package com.example.letsconnect.post

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.letsconnect.*
import com.example.letsconnect.adapters.AllCommentsFirestoreAdapter
import com.example.letsconnect.databinding.FragmentPostBinding
import com.example.letsconnect.models.Comment
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.firebase.ui.firestore.ObservableSnapshotArray
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class PostFragment : Fragment(), AllCommentsFirestoreAdapter.OnCommentItemClicked {

    private var _binding: FragmentPostBinding? = null
    private lateinit var adapter: AllCommentsFirestoreAdapter
    private lateinit var postId: String
    private lateinit var navBar: BottomNavigationView
    private val viewModel: PostViewModel by activityViewModels()
    private lateinit var options: FirestoreRecyclerOptions<Comment>
    private lateinit var arr: ObservableSnapshotArray<Comment>
    @Inject
    lateinit var currentUser: String
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentPostBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().title = "Comments"

        postId = arguments?.getString("selected_postId").toString()
        showCurrentPost(postId)
        setRecyclerView()
        binding.btnComment.setOnClickListener {
            val message = binding.etComment.text.toString()
            if (message.isNotEmpty())
                postComment(postId, message)
        }

        navBar = requireActivity().findViewById(R.id.nav_view)
        navBar.visibility = View.GONE

        binding.lifecycleOwner = this
        binding.retryButton.setOnClickListener {
            viewModel.getAllComments(postId)
            viewModel.getCurrentPost(postId)
        }

    }

    private fun showCurrentPost(postId: String) {
        viewModel.getCurrentPost(postId)
        lifecycleScope.launchWhenCreated {
            viewModel.post.collect {
                when (it) {
                    is Resource.Error -> {
                        showSnackBar(message = it.message!!)
                        binding.apply {
                            statusBox.isVisible = true
                            detailsLayout.isVisible = false
                            pbLoading.isVisible = false
                            etComment.isVisible = false
                            btnComment.isVisible = false
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
                            etComment.isVisible = true
                            btnComment.isVisible = true
                        }
                        if (it.data!!.exists()) {
                            val list = it.data.get("likedBy") as ArrayList<String>
                            binding.apply {
                                detailsLayout.isVisible = true
                                tvUsername.text = it.data.getString(KEY_USER_NAME)
                                tvEmail.text = it.data.getString(KEY_EMAIL)
                                tvPostMessage.text = it.data.getString(KEY_POST_MESSAGE)
                                tvUploadTime.text = Utils.getTimeAgo(it.data.getLong(KEY_UPLOAD_TIME)!!)
                                tvTotalLikes.text = it.data.getLong(KEY_TOTAL_LIKES).toString()
                                if(list.contains(currentUser)){
                                    ibtnLike.setImageResource(R.drawable.ic_liked_post)
                                }else
                                    ibtnLike.setImageResource(R.drawable.ic_like)
                                val image = it.data.getString(KEY_PROFILE_IMAGE)
                                if (image != null)
                                    Glide.with(requireContext()).load(image).into(imageView)
                            }
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
        viewModel.addNewComment(postId, message)
        binding.etComment.text = null
        scrollToLast()
    }

    private fun setRecyclerView() {
        viewModel.getAllComments(postId)
        lifecycleScope.launchWhenCreated {
            viewModel.allComments.collect {
            when (it) {
                is Resource.Error -> {
                    binding.apply {
                        statusBox.isVisible = true
                        rvAllComments.isVisible = false
                        pbLoading.isVisible = false
                    }
                }
                is Resource.Loading -> {
                    binding.apply {
                        statusBox.isVisible = false
                        rvAllComments.isVisible = false
                        pbLoading.isVisible = true
                    }
                }
                is Resource.Success -> {
                    binding.apply {
                        statusBox.isVisible = false
                        pbLoading.isVisible = false
                    }

                    if (!it.data!!.isEmpty) {
                        binding.rvAllComments.isVisible = true
                        options =
                            FirestoreRecyclerOptions.Builder<Comment>()
                                .setQuery(it.data.query, Comment::class.java).build()
                        binding.rvAllComments.layoutManager = LinearLayoutManager(context)
                        arr = options.snapshots

                        adapter =
                            AllCommentsFirestoreAdapter(options, this@PostFragment)
                        adapter.startListening()
                        binding.rvAllComments.adapter = adapter
                        binding.adapter = adapter
                    }
                }
            }
        }
        }
    }

    private fun scrollToLast() {
        if (adapter.itemCount != 0)
            binding.rvAllComments.smoothScrollToPosition(adapter.itemCount)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (this::adapter.isInitialized)
            adapter.stopListening()
        navBar.visibility = View.VISIBLE
    }

    private fun sendData(position:Int) {
        val bundle = Bundle()
        bundle.putString("selected_userId", arr[position].userId)
        Navigation.findNavController(requireView())
            .navigate(R.id.action_postFragment_to_navigation_profile, bundle)
    }

    override fun onUsernameClicked(position: Int) {
        sendData(position)
    }

    override fun onEmailClicked(position: Int) {
        sendData(position)
    }


}