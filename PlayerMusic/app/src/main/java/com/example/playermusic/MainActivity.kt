package com.example.playermusic

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import com.example.playermusic.ui.theme.PlayerMusicTheme
import com.example.playermusic.ui.viewModel.PlayerMusicViewModel

class MainActivity : ComponentActivity() {

    private val vmPlayerMusic by lazy { PlayerMusicViewModel() }

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        getPermission()
        vmPlayerMusic.getArtistList(applicationContext)
        vmPlayerMusic.getPlayList()
        vmPlayerMusic.getConfig(applicationContext)
        setContent {
            PlayerMusicTheme {
                AppScreen(vmPlayerMusic = vmPlayerMusic)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onResume() {
        super.onResume()
        getPermission()
        vmPlayerMusic.getArtistList(applicationContext)
        vmPlayerMusic.getPlayList()
        vmPlayerMusic.getConfig(applicationContext)
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun getPermission() {
        val permissionRequired = arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.BLUETOOTH_CONNECT
        )
        //Solicita permisos
        ActivityCompat.requestPermissions(this,permissionRequired,0)
    }
}