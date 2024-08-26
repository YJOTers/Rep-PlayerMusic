package com.example.playermusic.ui.model

data class MusicModel(
    //Datos de la música según el artista
    val musicPath: String = "",//Ruta del archivo de música
    val musicDuration: Long = 0L,//Duración total de la música en Long
    val musicDurationFormat: String = "", //Duración total de la música con formato m:s
    val musicName: String = "",
    val artistName: String = "",
    val albumName: String = "",
    val albumUri: String = ""//Uri de la imagen del album
)