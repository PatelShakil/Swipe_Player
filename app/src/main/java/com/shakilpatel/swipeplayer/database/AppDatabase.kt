package com.shakilpatel.swipeplayer.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Video::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun videoDao(): VideoDao
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "video-db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}