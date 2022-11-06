package com.example.letsconnect.profile

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.letsconnect.KEY_FOLLOWERS
import com.example.letsconnect.KEY_FOLLOWING
import com.example.letsconnect.Resource
import com.example.letsconnect.models.Users
import com.example.letsconnect.repository.UserRepository
import com.google.firebase.firestore.QuerySnapshot
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FollowViewModel @Inject constructor(private val repository: UserRepository) :
    ViewModel()  {

    private var _allFollowers: MutableStateFlow<Resource<ArrayList<String>>> = MutableStateFlow(Resource.Loading())
    val allFollowers: StateFlow<Resource<ArrayList<String>>> = _allFollowers

    private var _allFollowing: MutableStateFlow<Resource<ArrayList<String>>> = MutableStateFlow(Resource.Loading())
    val allFollowing: StateFlow<Resource<ArrayList<String>>> = _allFollowing

    private var _allUserProfiles: MutableStateFlow<Map<String, Users>> = MutableStateFlow(mapOf())
    val allUserProfiles: StateFlow<Map<String,Users>> = _allUserProfiles

    fun getAllUserProfiles() = viewModelScope.launch(Dispatchers.IO) {
        val usersData = repository.getAllUsers()
        val mappedData = mutableMapOf<String,Users>()
        usersData.data?.forEach {
            mappedData[it.getString("userId").toString()] = it.toObject(Users::class.java)
        }
        _allUserProfiles.emit(mappedData)
    }

    fun getAllFollowers(userId:String) = viewModelScope.launch(Dispatchers.IO) {
        val user = repository.getUser(userId)
        val allFollowers = user.data?.get(KEY_FOLLOWERS) as ArrayList<String>
        _allFollowers.emit(Resource.Success(allFollowers))
    }

    fun getAllFollowing(userId:String) = viewModelScope.launch(Dispatchers.IO) {
        val user = repository.getUser(userId)
        val allFollowing = user.data?.get(KEY_FOLLOWING) as ArrayList<String>
        _allFollowing.emit(Resource.Success(allFollowing))
    }

    fun unfollowUser(userId:String)= viewModelScope.launch(Dispatchers.IO){
        repository.unfollowUser(userId)
    }

    fun removeFollower(userId: String)= viewModelScope.launch(Dispatchers.IO){
        repository.removeFollower(userId)
    }
}