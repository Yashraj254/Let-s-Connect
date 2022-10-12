package com.example.letsconnect.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.letsconnect.databinding.RvPostItemBinding
import com.example.letsconnect.models.Post

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
                val image = post.profileImage
                if(image!=null)
                Glide.with(imageView).load(image).into(imageView)
            }
        }
    }
}