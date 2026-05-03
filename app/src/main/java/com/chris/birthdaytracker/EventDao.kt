package com.chris.birthdaytracker

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface EventDao {
    @Query("SELECT * FROM events")
    suspend fun getAll(): List<EventEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(event: EventEntity)

    @Delete
    suspend fun delete(event: EventEntity)

    @Query("DELETE FROM events WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("UPDATE events SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun updateFavorite(id: String, isFavorite: Boolean)
}
