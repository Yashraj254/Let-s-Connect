package com.example.letsconnect.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.letsconnect.Resource
import com.example.letsconnect.models.Users
import com.example.letsconnect.repository.FirebaseRepository
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(private val repository: FirebaseRepository) :
    ViewModel() {

    // QuerySnapshot
    private var _currentUserPosts: MutableLiveData<Resource<QuerySnapshot>> = MutableLiveData()
    val currentUserPosts: LiveData<Resource<QuerySnapshot>> = _currentUserPosts

    //DocumentSnapshot
    private var _currentUserDetails: MutableLiveData<Resource<DocumentSnapshot>> = MutableLiveData()
    val currentUserDetails: LiveData<Resource<DocumentSnapshot>> = _currentUserDetails

    private var _following: MutableLiveData<Resource<QuerySnapshot>> = MutableLiveData()
    val following: LiveData<Resource<QuerySnapshot>> = _following

    fun getCurrentUserPosts(userId: String) = viewModelScope.launch(Dispatchers.IO) {
        _currentUserPosts.postValue(Resource.Loading())
        _currentUserPosts.postValue(repository.showCurrentUserPosts(userId))
    }

    fun getCurrentUserDetails(userId: String) = viewModelScope.launch(Dispatchers.IO) {
        _currentUserDetails.postValue(Resource.Loading())
        _currentUserDetails.postValue(repository.showCurrentUserDetails(userId))
    }

    fun followUser(userId: String,user:HashMap<String, Any>) = viewModelScope.launch(Dispatchers.IO)  {
        repository.followUser(userId, user)
    }

    fun checkIfFollowing(userId: String) = viewModelScope.launch(Dispatchers.IO) {
        _following.postValue(Resource.Loading())
        _following.postValue(repository.checkIfFollowing(userId))
    }
}