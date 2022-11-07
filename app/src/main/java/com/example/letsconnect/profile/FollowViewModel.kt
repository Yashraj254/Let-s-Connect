package com.example.letsconnect.profile

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
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

    fun getAllUserProfiles() = liveData(Dispatchers.IO) {
        repository.getAllUserProfiles().collect{response->
            emit(response)
        }
    }

    fun getAllFollowers(userId:String) = liveData(Dispatchers.IO) {
       repository.getAllFollowers(userId).collect{
           response->emit(response)
       }
    }

    fun getAllFollowing(userId:String) = liveData(Dispatchers.IO) {
        repository.getAllFollowing(userId).collect{
                response->emit(response)
        }
    }

    fun unfollowUser(userId:String)= viewModelScope.launch(Dispatchers.IO){
        repository.unfollowUser(userId)
    }

    fun removeFollower(userId: String)= viewModelScope.launch(Dispatchers.IO){
        repository.removeFollower(userId)
    }
}