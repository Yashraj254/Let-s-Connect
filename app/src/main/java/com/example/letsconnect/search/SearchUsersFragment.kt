package com.example.letsconnect.search

import android.media.CamcorderProfile.getAll
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
import com.example.letsconnect.R
import com.example.letsconnect.Response
import com.example.letsconnect.adapters.AllUsersAdapter
import com.example.letsconnect.databinding.FragmentSearchUsersBinding
import com.example.letsconnect.models.Users
import com.example.letsconnect.showSnackBar
import com.google.android.material.appbar.MaterialToolbar
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SearchUsersFragment : Fragment(), AllUsersAdapter.OnSearchUserItemClicked {
    private var _binding: FragmentSearchUsersBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SearchViewModel by activityViewModels()
    private lateinit var adapter: AllUsersAdapter
    private lateinit var arr: ArrayList<Users>

    @Inject
    lateinit var auth: FirebaseAuth
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentSearchUsersBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val actionBar =  requireActivity().findViewById<MaterialToolbar>(R.id.materialToolbar);
        actionBar.title = "All Users"
        setRecyclerView()
        binding.retryButton.setOnClickListener {
            setRecyclerView()
        }
    }

    private fun setRecyclerView() {
        viewModel.getAllUsers().observe(viewLifecycleOwner){
            when(it){
                is Response.Failure -> {
                    showSnackBar("Error: "+it.errorMessage)
                    binding.apply {
                        statusBox.isVisible = true
                        rvSearch.isVisible = false
                        pbLoading.isVisible = false
                    }
                }
                is Response.Loading -> {
                    binding.apply {
                        statusBox.isVisible = false
                        rvSearch.isVisible = false
                        pbLoading.isVisible = true
                    }
                }
                is Response.Success -> {
                    binding.apply {
                        statusBox.isVisible = false
                        pbLoading.isVisible = false
                    }

                    if (it.data.isNotEmpty()) {
                        binding.rvSearch.isVisible = true

                        arr = it.data as ArrayList<Users>
                        binding.rvSearch.layoutManager = LinearLayoutManager(context)
                        adapter = AllUsersAdapter(it.data, this, auth.currentUser!!.uid)
                        binding.rvSearch.adapter = adapter

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
        bundle.putString("selected_userId", arr[position].userId)
        Navigation.findNavController(requireView())
            .navigate(R.id.action_navigation_search_users_to_navigation_profile, bundle)
    }


}