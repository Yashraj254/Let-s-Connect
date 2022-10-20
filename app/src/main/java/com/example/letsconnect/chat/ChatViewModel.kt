package com.example.letsconnect.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.letsconnect.Resource
import com.example.letsconnect.repository.UserRepository
import com.google.firebase.firestore.QuerySnapshot
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(private val repository: UserRepository) :
    ViewModel() {

    private var _allFollowing: MutableStateFlow<Resource<QuerySnapshot>> = MutableStateFlow(Resource.Loading())
    val allFollowing: StateFlow<Resource<QuerySnapshot>> = _allFollowing

    private var _allChats: MutableStateFlow<Resource<QuerySnapshot>> = MutableStateFlow(Resource.Loading())
    val allChats: StateFlow<Resource<QuerySnapshot>> = _allChats

    fun getAllFollowing(userId: String) = viewModelScope.launch(Dispatchers.IO) {
        _allFollowing.emit(repository.getAllFollowing(userId))
    }

    fun sendAndReceiveMessage(message: String, userId: String) =
        viewModelScope.launch(Dispatchers.IO) {
            repository.sendAndReceiveMessage(message, userId)
        }

    fun getAllChats(senderRoom: String) = viewModelScope.launch(Dispatchers.IO) {
        _allChats.emit(repository.getAllChats(senderRoom))
    }

}