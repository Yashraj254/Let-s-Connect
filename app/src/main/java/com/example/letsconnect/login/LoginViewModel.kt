package com.example.letsconnect.login

import androidx.lifecycle.ViewModel
import com.example.letsconnect.repository.LoginRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(repository: LoginRepository) : ViewModel() {

    fun signIn(){

    }
}