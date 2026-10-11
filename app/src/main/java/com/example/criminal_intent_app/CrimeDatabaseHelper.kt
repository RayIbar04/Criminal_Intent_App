package com.example.criminal_intent_app
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class CrimeDatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE crimes (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                title TEXT NOT NULL,
                date_millis INTEGER NOT NULL,
                is_solved INTEGER NOT NULL,
                suspect TEXT NOT NULL,
                photo_uri TEXT
            )
            """.trimIndent()
        )
    }

    override fun onUpgrade(
        db: SQLiteDatabase,
        oldVersion: Int,
        newVersion: Int
    ) {
        db.execSQL("DROP TABLE IF EXISTS crimes")
        onCreate(db)
    }

    fun getAllCrimes(): List<Crime> {
        val crimes = mutableListOf<Crime>()

        readableDatabase.query(
            "crimes",
            null,
            null,
            null,
            null,
            null,
            "date_millis DESC"
        ).use { cursor ->

            while (cursor.moveToNext()) {
                crimes.add(
                    Crime(
                        id = cursor.getLong(
                            cursor.getColumnIndexOrThrow("id")
                        ),
                        title = cursor.getString(
                            cursor.getColumnIndexOrThrow("title")
                        ),
                        dateMillis = cursor.getLong(
                            cursor.getColumnIndexOrThrow("date_millis")
                        ),
                        isSolved = cursor.getInt(
                            cursor.getColumnIndexOrThrow("is_solved")
                        ) == 1,
                        suspect = cursor.getString(
                            cursor.getColumnIndexOrThrow("suspect")
                        ),
                        photoUri = cursor.getString(
                            cursor.getColumnIndexOrThrow("photo_uri")
                        )
                    )
                )
            }
        }

        return crimes
    }

    fun saveCrime(crime: Crime): Long {
        val values = ContentValues().apply {
            put("title", crime.title)
            put("date_millis", crime.dateMillis)
            put("is_solved", if (crime.isSolved) 1 else 0)
            put("suspect", crime.suspect)
            put("photo_uri", crime.photoUri)
        }

        return if (crime.id == 0L) {
            writableDatabase.insert("crimes", null, values)
        } else {
            writableDatabase.update(
                "crimes",
                values,
                "id = ?",
                arrayOf(crime.id.toString())
            )
            crime.id
        }
    }

    fun deleteCrime(id: Long) {
        writableDatabase.delete(
            "crimes",
            "id = ?",
            arrayOf(id.toString())
        )
    }

    companion object {
        private const val DATABASE_NAME = "criminal_intent.db"
        private const val DATABASE_VERSION = 1
    }
}
