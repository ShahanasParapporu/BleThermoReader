package com.takniatech.contec.data.model

data class User(
    val id: Int = 0,
    val email: String,
    val password: String,
    val name: String,
    val dateOfBirth: String,
    val gender: String,
    val weight: Float,
    val height: Float,
    val profileImageUri: String? = null
)