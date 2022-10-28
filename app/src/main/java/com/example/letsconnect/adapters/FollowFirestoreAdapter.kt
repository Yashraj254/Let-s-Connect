package com.example.letsconnect.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.letsconnect.databinding.RvFollowItemBinding
import com.example.letsconnect.models.Users
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions

class FollowFirestoreAdapter(
    options: FirestoreRecyclerOptions<Users>,
    private val followItemClicked:OnFollowItemClicked,
    private val btnTextFollows:String,
    private val map: Map<String,Users>
) :
    FirestoreRecyclerAdapter<Users, RecyclerView.ViewHolder>(options) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding = RvFollowItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        binding.onFollowItemClicked = followItemClicked
        return UserViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int, model: Users) {
        (holder as UserViewHolder).bind(model,position)
    }

    inner class UserViewHolder(private val binding: RvFollowItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(users: Users, position:Int) {
            binding.users = users
            binding.position = position
            val data = map[users.userId]
            binding.apply {
                tvChatName.text = data?.name
                tvChatUsername.text = data?.username
                btnFollows.text = btnTextFollows
                val image = data?.profileImage
                if (image != null)
                    Glide.with(ivProfileImage).load(image).into(binding.ivProfileImage)
            }
        }
    }
    interface OnFollowItemClicked{
        fun onUserClicked(position: Int)
        fun onFollowsClicked(position: Int)
    }
}