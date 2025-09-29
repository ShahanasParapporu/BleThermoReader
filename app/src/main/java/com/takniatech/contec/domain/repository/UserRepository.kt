package com.takniatech.contec.domain.repository

import com.takniatech.contec.data.model.User

interface UserRepository {
    suspend fun insertUser(user: User): Long
    suspend fun getUser(email: String, password: String): User?
    suspend fun getUserById(userId: Int): User?
    suspend fun updateUser(user: User)
    suspend fun isEmailRegistered(email: String): Boolean
    suspend fun getLoggedInUserId(): Int?
}
