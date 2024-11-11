package com.example.playermusic.ui.viewModel

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playermusic.service.MusicService
import com.example.playermusic.data.ObjectsManager
import com.example.playermusic.ui.model.MusicListModel
import com.example.playermusic.ui.model.MusicModel
import com.example.playermusic.ui.model.RepeatOptions
import com.example.playermusic.ui.model.UiPlayerMusicModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PlayerMusicViewModel: ViewModel(){
    //Estados mutables del ViewModel
    private val _uiState by lazy{ MutableStateFlow(UiPlayerMusicModel()) }
    //Estados de solo lectura del ViewModel
    val uiState: StateFlow<UiPlayerMusicModel> = _uiState.asStateFlow()
    //Inicializa DatabaseProcess
    private val dbProcess by lazy{ DatabaseProcess() }
    //Inicializa reproductor de multimedia
    private val mp by lazy{ ObjectsManager.getMediaPlayer() }

    /** Actualiza estado de lista de artistas **/
    fun setUiArtistList(valueList: List<MusicListModel>){
        _uiState.update { currentState ->
            currentState.copy(uiArtistList = valueList)
        }
    }
    /** Actualiza estado de lista de música actual **/
    fun setUiMusicList(valueList: List<MusicModel>){
        _uiState.update { currentState ->
            currentState.copy(uiMusicList = valueList)
        }
    }
    /** Actualiza estado de pausa **/
    fun setUiIsPause(value: Boolean){
        _uiState.update { currentState ->
            currentState.copy(uiIsPause = value)
        }
    }
    /** Actualiza estado de aleatorio **/
    fun setUiIsShuffle(value: Boolean){
        _uiState.update { currentState ->
            currentState.copy(uiIsShuffle = value)
        }
    }
    /** Actualiza estado si es playlist **/
    fun setUiIsPlayList(value: Boolean){
        _uiState.update { currentState ->
            currentState.copy(uiIsPlayList = value)
        }
    }
    /** Actualiza estado de música completada **/
    fun setUiIsCompletion(value: Boolean){
        _uiState.update { currentState ->
            currentState.copy(uiIsCompletion = value)
        }
    }
    /** Actualiza estado de repetir **/
    fun setUiRepeat(value: Int){
        val v = when(value){
            0 -> { RepeatOptions.Current }
            1 -> { RepeatOptions.All }
            else -> { RepeatOptions.Off }
        }
        _uiState.update { currentState ->
            currentState.copy(uiRepeat = v)
        }
    }
    /** Actualiza estado de reiniciar app **/
    fun setUiIsRestartApp(value: Boolean){
        _uiState.update { currentState ->
            currentState.copy(uiIsRestartApp = value)
        }
    }
    /** Actualiza estado de indice de artista actual **/
    fun setUiCurrentArtistListIndex(value: Int){
        val list = uiState.value.uiArtistList
        if(list.isNotEmpty()){
            val v = if(value == -1){ list.lastIndex }
            else{ value % list.size }
            _uiState.update { currentState ->
                currentState.copy(uiCurrentArtistListIndex = v)
            }
        }
    }
    /** Actualiza estado de indice de lista de reproducción actual **/
    fun setUiCurrentPlayListIndex(value: Int){
        val list = dbProcess.playListFlowToList()
        if(list.isNotEmpty()){
            val v = if(value == -1){ list.lastIndex }
            else{ value % list.size }
            _uiState.update { currentState ->
                currentState.copy(uiCurrentPlayListIndex = v)
            }
        }
    }
    /** Actualiza estado de indice de música actual **/
    fun setUiCurrentMusicListIndex(value: Int){
        val list = uiState.value.uiMusicList
        if(list.isNotEmpty()){
            val v = if(value == -1){ list.lastIndex }
            else{ value % list.size }
            _uiState.update { currentState ->
                currentState.copy(uiCurrentMusicListIndex = v)
            }
        }
    }
    /** Actualiza estado de indice de lista de indices aleatorios **/
    fun setUiCountIndex(value: Int){
        val list = uiState.value.uiMusicList
        val v = if(value == -1){ list.lastIndex }
        else{ value % list.size }
        _uiState.update { currentState ->
            currentState.copy(uiCountIndex = v)
        }
    }
    /** Actualiza estado manual de la duración de la música **/
    fun setUiManualDurationValue(value: Int){
        val valueInMilliseconds = value*1000
        mp.seekTo(valueInMilliseconds)
        _uiState.update { currentState ->
            currentState.copy(uiCurrentDuration = valueInMilliseconds)
        }
    }
    /** Actualiza estado automático de la duración de la música **/
    fun setUiAutoDurationValue(){
        viewModelScope.launch(Dispatchers.IO){
            val list = uiState.value.uiMusicList
            val totalDurationSec = list[uiState.value.uiCurrentMusicListIndex].musicDuration/1000
            val currentDurationSec = uiState.value.uiCurrentDuration/1000
            val condition = currentDurationSec < totalDurationSec
            while(condition){
                _uiState.update { currentState ->
                    currentState.copy(uiCurrentDuration = mp.currentPosition)
                }
                delay(1000)
            }
        }
    }

    /** Inicia servicio de notificación de la reproducción de música **/
    fun startServiceMusicPlayer(applicationContext: Context){
        val list = uiState.value.uiMusicList
        val intent = Intent(applicationContext, MusicService::class.java).apply {
            putExtra("musicName", list[uiState.value.uiCurrentMusicListIndex].musicName)
            putExtra("artistName", list[uiState.value.uiCurrentMusicListIndex].artistName)
        }
        ContextCompat.startForegroundService(applicationContext, intent)
    }
}