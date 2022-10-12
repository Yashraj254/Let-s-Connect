package com.example.letsconnect.search

import android.media.CamcorderProfile.getAll
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.letsconnect.R
import com.example.letsconnect.Resource
import com.example.letsconnect.adapters.AllUsersFirestoreAdapter
import com.example.letsconnect.adapters.UsersChatsFirestoreAdapter
import com.example.letsconnect.databinding.FragmentSearchUsersBinding
import com.example.letsconnect.databinding.FragmentUsersChatBinding
import com.example.letsconnect.models.Users
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SearchUsersFragment : Fragment() {
    private var _binding: FragmentSearchUsersBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SearchViewModel by activityViewModels()
    private lateinit var adapter: AllUsersFirestoreAdapter
    private lateinit var navBar: BottomNavigationView

    @Inject
    lateinit var auth: FirebaseAuth
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentSearchUsersBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        var title = arguments?.getString("fragment", "AllUsers")
        var userId = arguments?.getString("userId", auth.currentUser!!.uid)
        if (title == null)
            title = "AllUsers"
        if (userId == null)
            userId = auth.currentUser!!.uid
        requireActivity().title = title
        navBar = requireActivity().findViewById(R.id.nav_view)
        if (title == "Followers" || title == "Following") {
            navBar.visibility = View.GONE
        }
        setRecyclerView(title, userId)
    }

    private fun setRecyclerView(title: String, userId: String) {
        when (title) {
            "Followers" -> {
                viewModel.getAllFollowers(userId)
                viewModel.allFollowers.observe(viewLifecycleOwner) { getAll(it) }
            }
            "Following" -> {
                viewModel.getAllFollowing(userId)
                viewModel.allFollowing.observe(viewLifecycleOwner) { getAll(it) }
            }
            "AllUsers" -> {
                viewModel.getAllUsers()
                viewModel.allUsers.observe(viewLifecycleOwner) { getAll(it) }
            }
        }
    }

    private fun getAll(resource: Resource<QuerySnapshot>) {
        when (resource) {
            is Resource.Loading -> {}
            is Resource.Error -> {}
            is Resource.Success -> {
                val options: FirestoreRecyclerOptions<Users> =
                    FirestoreRecyclerOptions.Builder<Users>()
                        .setQuery(resource.data!!.query, Users::class.java).build()
                binding.rvSearch.layoutManager = LinearLayoutManager(context)
                adapter = AllUsersFirestoreAdapter(options)
                binding.rvSearch.adapter = adapter
                adapter.startListening()
            }
        }
    }

    override fun onStop() {
        super.onStop()
        navBar.visibility = View.VISIBLE
        adapter.stopListening()
    }

}