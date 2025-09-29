package com.takniatech.contec.data.repository

import com.takniatech.contec.data.local.ContecSQLiteHelper
import com.takniatech.contec.data.model.User
import com.takniatech.contec.domain.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val dbHelper: ContecSQLiteHelper
) : UserRepository {

    override suspend fun insertUser(user: User): Long = withContext(Dispatchers.IO) {
        dbHelper.insertUserSync(user)
    }

    override suspend fun getUser(email: String, password: String): User? = withContext(Dispatchers.IO) {
        dbHelper.getUserByEmailAndPasswordSync(email, password)
    }

    override suspend fun getUserById(userId: Int): User? = withContext(Dispatchers.IO) {
        dbHelper.getUserByIdSync(userId)
    }

    override suspend fun updateUser(user: User) {
        withContext(Dispatchers.IO) {
            dbHelper.updateUserSync(user)
        }
    }

    override suspend fun isEmailRegistered(email: String): Boolean = withContext(Dispatchers.IO) {
        dbHelper.getUserByEmailSync(email) != null
    }

    override suspend fun getLoggedInUserId(): Int? = withContext(Dispatchers.IO) {
        dbHelper.getLoggedInUserIdSync()
    }


}
