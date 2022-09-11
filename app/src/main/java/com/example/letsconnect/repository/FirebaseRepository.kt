package com.example.letsconnect.repository

import com.google.firebase.database.FirebaseDatabase

class FirebaseRepository {
    private val database = FirebaseDatabase.getInstance().getReference("users")

    @Volatile private var INSTANCE:FirebaseRepository?=null
    fun getInstance():FirebaseRepository{
        return INSTANCE?: synchronized(this){
            val instance = FirebaseRepository()
            INSTANCE = instance
            instance
        }
    }
}