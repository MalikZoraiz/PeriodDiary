package com.nexadev.perioddiary.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)

    @Query("SELECT * FROM user LIMIT 1")
    suspend fun getUser(): User?

    @Query("SELECT * FROM user LIMIT 1")
    fun getUserFlow(): Flow<User?>

    @Query("SELECT * FROM user WHERE backupEmail = :email LIMIT 1")
    suspend fun findUserByEmail(email: String): User?

    @Query("DELETE FROM user")
    suspend fun deleteAllUsers()
}