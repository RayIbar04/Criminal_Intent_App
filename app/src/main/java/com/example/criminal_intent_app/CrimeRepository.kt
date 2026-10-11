package com.example.criminal_intent_app

import kotlinx.coroutines.flow.Flow

class CrimeRepository(
    private val crimeDao: CrimeDao
) {
    val crimes: Flow<List<Crime>> = crimeDao.getCrimes()

    fun getCrime(crimeId: Long): Flow<Crime?> {
        return crimeDao.getCrime(crimeId)
    }

    suspend fun addCrime(crime: Crime): Long {
        return crimeDao.insertCrime(crime)
    }

    suspend fun updateCrime(crime: Crime) {
        crimeDao.updateCrime(crime)
    }

    suspend fun deleteCrime(crime: Crime) {
        crimeDao.deleteCrime(crime)
    }
}
