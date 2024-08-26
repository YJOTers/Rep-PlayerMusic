package com.example.playermusic.data

import android.media.MediaPlayer
import com.example.playermusic.MainApplication
import com.example.playermusic.ui.model.MusicModel
import com.example.playermusic.ui.model.UiPlayerMusicModel
import kotlinx.coroutines.flow.MutableStateFlow

object MediaPlayerManager {
    private var mediaPlayer: MediaPlayer? = null
    private var currentMusic: MusicModel? = null
    private var uiState: MutableStateFlow<UiPlayerMusicModel>? = null
    private var database: PlayListDatabase? = null

    fun getMediaPlayer(): MediaPlayer {
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer()
        }
        return mediaPlayer!!
    }

    fun setCurrentMusic(musicName: String, artistName: String){
        currentMusic = MusicModel(
            musicName = musicName,
            artistName = artistName
        )
    }

    fun getCurrentMusic(): MusicModel {
        if(currentMusic == null){
            currentMusic = MusicModel()
        }
        return currentMusic!!
    }

    fun getUiState(): MutableStateFlow<UiPlayerMusicModel> {
        if(uiState == null){
            uiState = MutableStateFlow(UiPlayerMusicModel())
        }
        return uiState!!
    }

    fun getBD(): PlayListDatabase {
        if(database == null){
            database = MainApplication.database
        }
        return database!!
    }
    /** Libera los recursos del app luego de finalizar el servicio **/
    fun releaseMediaPlayer() {
        mediaPlayer?.release()
        mediaPlayer = null
        currentMusic = null
        uiState = null
        database?.close()
        database = null
    }
}