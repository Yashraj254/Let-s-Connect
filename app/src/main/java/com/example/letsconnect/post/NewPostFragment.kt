package com.example.letsconnect.post

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.letsconnect.databinding.FragmentNewPostBinding
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NewPostFragment : Fragment() {

    private var _binding: FragmentNewPostBinding? = null
    private val viewModel: PostViewModel by activityViewModels()

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentNewPostBinding.inflate(layoutInflater, container, false)
        requireActivity().title = "New Post"
        binding.btnAddPost.setOnClickListener {
            addNewPost(binding.etPostMessage.text.toString())
        }
        return binding.root
    }

    private fun addNewPost(message: String) {
        viewModel.addNewPost(message)
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}