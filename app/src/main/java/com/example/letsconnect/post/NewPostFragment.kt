package com.example.letsconnect.post

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.letsconnect.R
import com.example.letsconnect.Response
import com.example.letsconnect.databinding.FragmentNewPostBinding
import com.example.letsconnect.showSnackBar
import com.google.android.material.appbar.MaterialToolbar
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
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentNewPostBinding.inflate(layoutInflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val actionBar =  requireActivity().findViewById<MaterialToolbar>(R.id.materialToolbar);
        actionBar.title = "New Post"
            binding.btnAddPost.setOnClickListener {
                if (!binding.etPostMessage.text.isNullOrBlank())
                {
                    addNewPost(binding.etPostMessage.text.toString())
                }
            }
    }
    private fun addNewPost(message: String) {
        viewModel.addNewPost(message).observe(viewLifecycleOwner){
            when(it){
                is Response.Failure->{
                    binding.progressBar2.isVisible = false
                    showSnackBar("Error: "+it.errorMessage)
                }
                is Response.Loading->{
                    binding.progressBar2.isVisible = true
                }
                is Response.Success->{
                    if(it.data){
                        binding.progressBar2.isVisible = false
                        binding.etPostMessage.text = null
                        showSnackBar("Post Uploaded.")
                    }

                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}