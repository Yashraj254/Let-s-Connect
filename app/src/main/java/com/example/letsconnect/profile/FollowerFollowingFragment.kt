package com.example.letsconnect.profile

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
import com.example.letsconnect.Resource
import com.example.letsconnect.adapters.FollowAdapter
import com.example.letsconnect.databinding.FragmentFollowerFollowingBinding
import com.example.letsconnect.models.Users
import com.firebase.ui.firestore.ObservableSnapshotArray
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class FollowerFollowingFragment : Fragment(), FollowAdapter.OnFollowItemClicked {


    private var _binding: FragmentFollowerFollowingBinding? = null
    private val binding get() = _binding!!
    private val viewModel: FollowViewModel by activityViewModels()
    private lateinit var adapter: FollowAdapter
    private lateinit var navBar: BottomNavigationView
    private lateinit var arr: ArrayList<Users>
    private lateinit var title: String
    private lateinit var usersList:String
    @Inject
    lateinit var auth: FirebaseAuth
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentFollowerFollowingBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        title = requireArguments().getString("fragment")!!
        val userId = requireArguments().getString("userId")!!

        val actionBar =  requireActivity().findViewById<MaterialToolbar>(R.id.materialToolbar);
        actionBar.title = title
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
                lifecycleScope.launchWhenCreated {
                    viewModel.allFollowers.collect { getAll(it, "Remove") }
                }
            }
            "Following" -> {
                viewModel.getAllFollowing(userId)
                lifecycleScope.launchWhenCreated {
                    viewModel.allFollowing.collect { getAll(it, "Unfollow") }
                }
            }
        }
    }

    private fun getAll(resource: Resource<ArrayList<String>>, btnText: String) {
        when (resource) {
            is Resource.Error -> {
                binding.apply {
                    statusBox.isVisible = true
                    rvSearch.isVisible = false
                    pbLoading.isVisible = false
                }
            }
            is Resource.Loading -> {
                binding.apply {
                    statusBox.isVisible = false
                    rvSearch.isVisible = false
                    pbLoading.isVisible = true
                }
            }

            is Resource.Success -> {
                binding.apply {
                    statusBox.isVisible = false
                    pbLoading.isVisible = false
                }

                if (resource.data!!.size!=0) {
                    binding.rvSearch.isVisible = true
                    viewLifecycleOwner.lifecycleScope.launchWhenCreated {
                        viewModel.getAllUserProfiles()
                        arr = ArrayList()
                        viewModel.allUserProfiles.collect{
                            it.forEach { map->
                                arr.add(map.value)
                            }
                                binding.rvSearch.layoutManager = LinearLayoutManager(context)
                                adapter = FollowAdapter(resource.data,
                                    this@FollowerFollowingFragment,
                                    btnText,
                                    it)
                                binding.rvSearch.adapter = adapter


                        }
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
            .navigate(R.id.action_followerFollowingFragment_to_navigation_profile, bundle)
    }

    override fun onFollowsClicked(position: Int) {
        val userId = arr[position].userId
        if (title == "Followers") {
            viewModel.removeFollower(userId)
        } else {
            viewModel.unfollowUser(userId)
        }
    }
}