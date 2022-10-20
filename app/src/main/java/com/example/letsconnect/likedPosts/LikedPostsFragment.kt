package com.example.letsconnect.likedPosts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.letsconnect.R
import com.example.letsconnect.Resource
import com.example.letsconnect.adapters.AllPostsFirestoreAdapter
import com.example.letsconnect.databinding.FragmentLikedPostsBinding
import com.example.letsconnect.models.Post
import com.example.letsconnect.showSnackBar
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.firebase.ui.firestore.ObservableSnapshotArray
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class LikedPostsFragment : Fragment(),AllPostsFirestoreAdapter.OnPostItemClicked {

    private var _binding: FragmentLikedPostsBinding? = null
    private val viewModel: LikedPostsViewModel by activityViewModels()
    private lateinit var adapter: AllPostsFirestoreAdapter
    private lateinit var arr: ObservableSnapshotArray<Post>
   @Inject
   lateinit var currentUser:String

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentLikedPostsBinding.inflate(inflater, container, false)
        requireActivity().title = "Liked Posts"
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setRecyclerView()
        binding.retryButton.setOnClickListener { viewModel.getLikedPosts() }
    }
    private fun setRecyclerView() {
        viewModel.getLikedPosts()
        lifecycleScope.launchWhenCreated {
            viewModel.likedPosts.collect{
                when (it) {
                    is Resource.Error -> {
                        showSnackBar(message = it.message!!)
                        binding.apply {
                            statusBox.isVisible = true
                            rvLikedPosts.isVisible = false
                            pbLoading.isVisible = false
                        }
                    }
                    is Resource.Loading -> {
                        binding.apply  {
                            statusBox.isVisible = false
                            rvLikedPosts.isVisible = false
                            pbLoading.isVisible = true
                        }
                    }
                    is Resource.Success -> {
                        binding.apply  {
                            statusBox.isVisible = false
                            pbLoading.isVisible = false
                        }
                        if (!it.data!!.isEmpty) {
                            binding.lottieNoLikedPost.isVisible = false
                            binding.tvNoPost.isVisible = false

                            binding.rvLikedPosts.isVisible = true

                            val options = FirestoreRecyclerOptions.Builder<Post>()
                                .setQuery(it.data.query, Post::class.java)
                                .build()
                            arr = options.snapshots

                            binding.rvLikedPosts.layoutManager = LinearLayoutManager(context)
                            adapter = AllPostsFirestoreAdapter(options,this@LikedPostsFragment)
                            binding.rvLikedPosts.adapter = adapter
                            adapter.startListening()
                        }else{
                            binding.lottieNoLikedPost.isVisible = true
                            binding.tvNoPost.isVisible = true
                        }

                    }
                }

            }
        }
    }

    override fun onStop() {
        super.onStop()
        binding.lottieNoLikedPost.isVisible = false
        binding.tvNoPost.isVisible = false
    }


    override fun onDestroyView() {
        super.onDestroyView()
        if(this::adapter.isInitialized)
            adapter.stopListening()
        _binding = null
    }

    override fun onLikeClicked(position: Int, imageButton: ImageButton) {
        viewModel.likePost(arr[position])

    }

    private fun sendData(post: Post) {
        val bundle = Bundle()
        bundle.putString("selected_userId", post.userId)
        Navigation.findNavController(requireView())
            .navigate(R.id.action_navigation_liked_posts_to_postFragment,
                bundle)

    }

    override fun onCommentClicked(position: Int) {
        val bundle = Bundle()
        bundle.putString("selected_postId", arr[position].postId)
        Navigation.findNavController(requireView())
            .navigate(R.id.action_navigation_home_to_postFragment,
                bundle)
    }

    override fun onUsernameClicked(position: Int) {
        sendData(arr[position])
    }

    override fun onEmailClicked(position: Int) {
        sendData(arr[position])
    }
}