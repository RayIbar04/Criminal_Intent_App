
package com.example.criminal_intent_app

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "crimes")
data class Crime(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    var title: String = "",
    var dateMillis: Long = System.currentTimeMillis(),
    var isSolved: Boolean = false,
    var suspect: String = "",
    var photoUri: String? = null
)
