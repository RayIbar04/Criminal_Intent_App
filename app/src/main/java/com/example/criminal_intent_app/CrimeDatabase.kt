package com.example.criminal_intent_app

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [Crime::class],
    version = 1,
    exportSchema = false
)
abstract class CrimeDatabase : RoomDatabase() {

    abstract fun crimeDao(): CrimeDao

    companion object {
        @Volatile
        private var INSTANCE: CrimeDatabase? = null

        fun getDatabase(context: Context): CrimeDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CrimeDatabase::class.java,
                    "criminal_intent_database"
                ).build()

                INSTANCE = instance
                instance
            }
        }
    }
}
