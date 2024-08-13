package com.example.playermusic.ui.viewModel

import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playermusic.MainApplication
import com.example.playermusic.service.MusicService
import com.example.playermusic.R
import com.example.playermusic.data.MediaPlayerManager
import com.example.playermusic.ui.model.MusicListModel
import com.example.playermusic.ui.model.MusicModel
import com.example.playermusic.ui.model.UiPlayerMusicModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PlayerMusicViewModel: ViewModel(){
    //Estados mutables del ViewModel
    private val _uiState = MutableStateFlow(UiPlayerMusicModel())
    //Estados de solo lectura del ViewModel
    val uiState: StateFlow<UiPlayerMusicModel> = _uiState.asStateFlow()
    //Inicializa reproductor de multimedia
    private val mp by lazy{ MediaPlayerManager.getMediaPlayer() }
    //Base de datos para guardar las playlist
    private val db by lazy{ MainApplication.database.playListDao() }

    override fun onCleared() {
        super.onCleared()
        MediaPlayerManager.releaseMediaPlayer()
        resetPlayerMusic()
        MainApplication.database.close()
    }
    /** Actualiza estado de lista de artistas **/
    private fun setUiArtistList(valueList: List<MusicListModel>){
        _uiState.update { currentState ->
            currentState.copy(uiArtistList = valueList)
        }
    }
    /** Actualiza estado de lista de música **/
    fun setUiMusicList(value: MusicListModel){
        val list1 = uiState.value.uiArtistList
        val index = list1.indexOf(value)
        setUiCurrentArtistIndex(index)
        _uiState.update { currentState ->
            currentState.copy(uiMusicList = value.musicList)
        }
    }
    /** Actualiza estado de lista de reproducción **/
    private fun setUiPlayList(valueList: List<MusicListModel>){
        _uiState.update { currentState ->
            currentState.copy(uiPlayList = valueList)
        }
    }
    /** Actualiza estado de música en lista de reproducción **/
    fun setUiPlayListMusic(value: MusicListModel){
        val list2 = uiState.value.uiPlayList
        val index = list2.indexOf(value)
        setUiCurrentPlayListIndex(index)
        _uiState.update { currentState ->
            currentState.copy(uiPlayListMusic = value.musicList)
        }
    }
    /** Actualiza estado de pausa **/
    private fun setUiIsPause(value: Boolean){
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
    /** Actualiza estado de repetir **/
    fun setUiIsRepeat(value: Boolean){
        _uiState.update { currentState ->
            currentState.copy(uiIsRepeat = value)
        }
    }
    /** Actualiza estado de indice de lista de indices aleatorios **/
    private fun setUiCountIndex(value: Int, isPlayList: Boolean){
        val list = if(isPlayList) uiState.value.uiPlayListMusic
        else uiState.value.uiMusicList
        val v = if(value == -1){ list.lastIndex }
        else{ value % list.size }
        _uiState.update { currentState ->
            currentState.copy(uiCountIndex = v)
        }
    }
    /** Actualiza estado de indice de artista actual **/
    private fun setUiCurrentArtistIndex(value: Int){
        val list = uiState.value.uiArtistList
        val v = if(value == -1){ list.lastIndex }
        else{ value % list.size }
        _uiState.update { currentState ->
            currentState.copy(uiCurrentArtistIndex = v)
        }
    }
    /** Actualiza estado de indice de lista de reproducción actual **/
    private fun setUiCurrentPlayListIndex(value: Int){
        val list = uiState.value.uiPlayList
        val v = if(value == -1){ list.lastIndex }
        else{ value % list.size }
        _uiState.update { currentState ->
            currentState.copy(uiCurrentPlayListIndex = v)
        }
    }
    /** Actualiza estado de indice de música actual **/
    private fun setUiCurrentMusicIndex(value: Int, isPlayList: Boolean){
        val list = if(isPlayList) uiState.value.uiPlayListMusic
        else uiState.value.uiMusicList
        val v = if(value == -1){ list.lastIndex }
        else{ value % list.size }
        _uiState.update { currentState ->
            currentState.copy(uiCurrentMusicIndex = v)
        }
    }
    /** Actualiza estado manual de la duración de la música **/
    fun setUiManualDurationValue(value: Long){
        mp.seekTo(value.toInt()*1000)
        _uiState.update { currentState ->
            currentState.copy(uiCurrentDuration = value*1000)
        }
    }
    /** Actualiza estado automático de la duración de la música **/
    private fun setUiAutoDurationValue(isPlayList: Boolean){
        viewModelScope.launch(Dispatchers.IO){
            //Actualiza estado automático
            val index = uiState.value.uiCurrentMusicIndex
            val list = if(isPlayList) uiState.value.uiPlayListMusic
            else uiState.value.uiMusicList
            val totalDurationSec = list[index].musicDuration/1000
            val currentDurationSec = uiState.value.uiCurrentDuration/1000
            val condition = currentDurationSec < totalDurationSec
            while(condition){
                _uiState.update { currentState ->
                    currentState.copy(uiCurrentDuration = mp.currentPosition.toLong())
                }
                delay(1000)
            }
            //Inicializa lista de indices aleatorios no repetibles
            val indexRandom = if(isPlayList) (0..uiState.value.uiPlayListMusic.lastIndex).toList().shuffled()
            else (0..uiState.value.uiMusicList.lastIndex).toList().shuffled()
            mp.setOnCompletionListener{
                if (uiState.value.uiIsShuffle && uiState.value.uiIsRepeat){
                    //Si ambos estan activos
                    musicClicked(indexRandom[uiState.value.uiCountIndex], isPlayList)
                    playClicked(isPlayList)
                    setUiCountIndex(uiState.value.uiCountIndex + 1, isPlayList)
                }else if(uiState.value.uiIsShuffle && !uiState.value.uiIsRepeat){
                    //Si aleatorio esta activo
                    musicClicked(indexRandom[uiState.value.uiCountIndex], isPlayList)
                    playClicked(isPlayList)
                    setUiCountIndex(uiState.value.uiCountIndex + 1, isPlayList)
                }else if(uiState.value.uiIsRepeat && !uiState.value.uiIsShuffle){
                    //Si repetir esta activo
                    musicClicked(uiState.value.uiCurrentMusicIndex + 1, isPlayList)
                    playClicked(isPlayList)
                }
                else setUiIsPause(true) //Si ninguno esta activo
            }
        }
    }
    /** Actualiza estado del nombre de la lista de reproducción **/
    fun setUiPlayListName(value: String){
        _uiState.update{ currentState ->
            currentState.copy(uiPlayListName = value)
        }
    }
    /** Actualiza estado de la música actual **/
    fun setUiSelectedMusic(value: MusicModel){
        _uiState.update{ currentState ->
            currentState.copy(uiSelectedMusic = value)
        }
    }
    /** Actualiza estado del filtro de busqueda de listas de reproducción **/
    fun setUiFilter(value: List<MusicListModel>){
        _uiState.update{ currentState ->
            currentState.copy(uiFilter = value)
        }
    }
    /** Resetea estados del modelo **/
    private fun resetPlayerMusic(){
        _uiState.value = UiPlayerMusicModel()
    }
    /** Convierte duración de Long a String formato minutos:segundos **/
    fun durationFormat(duration: Long) : String {
        var seconds = duration/1000
        val minutes = seconds/60
        seconds %= 60
        return if (seconds < 10) "$minutes:0$seconds"
        else "$minutes:$seconds"
    }
    /** Establece música actual en el MediaPlayer **/
    private fun refreshMusic(isPlayList: Boolean){
        val index = uiState.value.uiCurrentMusicIndex
        val list = if(isPlayList) uiState.value.uiPlayListMusic
        else uiState.value.uiMusicList
        MediaPlayerManager.setCurrentMusic(
            musicName = list[index].musicName,
            artistName = list[index].artistName
        )
        mp.reset()
        mp.setDataSource(list[index].musicPath)
        mp.prepare()
    }
    /** Establece música actual en la Interfaz de usuario **/
    fun musicClicked(index: Int, isPlayList: Boolean){
        setUiCurrentMusicIndex(index, isPlayList)
        setUiManualDurationValue(0)
        setUiIsPause(true)
        refreshMusic(isPlayList)
    }
    /** Reproduce o pausa la música actual **/
    fun playClicked(isPlayList: Boolean){
        if (mp.isPlaying) {
            mp.pause()
            setUiIsPause(true)
        } else {
            mp.start()
            setUiIsPause(false)
        }
        setUiAutoDurationValue(isPlayList)
    }
    /** Inicia servicio de notificación de la reproducción de música **/
    fun startServiceMusicPlayer(applicationContext: Context){
        val intent = Intent(applicationContext, MusicService::class.java)
        ContextCompat.startForegroundService(applicationContext, intent)
    }
    /** Obtiene imagen del album o deja imagen por defecto **/
    fun getAlbumUri(
        applicationContext: Context,
        albumImageUri: String
    ) : String{
        val albumArtExists = try{
            applicationContext.contentResolver.openInputStream(albumImageUri.toUri())?.close()
            true
        }
        catch (e: Exception) { false }
        return if(albumArtExists){ albumImageUri }
        else{
            Uri.parse("android.resource://" +
                    "${applicationContext.packageName}/" +
                    "${R.drawable.album_48}").toString()
        }
    }
    /** Obtiene música del dispositivo móvil del usuario **/
    fun getArtistList(applicationContext: Context){
        //Realiza la consulta en un hilo de fondo para optimizar el rendimiento
        val musicUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val subUri = Uri.parse("content://media/external/audio/albumart")
        //Define la información a obtener
        val projection = arrayOf(
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ALBUM_ID
        )
        val sortOrder = "${MediaStore.Audio.Media.TITLE} DESC"
        val artistList = mutableListOf<MusicListModel>()
        applicationContext.contentResolver.query(
            musicUri,
            projection,
            null,
            null,
            sortOrder
        )?.use{ cursor ->
            //Obtiene la información de los audios del dispositivo móvil
            val musicPathColumn = cursor.getColumnIndex(MediaStore.Audio.Media.DATA)
            val musicDurationColumn = cursor.getColumnIndex(MediaStore.Audio.Media.DURATION)
            val musicNameColumn = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)
            val artistNameColumn = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)
            val albumNameColumn = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM)
            val albumIdColumn = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)
            while(cursor.moveToNext()){
                val thisMusicPath = cursor.getString(musicPathColumn)
                val thisMusicDuration = cursor.getLong(musicDurationColumn)
                val thisMusicName = cursor.getString(musicNameColumn)
                val thisArtistName = cursor.getString(artistNameColumn)
                val thisAlbumName = cursor.getString(albumNameColumn)
                val thisAlbumId = cursor.getLong(albumIdColumn)
                val albumUri = ContentUris.withAppendedId(subUri, thisAlbumId).toString()
                val index = artistList.indexOfLast { it.name == thisArtistName }
                if(index == -1){
                    //Si no hay el artista indicado
                    val musicList = listOf(
                        MusicModel(
                            musicPath = thisMusicPath,
                            musicDuration = thisMusicDuration,
                            musicName = thisMusicName,
                            artistName = thisArtistName,
                            albumName = thisAlbumName,
                            albumUri = albumUri
                        ))
                    artistList.add(MusicListModel(
                        name = thisArtistName,
                        musicList = musicList
                    ))
                }else{
                    //Si hay el artista indicado
                    val musicList = artistList[index].musicList.toMutableList()
                    musicList.add(
                        MusicModel(
                            musicPath = thisMusicPath,
                            musicDuration = thisMusicDuration,
                            musicName = thisMusicName,
                            artistName = thisArtistName,
                            albumName = thisAlbumName,
                            albumUri = albumUri
                        ))
                    artistList[index] = MusicListModel(
                        name = thisArtistName,
                        musicList = musicList
                    )
                }
            }
        }
        setUiArtistList(artistList)
    }
    /** Obtiene las listas de reproducción guardadas en la BD local **/
    fun getPlayList(){
        viewModelScope.launch(Dispatchers.IO){
            val data = db.getPlayList()
            withContext(Dispatchers.Main){
                setUiPlayList(data)
                setUiFilter(data)
            }
        }
    }
    /** Crea e inserta una lista de reproducción en la BD local **/
    fun insertPlayList(item: MusicListModel){
        viewModelScope.launch(Dispatchers.IO) {
            db.insertPlayList(item)
            getPlayList()
        }
    }
    /** Actualiza una lista de reproducción en la BD local **/
    fun updatePlayList(item: MusicListModel){
        viewModelScope.launch(Dispatchers.IO) {
            db.updatePlayList(item)
            getPlayList()
        }
    }
    /** Elimina una lista de reproducción en la BD local **/
    fun deletePlayList(item: MusicListModel){
        viewModelScope.launch(Dispatchers.IO) {
            db.deletePlayList(item)
            getPlayList()
        }
    }
}