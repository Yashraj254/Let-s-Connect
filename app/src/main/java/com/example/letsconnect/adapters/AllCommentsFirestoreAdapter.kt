package com.example.letsconnect.adapters

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.letsconnect.models.Comment
import com.example.letsconnect.R
import com.example.letsconnect.databinding.RvCommentItemBinding
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import java.security.PrivateKey


class AllCommentsFirestoreAdapter(
    private val options: FirestoreRecyclerOptions<Comment>,
    private val navHostFragment: NavHostFragment,
) :
    FirestoreRecyclerAdapter<Comment, RecyclerView.ViewHolder>(options) {
    private var _totalComments: MutableLiveData<Int> = MutableLiveData()
    val totalComments: LiveData<Int> = _totalComments
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding =
            RvCommentItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PostViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int, model: Comment) {
        (holder as PostViewHolder).bind(model)

    }

    override fun getItemCount(): Int {
        _totalComments.value = super.getItemCount()
        Log.d("MyTag", "getItemCount: ${ totalComments.value}")

        return super.getItemCount()
    }
    inner class PostViewHolder(private val binding: RvCommentItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(comment: Comment) {
            binding.apply {
                tvUsername.text = comment.username
                tvUsermail.text = comment.email
                tvComment.text = comment.commentMessage
                val image = comment.profileImage
                if(image!=null)
                    Glide.with(ivProfilePic).load(image).into(ivProfilePic)
                tvUsermail.setOnClickListener {
                    val bundle = Bundle()
                    bundle.putString("selected_userId", comment.userId)
                    val navController: NavController = navHostFragment.navController
                    navController.navigate(R.id.action_postFragment_to_navigation_profile, bundle)
                }
                tvUsername.setOnClickListener {
                    val bundle = Bundle()
                    bundle.putString("selected_userId", comment.userId)
                    val navController: NavController = navHostFragment.navController
                    navController.navigate(R.id.action_postFragment_to_navigation_profile, bundle)
                }
            }
        }
    }
}