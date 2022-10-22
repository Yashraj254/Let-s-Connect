package com.example.letsconnect.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.letsconnect.models.ChatMessage
import com.example.letsconnect.databinding.RvReceiverItemBinding
import com.example.letsconnect.databinding.RvSentItemBinding
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth

class ChatsFirestoreAdapter(
    options: FirestoreRecyclerOptions<ChatMessage>
) :
    FirestoreRecyclerAdapter<ChatMessage, RecyclerView.ViewHolder>(options) {
    private var ITEM_SENT = 1
    private var ITEM_RECEIVED = 2
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val sentBinding =
            RvSentItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        val receiverBinding =
            RvReceiverItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return if (viewType == ITEM_SENT)
            ChatSentViewHolder(sentBinding)
        else
            ChatReceiveViewHolder(receiverBinding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int, model: ChatMessage) {
        if (holder.itemViewType == ITEM_SENT)
            (holder as ChatSentViewHolder).bind(model)
        else
            (holder as ChatReceiveViewHolder).bind(model)
    }

    override fun getItemViewType(position: Int): Int {
        return if (FirebaseAuth.getInstance().uid == getItem(position).senderId) ITEM_SENT else ITEM_RECEIVED
    }


    inner class ChatSentViewHolder(private val binding: RvSentItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(chat: ChatMessage) {
            binding.apply {
                tvSenderMessage.text = chat.message
                tvTimeStamp.text = chat.sentTime
            }
        }
    }

    inner class ChatReceiveViewHolder(private val binding: RvReceiverItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(chat: ChatMessage) {
            binding.apply {
                tvReceiverMessage.text = chat.message
                tvTimeStamp.text = chat.sentTime
            }
        }
    }
}