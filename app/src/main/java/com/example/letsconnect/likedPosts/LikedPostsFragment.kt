package com.example.letsconnect.likedPosts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.letsconnect.R
import com.example.letsconnect.Resource
import com.example.letsconnect.adapters.AllPostsFirestoreAdapter
import com.example.letsconnect.databinding.FragmentLikedPostsBinding
import com.example.letsconnect.models.Post
import com.example.letsconnect.showSnackBar
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LikedPostsFragment : Fragment() {

    private var _binding: FragmentLikedPostsBinding? = null
    private val viewModel: LikedPostsViewModel by activityViewModels()
    private lateinit var adapter: AllPostsFirestoreAdapter

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
    }
    private fun setRecyclerView() {
        viewModel.getLikedPosts()
        viewModel.likedPosts.observe(viewLifecycleOwner){
            when (it) {
                is Resource.Error -> {
                    showSnackBar(message = it.message!!)
                }
                is Resource.Loading -> {
                }
                is Resource.Success -> {
                    if (!it.data!!.isEmpty) {
                        val options = FirestoreRecyclerOptions.Builder<Post>()
                            .setQuery(it.data.query, Post::class.java)
                            .build()
                        binding.rvLikedPosts.layoutManager = LinearLayoutManager(context)
                        val navHostFragment =
                            requireActivity().supportFragmentManager.findFragmentById(R.id.container_fragment) as NavHostFragment
                        adapter = AllPostsFirestoreAdapter(options, navHostFragment,"LikedPosts")
                        binding.rvLikedPosts.adapter = adapter
                        adapter.startListening()

                    }
                }
            }

        }

    }

    override fun onStop() {
        super.onStop()
        if(this::adapter.isInitialized)
            adapter.stopListening()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}