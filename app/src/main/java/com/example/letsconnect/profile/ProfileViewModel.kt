package com.example.letsconnect.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.example.letsconnect.KEY_FOLLOWERS
import com.example.letsconnect.Resource
import com.example.letsconnect.models.Post
import com.example.letsconnect.repository.PostsRepository
import com.example.letsconnect.repository.UserRepository
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repository: UserRepository,
    private val repo: PostsRepository,
) :
    ViewModel() {

    // QuerySnapshot
    private var _currentUserPosts: MutableStateFlow<Resource<QuerySnapshot>> =
        MutableStateFlow(Resource.Loading())
    val currentUserPosts: StateFlow<Resource<QuerySnapshot>> = _currentUserPosts

    //DocumentSnapshot
    private var _currentUserDetails: MutableStateFlow<Resource<DocumentSnapshot>> =
        MutableStateFlow(Resource.Loading())
    val currentUserDetails: StateFlow<Resource<DocumentSnapshot>> = _currentUserDetails

    private var _following: MutableStateFlow<Resource<Boolean>> =
        MutableStateFlow(Resource.Loading())
    val following: StateFlow<Resource<Boolean>> = _following

    private var _totalFollowers: MutableStateFlow<Resource<Int>> =
        MutableStateFlow(Resource.Loading())
    val totalFollowers: StateFlow<Resource<Int>> = _totalFollowers

    private var _totalFollowing: MutableStateFlow<Resource<Int>> =
        MutableStateFlow(Resource.Loading())
    val totalFollowing: StateFlow<Resource<Int>> = _totalFollowing


    fun getCurrentUserPosts(userId: String) = viewModelScope.launch(Dispatchers.IO) {
        _currentUserPosts.emit(Resource.Loading())
        _currentUserPosts.emit(repo.showCurrentUserPosts(userId))
    }

    fun getCurrentUserDetails(userId: String) = viewModelScope.launch(Dispatchers.IO) {
        _currentUserDetails.emit(Resource.Loading())
        _currentUserDetails.emit(repository.showCurrentUserDetails(userId))
    }


    fun followUser(userId: String) = liveData(Dispatchers.IO) {
        repository.followUser(userId).collect{
            response->emit(response)
        }
    }

    fun checkIfFollowing(userId: String) = liveData(Dispatchers.IO) {
        repository.checkIfFollowing(userId).collect{
         response->emit(response)
        }
    }
     fun userSignOut(){
        repository.userSignOut()
    }
    fun likePost(post: Post) = viewModelScope.launch(Dispatchers.IO) {
        repo.likePost(post)
    }

    fun unfollowUser(userId: String) = liveData(Dispatchers.IO) {
        repository.unfollowUser(userId).collect{
            response-> emit(response)
        }
    }

    fun uploadProfilePic(encodedImage: ByteArray) = viewModelScope.launch(Dispatchers.IO) {
        repository.uploadProfilePic(encodedImage)
    }

    fun deletePost(postId: String) = viewModelScope.launch(Dispatchers.IO) {
        repo.deletePost(postId)
    }

    fun updateProfile(name: String, username: String) = liveData(Dispatchers.IO) {
        repository.updateUserProfile(name, username).collect{
            response-> emit(response)
        }
    }

    fun checkUsername(username: String) = liveData(Dispatchers.IO) {
        repository.checkUsername(username).collect { response ->
            emit(response)
        }
    }
}