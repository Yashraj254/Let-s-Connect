package com.example.letsconnect.search

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
class SearchViewModel @Inject constructor(private val repository: UserRepository) :
    ViewModel() {

    private var _allUsers: MutableStateFlow<Resource<QuerySnapshot>> = MutableStateFlow(Resource.Loading())
    val allUsers: StateFlow<Resource<QuerySnapshot>> = _allUsers

    private var _allFollowers: MutableStateFlow<Resource<QuerySnapshot>> = MutableStateFlow(Resource.Loading())
    val allFollowers: StateFlow<Resource<QuerySnapshot>> = _allFollowers

    private var _allFollowing: MutableStateFlow<Resource<QuerySnapshot>> = MutableStateFlow(Resource.Loading())
    val allFollowing: StateFlow<Resource<QuerySnapshot>> = _allFollowing

    fun getAllUsers() = viewModelScope.launch(Dispatchers.IO) {
        _allUsers.emit(repository.getAllUsers())
    }

    fun getAllFollowers(userId:String) = viewModelScope.launch(Dispatchers.IO) {
        _allFollowers.emit(repository.getAllFollowers(userId))
    }

    fun getAllFollowing(userId:String) = viewModelScope.launch(Dispatchers.IO) {
        _allFollowing.emit(repository.getAllFollowing(userId))
    }


}
