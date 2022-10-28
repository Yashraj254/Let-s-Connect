package com.example.letsconnect.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.letsconnect.databinding.RvSearchUserItemBinding
import com.example.letsconnect.models.Users
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions

class AllUsersFirestoreAdapter(
    options: FirestoreRecyclerOptions<Users>,
    private val userItemClicked:OnSearchUserItemClicked,
) :
    FirestoreRecyclerAdapter<Users, RecyclerView.ViewHolder>(options) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding = RvSearchUserItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        binding.onSearchUserItemClicked = userItemClicked
        return UserViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int, model: Users) {
        (holder as UserViewHolder).bind(model,position)
    }

    inner class UserViewHolder(private val binding: RvSearchUserItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(users: Users,position:Int) {
            binding.users = users
            binding.position = position
            binding.apply {
                tvChatName.text = users.name
                tvChatUserName.text = users.username
                val image = users.profileImage
                if (image != null)
                    Glide.with(ivProfileImage).load(image).into(binding.ivProfileImage)
            }
        }
    }
    interface OnSearchUserItemClicked{
        fun onUserClicked(position: Int)
    }
}