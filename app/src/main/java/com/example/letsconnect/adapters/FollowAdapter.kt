package com.example.letsconnect.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.letsconnect.databinding.RvFollowItemBinding
import com.example.letsconnect.models.Users
import com.google.firebase.auth.FirebaseAuth

class FollowAdapter(
    private val followersList:ArrayList<String>,
    private val followItemClicked:OnFollowItemClicked,
    private val btnTextFollows:String,
    private val map: Map<String,Users>,
    private val selectedUser:String
) :
    RecyclerView.Adapter<FollowAdapter.UserViewHolder>() {
    val currentUser = FirebaseAuth.getInstance().currentUser!!.uid
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding = RvFollowItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        binding.onFollowItemClicked = followItemClicked
        return UserViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemCount(): Int {
        return followersList.size
    }

    inner class UserViewHolder(private val binding: RvFollowItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind( position:Int) {
            val userId = followersList[position]
            binding.position = position


            val data = map[userId]
            binding.apply {
                if(currentUser!=selectedUser)
                    btnFollows.isVisible = false
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