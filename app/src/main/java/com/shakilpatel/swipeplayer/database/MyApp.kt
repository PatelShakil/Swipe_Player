package com.shakilpatel.swipeplayer.database

import android.app.Application
import androidx.room.Room

class MyApp : Application() {
    companion object {
        lateinit var database: AppDatabase
    }

    override fun onCreate() {
        super.onCreate()
        database = AppDatabase.getInstance(this)
    }
}