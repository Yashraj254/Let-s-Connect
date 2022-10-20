package com.example.letsconnect.home

import android.os.Bundle
import android.view.*
import android.widget.ImageButton
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.letsconnect.R
import com.example.letsconnect.Resource
import com.example.letsconnect.adapters.AllPostsFirestoreAdapter
import com.example.letsconnect.databinding.FragmentHomeBinding
import com.example.letsconnect.models.Post
import com.example.letsconnect.showSnackBar
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.firebase.ui.firestore.ObservableSnapshotArray
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class HomeFragment : Fragment(), MenuProvider, AllPostsFirestoreAdapter.OnPostItemClicked {

    private var _binding: FragmentHomeBinding? = null
    private lateinit var adapter: AllPostsFirestoreAdapter
    private var menu: Menu? = null
    private lateinit var navBar: BottomNavigationView
    private val viewModel: HomeViewModel by activityViewModels()
    private lateinit var arr: ObservableSnapshotArray<Post>

    @Inject
    lateinit var currentUser: String

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {

        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().title = "Home"
        viewModel.getAllPosts()
        setRecyclerView()
        activity?.addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)
        binding.retryButton.setOnClickListener { viewModel.getAllPosts() }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        navBar = requireActivity().findViewById(R.id.nav_view)
        navBar.visibility = View.VISIBLE
    }

    private fun setRecyclerView() {
        viewModel.getAllPosts()
        lifecycleScope.launchWhenCreated {
            viewModel.allPosts.collect{
                when (it) {
                    is Resource.Error -> {
                        showSnackBar(message = it.message!!)
                        binding.apply {
                            statusBox.isVisible = true
                            rvAllPosts.isVisible = false
                            pbLoading.isVisible = false
                        }
                    }
                    is Resource.Loading -> {
                        binding.apply {
                            statusBox.isVisible = false
                            rvAllPosts.isVisible = false
                            pbLoading.isVisible = true
                        }
                    }
                    is Resource.Success -> {
                        binding.apply {
                            statusBox.isVisible = false
                            pbLoading.isVisible = false
                        }
                        if (!it.data!!.isEmpty) {
                            binding.rvAllPosts.isVisible = true
                            val options = FirestoreRecyclerOptions.Builder<Post>()
                                .setQuery(it.data.query, Post::class.java)
                                .build()
                            arr = options.snapshots
                            binding.rvAllPosts.layoutManager = LinearLayoutManager(context)
                            adapter =
                                AllPostsFirestoreAdapter(options,  this@HomeFragment)
                            binding.rvAllPosts.adapter = adapter
                            adapter.startListening()

                        }
                    }
                }

            }
        }


    }


    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        this.menu = menu
        menuInflater.inflate(R.menu.chat_menu, menu)
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            R.id.usersChatFragment -> {
                Navigation.findNavController(requireView())
                    .navigate(R.id.action_navigation_home_to_usersChatFragment)
            }
        }
        return false
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (this::adapter.isInitialized)
            adapter.stopListening()
        _binding = null
    }

    override fun onLikeClicked(position: Int, imageButton: ImageButton) {
        val list = arr[position].likedBy
        viewModel.likePost(arr[position])
        if (list.contains(currentUser)) {
            imageButton.setImageResource(R.drawable.ic_like)
        } else {
            imageButton.setImageResource(R.drawable.ic_liked_post)

        }

    }

    private fun sendData(post: Post) {
        val bundle = Bundle()
        bundle.putString("selected_userId", post.userId)
        Navigation.findNavController(requireView())
            .navigate(R.id.action_navigation_home_to_navigation_profile,
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