package com.example.playermusic.ui.model

data class UiPlayerMusicModel(
    //Estados del reproductor de música
    val uiArtistList: List<MusicListModel> = listOf(),
    val uiPlayList: List<MusicListModel> = listOf(),
    val uiMusicList: List<MusicModel> = listOf(),
    val uiIsPause: Boolean = true,
    val uiIsShuffle: Boolean = false,
    val uiIsCompletion: Boolean = false,
    val uiIsPlayList: Boolean = false,
    val uiRepeat: RepeatOptions = RepeatOptions.Off,
    val uiCountIndex: Int = 0,//Indice de lista de indices aleatorios
    val uiCurrentArtistListIndex: Int = 0,//Indice actual del artista
    val uiCurrentPlayListIndex: Int = 0,//Indice actual de la playlist
    val uiCurrentMusicListIndex: Int = 0,//Indice actual de la música por artista o playlist
    val uiCurrentDuration: Long = 0L,//Duración actual de la música
)

enum class RepeatOptions{ Current, All, Off }