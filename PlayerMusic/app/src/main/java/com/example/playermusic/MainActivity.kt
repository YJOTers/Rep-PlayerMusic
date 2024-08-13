package com.example.playermusic

import android.Manifest
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.app.ActivityCompat
import com.example.playermusic.service.MusicService
import com.example.playermusic.ui.theme.PlayerMusicTheme
import com.example.playermusic.ui.viewModel.PlayerMusicViewModel

class MainActivity : ComponentActivity() {

    private val vmPlayerMusic by lazy { PlayerMusicViewModel() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        getPermission()
        vmPlayerMusic.getArtistList(applicationContext)
        setContent {
            PlayerMusicTheme(darkTheme = false) {
                AppScreen(vmPlayerMusic = vmPlayerMusic)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        getPermission()
        vmPlayerMusic.getArtistList(applicationContext)
    }

    override fun onDestroy() {
        super.onDestroy()
        //Cerrar servicio
        val serviceIntent = Intent(applicationContext, MusicService::class.java)
        stopService(serviceIntent)
        //Cerrar la aplicación
        finishAffinity()
    }

    private fun getPermission() {
        val permissionRequired = arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
        //Solicita permisos
        ActivityCompat.requestPermissions(this,permissionRequired,0)
    }
}