package com.example.letsconnect.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.letsconnect.adapters.AllPostsFirestoreAdapter
import com.example.letsconnect.KEY_ALL_POSTS
import com.example.letsconnect.Post
import com.example.letsconnect.R
import com.example.letsconnect.databinding.FragmentHomeBinding
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseFirestore.getInstance()
    private val userRef = database.collection(KEY_ALL_POSTS).orderBy("TIME_STAMP",Query.Direction.DESCENDING)
    private lateinit var adapter: AllPostsFirestoreAdapter

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root
        homeViewModel.text.observe(viewLifecycleOwner) {
        }
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setRecyclerView()
    }
    private fun setRecyclerView() {
        val options: FirestoreRecyclerOptions<Post> =
            FirestoreRecyclerOptions.Builder<Post>().setQuery(userRef, Post::class.java).build()
        binding.rvAllPosts.layoutManager = LinearLayoutManager(context)
        val navHostFragment =
            requireActivity().supportFragmentManager.findFragmentById(R.id.container_fragment) as NavHostFragment
        adapter = AllPostsFirestoreAdapter(options,navHostFragment)

        binding.rvAllPosts.adapter = adapter
    }


    override fun onStart() {
        super.onStart()
        adapter.startListening()

    }

    override fun onStop() {
        super.onStop()
        adapter.stopListening()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}