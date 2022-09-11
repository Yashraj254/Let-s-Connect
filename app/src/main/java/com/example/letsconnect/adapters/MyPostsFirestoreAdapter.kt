package com.example.letsconnect.adapters

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.RecyclerView
import com.example.letsconnect.Post
import com.example.letsconnect.R
import com.example.letsconnect.databinding.RvPostItemBinding
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions

class MyPostsFirestoreAdapter(
    private val options: FirestoreRecyclerOptions<Post>,
    private val navHostFragment: NavHostFragment
) :
    FirestoreRecyclerAdapter<Post, RecyclerView.ViewHolder>(options) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding = RvPostItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PostViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int, model: Post) {
        (holder as PostViewHolder).bind(model)

    }

    inner class PostViewHolder(private val binding: RvPostItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(post: Post) {
            binding.apply {
                tvUsername.text = post.username
                tvEmail.text = post.email
                tvPostMessage.text = post.postMessage
                tvTotalComments.text = post.totalComments.toString()
                tvTotalLikes.text = post.totalLikes.toString()
                tvViews.text = "Views: ${post.totalViews.toString()}"

            }
        }
    }
}