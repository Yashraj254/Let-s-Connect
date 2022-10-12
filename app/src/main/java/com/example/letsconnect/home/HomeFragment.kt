package com.example.letsconnect.home

import android.os.Bundle
import android.view.*
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.Navigation
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.letsconnect.R
import com.example.letsconnect.Resource
import com.example.letsconnect.adapters.AllPostsFirestoreAdapter
import com.example.letsconnect.databinding.FragmentHomeBinding
import com.example.letsconnect.models.Post
import com.example.letsconnect.showSnackBar
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeFragment : Fragment(), MenuProvider {

    private var _binding: FragmentHomeBinding? = null
    private lateinit var adapter: AllPostsFirestoreAdapter
    private var menu: Menu? = null
    private lateinit var navBar: BottomNavigationView
    private val viewModel: HomeViewModel by activityViewModels()

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

    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        navBar = requireActivity().findViewById(R.id.nav_view)
        navBar.visibility = View.VISIBLE
    }

    private fun setRecyclerView() {
        viewModel.getAllPosts()
        viewModel.allPosts.observe(viewLifecycleOwner) {
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
                        binding.rvAllPosts.layoutManager = LinearLayoutManager(context)
                        val navHostFragment =
                            requireActivity().supportFragmentManager.findFragmentById(R.id.container_fragment) as NavHostFragment
                        adapter =
                            AllPostsFirestoreAdapter(options, navHostFragment,"Home")
                        binding.rvAllPosts.adapter = adapter
                        adapter.startListening()

                    }
                }
            }

        }

    }

    override fun onStop() {
        super.onStop()
        if (this::adapter.isInitialized)
            adapter.stopListening()
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
        _binding = null
    }

}