package com.example.letsconnect.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.letsconnect.Resource
import com.example.letsconnect.repository.FirebaseRepository
import com.google.firebase.firestore.QuerySnapshot
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(private val repository: FirebaseRepository) :
    ViewModel() {

    private var _allUsers: MutableLiveData<Resource<QuerySnapshot>> = MutableLiveData()
    val allUsers: LiveData<Resource<QuerySnapshot>> = _allUsers

    private var _allFollowers: MutableLiveData<Resource<QuerySnapshot>> = MutableLiveData()
    val allFollowers: LiveData<Resource<QuerySnapshot>> = _allFollowers

    private var _allFollowing: MutableLiveData<Resource<QuerySnapshot>> = MutableLiveData()
    val allFollowing: LiveData<Resource<QuerySnapshot>> = _allFollowing

    fun getAllUsers() = viewModelScope.launch(Dispatchers.IO) {
        _allUsers.postValue(Resource.Loading())
        _allUsers.postValue(repository.getAllUsers())
    }

    fun getAllFollowers(userId:String) = viewModelScope.launch(Dispatchers.IO) {
        _allFollowers.postValue(Resource.Loading())
        _allFollowers.postValue(repository.getAllFollowers(userId))
    }

    fun getAllFollowing(userId:String) = viewModelScope.launch(Dispatchers.IO) {
        _allFollowing.postValue(Resource.Loading())
        _allFollowing.postValue(repository.getAllFollowing(userId))
    }


}
