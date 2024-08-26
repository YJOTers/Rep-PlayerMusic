package com.example.playermusic.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.MediaPlayer
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.example.playermusic.MainActivity
import com.example.playermusic.R
import com.example.playermusic.data.MediaPlayerManager
import com.example.playermusic.ui.model.MusicModel
import com.example.playermusic.ui.model.UiPlayerMusicModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

class MusicService : Service() {
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var currentMusic: MusicModel
    private lateinit var wakeLock: PowerManager.WakeLock
    private lateinit var uiState: MutableStateFlow<UiPlayerMusicModel>
    private val notificationId = 1
    private val channelId = "MUSIC_CHANNEL"

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                channelId,
                "Music Service Channel",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
            val powerManager = getSystemService(POWER_SERVICE) as PowerManager
            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                "MyApp::MusicPlayerWakelock")
            wakeLock.acquire(5*60*1000L) // 5 minutos
            registerReceiver(stopServiceReceiver,IntentFilter("ACTION_STOP_SERVICE"),
                RECEIVER_NOT_EXPORTED)
            registerReceiver(bluetoothReceiver,IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED),
                RECEIVER_NOT_EXPORTED)
        }else{
            registerReceiver(stopServiceReceiver,IntentFilter("ACTION_STOP_SERVICE"))
            registerReceiver(bluetoothReceiver,IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED))
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        mediaPlayer = MediaPlayerManager.getMediaPlayer()
        currentMusic = MediaPlayerManager.getCurrentMusic()
        uiState = MediaPlayerManager.getUiState()
        val notification = createNotification()
        startForeground(notificationId, notification)
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        //Libera memoria del wakelock
        wakeLock.release()
        //Libera memoria del mediaplayer
        MediaPlayerManager.releaseMediaPlayer()
        //Libera memoria del receiver
        unregisterReceiver(stopServiceReceiver)
        unregisterReceiver(bluetoothReceiver)
        //Detiene el estado de primer plano
        stopForeground(STOP_FOREGROUND_REMOVE)
    }

    private fun createNotification(): Notification {
        val iconId = R.drawable.ic_launcher_foreground
        /* Ejecuta la app luego de dar click en la notificación
           Si la app esta cerrada la vuelve abrir */
        val openActivity = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )
        // Detiene el servicio al dar click en el boton Detener
        val stopService = PendingIntent.getBroadcast(
            this,
            0,
            Intent("ACTION_STOP_SERVICE"),
            PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Builder(this, channelId)
            .setContentTitle(currentMusic.musicName.uppercase())
            .setContentText(currentMusic.artistName.uppercase())
            .setSmallIcon(iconId)
            .setContentIntent(openActivity)
            .addAction(iconId, "Detener", stopService)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private val stopServiceReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            //Detiene el servicio
            stopSelf()
        }
    }

    private val bluetoothReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            //Pausa reproductor de música si se ha desconectado el bluetooth
            when(intent?.action){
                BluetoothDevice.ACTION_ACL_DISCONNECTED -> {
                    if (mediaPlayer.isPlaying) {
                        mediaPlayer.pause()
                        uiState.update { currentState ->
                            currentState.copy(uiIsPause = true)
                        }
                    }
                }
            }
        }
    }
}