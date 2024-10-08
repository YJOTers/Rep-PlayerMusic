package com.example.playermusic.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.playermusic.ui.model.MusicListModel

@Database(entities = [MusicListModel::class], version = 1)
@TypeConverters(Converters::class)
abstract class PlayListDatabase: RoomDatabase() {

    companion object {
        const val NAME = "bd_playlist"
    }

    abstract fun playListDao(): PlayListDao
}