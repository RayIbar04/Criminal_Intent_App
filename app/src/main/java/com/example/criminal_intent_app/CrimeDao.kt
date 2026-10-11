package com.example.criminal_intent_app

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CrimeDao {

    @Query("SELECT * FROM crimes ORDER BY dateMillis DESC")
    fun getCrimes(): Flow<List<Crime>>

    @Query("SELECT * FROM crimes WHERE id = :crimeId")
    fun getCrime(crimeId: Long): Flow<Crime?>

    @Insert
    suspend fun insertCrime(crime: Crime): Long

    @Update
    suspend fun updateCrime(crime: Crime)

    @Delete
    suspend fun deleteCrime(crime: Crime)
}
