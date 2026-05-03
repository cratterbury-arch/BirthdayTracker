package com.chris.birthdaytracker

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ContactMetadataDao {
    @Query("SELECT * FROM contact_metadata WHERE contactKey = :key")
    suspend fun getMetadata(key: String): ContactMetadataEntity?

    @Query("SELECT * FROM contact_metadata")
    suspend fun getAllMetadata(): List<ContactMetadataEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(metadata: ContactMetadataEntity)

    @Query("UPDATE contact_metadata SET isFavorite = :isFavorite WHERE contactKey = :key")
    suspend fun updateFavorite(key: String, isFavorite: Boolean)
}
