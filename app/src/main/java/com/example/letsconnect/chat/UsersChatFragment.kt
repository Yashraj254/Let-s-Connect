package com.example.letsconnect.chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.letsconnect.R
import com.example.letsconnect.Response
import com.example.letsconnect.adapters.AllUsersAdapter
import com.example.letsconnect.databinding.FragmentUsersChatBinding
import com.example.letsconnect.models.Users
import com.example.letsconnect.search.SearchViewModel
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class UsersChatFragment : Fragment(), AllUsersAdapter.OnSearchUserItemClicked {
    private var _binding: FragmentUsersChatBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var currentUser: String

    private lateinit var adapter: AllUsersAdapter
    private lateinit var navBar: BottomNavigationView
    private val viewModel: SearchViewModel by activityViewModels()

    private lateinit var arr: ArrayList<Users>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentUsersChatBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val actionBar =  requireActivity().findViewById<MaterialToolbar>(R.id.materialToolbar);
        actionBar.title = "All Chats"

        navBar = requireActivity().findViewById(R.id.nav_view)
        navBar.visibility = View.GONE
        setRecyclerView()
        binding.retryButton.setOnClickListener {
            setRecyclerView()
        }
    }

    private fun setRecyclerView() {
        viewModel.getAllUsers().observe(viewLifecycleOwner){
            when(it){
                is Response.Failure -> {
                    binding.apply {
                    statusBox.isVisible = true
                    rvUsersChats.isVisible = false
                    pbLoading.isVisible = false
                }
                }
                is Response.Loading -> {
                    binding.apply {
                        statusBox.isVisible = false
                        rvUsersChats.isVisible = false
                        pbLoading.isVisible = true
                    }
                }
                is Response.Success -> {
                    binding.apply {
                        statusBox.isVisible = false
                        pbLoading.isVisible = false
                    }

                    if (it.data.isNotEmpty()) {
                        binding.rvUsersChats.isVisible = true
                        arr = it.data as ArrayList<Users>
                        binding.rvUsersChats.layoutManager = LinearLayoutManager(context)
                        adapter = AllUsersAdapter(it.data, this,currentUser)
                        binding.rvUsersChats.adapter = adapter

                    }
                }
            }
        }

    }


    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }


    override fun onUserClicked(position: Int) {
        val bundle = Bundle()
        bundle.putString("receiver_id", arr[position].userId)
        bundle.putString("receiver_name", arr[position].username)
        Navigation.findNavController(requireView())
            .navigate(R.id.action_usersChatFragment_to_chatFragment, bundle)
    }

}