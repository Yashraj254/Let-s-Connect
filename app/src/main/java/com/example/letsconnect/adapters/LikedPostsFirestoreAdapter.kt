package com.example.letsconnect.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.letsconnect.R
import com.example.letsconnect.Utils
import com.example.letsconnect.databinding.RvLikePostItemBinding
import com.example.letsconnect.databinding.RvPostItemBinding
import com.example.letsconnect.models.Post
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth

class LikedPostsFirestoreAdapter(
    options: FirestoreRecyclerOptions<Post>,
    private val onPostItemClicked: OnPostItemClicked,
    private val map: Map<String, Post>
) :
    FirestoreRecyclerAdapter<Post, RecyclerView.ViewHolder>(options) {
    val currentUser = FirebaseAuth.getInstance().currentUser!!.uid
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding = RvLikePostItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        binding.onPostItemClicked = onPostItemClicked
        return PostViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int, model: Post) {
        (holder as PostViewHolder).bind(model, position)
        holder.getLikedPosts(model, holder.ibtnLike)

    }

    inner class PostViewHolder(private val binding: RvLikePostItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val ibtnLike = binding.ibtnLike
        fun bind(post: Post, position: Int) {
            binding.post = post
            binding.position = position
            val data = map[post.postId]
            binding.apply {
                tvName.text = data?.name.toString()
                tvUsername.text = data?.username.toString()
                tvPostMessage.text = post.postMessage
                tvTotalComments.text = post.totalComments.toString()
                tvTotalLikes.text = post.likedBy.size.toString()
                tvUploadTime.text = Utils.getTimeAgo(post.uploadTime).toString()
                val image = post.profileImage
                if (image != null)
                    Glide.with(imageView).load(image).into(imageView)

            }

        }

        fun getLikedPosts(
            post: Post,
            ibtnLike: ImageButton,
        ) {
            val list = post.likedBy
            val isLiked = list.contains(currentUser)
            if (isLiked) {
                ibtnLike.setImageResource(R.drawable.ic_liked_post)
            } else {
                ibtnLike.setImageResource(R.drawable.ic_like)
            }
        }
    }

    interface OnPostItemClicked {
        fun onLikeClicked(position: Int, imageButton: ImageButton)
        fun onCommentClicked(position: Int)
        fun onUsernameClicked(position: Int)
        fun onEmailClicked(position: Int)
        fun onLongClick(position: Int): Boolean
    }

}

