package com.example.letsconnect

class Post {
    constructor() {}
    constructor(
        userId: String,
        postId:String,
        profileImage:String?,
        username: String?,
        email: String?,
        totalViews: Int,
        uploadTime: String?,
        postMessage: String?,
        totalLikes: Int,
        totalComments: Int,
    ) {
        this.userId = userId
        this.postId = postId
        this.username = username
        this.email = email
        this.profileImage = profileImage
        this.totalViews = totalViews
        this.uploadTime = uploadTime
        this.postMessage = postMessage
        this.totalLikes = totalLikes
        this.totalComments = totalComments
    }

    lateinit var userId: String
    lateinit var postId: String
    var username: String? = null
    var email: String? = null
    var profileImage:String? = null
    var totalViews: Int = 0
    var uploadTime: String? = null
    var postMessage: String? = null
    var totalLikes: Int = 0
    var totalComments: Int = 0
}
