package com.chris.birthdaytracker

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [LocalContactEntity::class, ContactMetadataEntity::class, EventEntity::class],
    version = 6,
    exportSchema = false
)
@TypeConverters(LocalDateConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun contactDao(): LocalContactDao
    abstract fun metadataDao(): ContactMetadataDao
    abstract fun eventDao(): EventDao
}
