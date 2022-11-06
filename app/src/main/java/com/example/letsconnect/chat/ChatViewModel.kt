package com.example.letsconnect.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.letsconnect.KEY_FOLLOWING
import com.example.letsconnect.Resource
import com.example.letsconnect.models.Users
import com.example.letsconnect.repository.UserRepository
import com.google.firebase.firestore.QuerySnapshot
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.forEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(private val repository: UserRepository) :
    ViewModel() {

    private var _allFollowing: MutableStateFlow<Resource<ArrayList<String>>> = MutableStateFlow(Resource.Loading())
    val allFollowing: StateFlow<Resource<ArrayList<String>>> = _allFollowing

    private var _allChats: MutableStateFlow<Resource<QuerySnapshot>> = MutableStateFlow(Resource.Loading())
    val allChats: StateFlow<Resource<QuerySnapshot>> = _allChats

    private var _allUserProfiles: MutableStateFlow<Map<String, Users>> = MutableStateFlow(mapOf())
    val allUserProfiles: StateFlow<Map<String, Users>> = _allUserProfiles


fun getAllFollowing(userId:String) = viewModelScope.launch(Dispatchers.IO) {
    val user = repository.getUser(userId)
    val allFollowing = user.data?.get(KEY_FOLLOWING) as ArrayList<String>
    _allFollowing.emit(Resource.Success(allFollowing))
}

    fun getAllUserProfiles() = viewModelScope.launch(Dispatchers.IO) {
        val usersData = repository.getAllUsers()
        val mappedData = mutableMapOf<String,Users>()
        usersData.data?.forEach {
            mappedData[it.getString("userId").toString()] = it.toObject(Users::class.java)
        }
        _allUserProfiles.emit(mappedData)
    }

    fun sendAndReceiveMessage(message: String, userId: String) =
        viewModelScope.launch(Dispatchers.IO) {
            repository.sendAndReceiveMessage(message, userId)
        }

    fun getAllChats(senderRoom: String) = viewModelScope.launch(Dispatchers.IO) {
        _allChats.emit(repository.getAllChats(senderRoom))
    }

}