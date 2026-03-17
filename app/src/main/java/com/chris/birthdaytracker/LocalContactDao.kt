package com.chris.birthdaytracker

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface LocalContactDao {

    @Query("SELECT * FROM local_contacts")
    suspend fun getAll(): List<LocalContactEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(contact: LocalContactEntity)

    @Delete
    suspend fun delete(contact: LocalContactEntity)

    @Query("DELETE FROM local_contacts WHERE id = :id")
    suspend fun deleteById(id: String)
}
