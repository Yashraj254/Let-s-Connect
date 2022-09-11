package com.example.letsconnect

class Users {
    constructor()
    constructor(
        userId: String,
        profileImage:String?,
        username: String?,
        email: String?,
        followers: Int,
        following: Int,
    ) {
        this.userId = userId
        this.username = username
        this.email = email
        this.profileImage = profileImage
        this.followers = followers
        this.following = following
    }

    lateinit var userId: String
    var username: String? = null
    var email: String? = null
    var profileImage:String? = null
    var followers: Int = 0
    var following: Int = 0
}
