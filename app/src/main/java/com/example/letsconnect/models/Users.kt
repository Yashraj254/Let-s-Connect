package com.example.letsconnect.models

class Users {
    constructor()
    constructor(
        userId: String,
        profileImage: String?,
        username: String?,
        name: String?,
        email: String?,
        followers: ArrayList<String>,
        following: ArrayList<String>,
    ) {
        this.userId = userId
        this.username = username
        this.name = name
        this.email = email
        this.profileImage = profileImage
        this.followers = followers
        this.following = following
    }

    lateinit var userId: String
    var username: String? = null
    var name: String? = null
    var email: String? = null
    var profileImage: String? = null
    var followers = ArrayList<String>()
    var following = ArrayList<String>()
}
