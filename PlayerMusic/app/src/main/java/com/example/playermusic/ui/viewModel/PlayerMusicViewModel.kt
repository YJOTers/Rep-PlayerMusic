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
import com.example.playermusic.service.MusicService
import com.example.playermusic.R
import com.example.playermusic.data.MediaPlayerManager
import com.example.playermusic.ui.model.MusicListModel
import com.example.playermusic.ui.model.MusicModel
import com.example.playermusic.ui.model.RepeatOptions
import com.example.playermusic.ui.model.UiPlayerMusicModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PlayerMusicViewModel: ViewModel(){
    //Estados mutables del ViewModel
    private val _uiState by lazy{ MediaPlayerManager.getUiState() }
    //Estados de solo lectura del ViewModel
    val uiState: StateFlow<UiPlayerMusicModel> = _uiState.asStateFlow()
    //Inicializa reproductor de multimedia
    private val mp by lazy{ MediaPlayerManager.getMediaPlayer() }
    //Base de datos para guardar las playlist
    private val db by lazy{ MediaPlayerManager.getBD().playListDao() }

    /** Actualiza estado de lista de artistas **/
    private fun setUiArtistList(valueList: List<MusicListModel>){
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
    /** Actualiza estado de lista de reproducción **/
    private fun setUiPlayList(valueList: List<MusicListModel>){
        _uiState.update { currentState ->
            currentState.copy(uiPlayList = valueList)
        }
    }
    /** Actualiza estado de música en lista de reproducción actual **/
    fun setUiPlayListMusic(valueList: List<MusicModel>){
        _uiState.update { currentState ->
            currentState.copy(uiPlayListMusic = valueList)
        }
    }
    /** Actualiza estado de pausa **/
    private fun setUiIsPause(value: Boolean){
        _uiState.update { currentState ->
            currentState.copy(uiIsPause = value)
        }
    }
    /** Actualiza estado de aleatorio **/
    private fun setUiIsShuffle(value: Boolean){
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
    /** Actualiza estado si se completó la música **/
    fun setUiIsCompletion(value: Boolean){
        _uiState.update { currentState ->
            currentState.copy(uiIsCompletion = value)
        }
    }
    /** Actualiza estado de repetir **/
    private fun setUiRepeat(value: Int){
        val v = when(value){
            0 -> { RepeatOptions.Current }
            1 -> { RepeatOptions.All }
            else -> { RepeatOptions.Off }
        }
        _uiState.update { currentState ->
            currentState.copy(uiRepeat = v)
        }
    }
    /** Actualiza estado de indice de lista de indices aleatorios **/
    private fun setUiCountIndex(value: Int){
        val list = if(uiState.value.uiIsPlayList) uiState.value.uiPlayListMusic
                   else uiState.value.uiMusicList
        val v = if(value == -1){ list.lastIndex }
                else{ value % list.size }
        _uiState.update { currentState ->
            currentState.copy(uiCountIndex = v)
        }
    }
    /** Actualiza estado de indice de artista actual **/
    fun setUiCurrentArtistListIndex(value: Int){
        val list = uiState.value.uiArtistList
        val v = if(value == -1){ list.lastIndex }
                else{ value % list.size }
        _uiState.update { currentState ->
            currentState.copy(uiCurrentArtistListIndex = v)
        }
    }
    /** Actualiza estado de indice de lista de reproducción actual **/
    fun setUiCurrentPlayListIndex(value: Int){
        val list = uiState.value.uiPlayList
        val v = if(value == -1){ list.lastIndex }
                else{ value % list.size }
        _uiState.update { currentState ->
            currentState.copy(uiCurrentPlayListIndex = v)
        }
    }
    /** Actualiza estado de indice de música actual **/
    fun setUiCurrentMusicListIndex(value: Int){
        val list = if(uiState.value.uiIsPlayList) uiState.value.uiPlayListMusic
                   else uiState.value.uiMusicList
        val v = if(value == -1){ list.lastIndex }
                else{ value % list.size }
        _uiState.update { currentState ->
            currentState.copy(uiCurrentMusicListIndex = v)
        }
        refreshMusic()
    }
    /** Actualiza estado manual de la duración de la música **/
    fun setUiManualDurationValue(value: Long){
        mp.seekTo(value.toInt()*1000)
        _uiState.update { currentState ->
            currentState.copy(uiCurrentDuration = value*1000)
        }
    }
    /** Actualiza estado automático de la duración de la música **/
    private fun setUiAutoDurationValue(){
        viewModelScope.launch(Dispatchers.IO){
            val list = if(uiState.value.uiIsPlayList) uiState.value.uiPlayListMusic
                       else uiState.value.uiMusicList
            val totalDurationSec = list[uiState.value.uiCurrentMusicListIndex].musicDuration/1000
            val currentDurationSec = uiState.value.uiCurrentDuration/1000
            val condition = currentDurationSec < totalDurationSec
            while(condition){
                _uiState.update { currentState ->
                    currentState.copy(uiCurrentDuration = mp.currentPosition.toLong())
                }
                delay(1000)
            }
        }
    }
    /** Convierte duración de Long a String formato minutos:segundos **/
    fun durationFormat(duration: Long) : String {
        var seconds = duration/1000
        val minutes = seconds/60
        seconds %= 60
        return if (seconds < 10) "$minutes:0$seconds"
               else "$minutes:$seconds"
    }
    /** Actualiza el artista y la música en la notificación **/
    fun refreshNotification(){
        val list = if(uiState.value.uiIsPlayList) uiState.value.uiPlayListMusic
                   else uiState.value.uiMusicList
        MediaPlayerManager.setCurrentMusic(
            musicName = list[uiState.value.uiCurrentMusicListIndex].musicName,
            artistName = list[uiState.value.uiCurrentMusicListIndex].artistName
        )
    }
    /** Establece música actual en el MediaPlayer **/
    private fun refreshMusic(){
        val list = if(uiState.value.uiIsPlayList) uiState.value.uiPlayListMusic
                   else uiState.value.uiMusicList
        mp.reset()
        mp.setDataSource(list[uiState.value.uiCurrentMusicListIndex].musicPath)
        mp.prepare()
        setUiManualDurationValue(0)
        setUiIsPause(true)
    }
    /** Reproduce automaticamente la música **/
    private fun musicClicked(){
        viewModelScope.launch(Dispatchers.IO){
            val indexRandom = if(uiState.value.uiIsPlayList) (0..uiState.value.uiPlayListMusic.lastIndex).toList().shuffled()
                              else (0..uiState.value.uiMusicList.lastIndex).toList().shuffled()
            mp.setOnCompletionListener {
                setUiIsCompletion(true)
                if(uiState.value.uiRepeat == RepeatOptions.Current && !uiState.value.uiIsShuffle){
                    //Si repetir la misma música esta activa
                    setUiCurrentMusicListIndex(uiState.value.uiCurrentMusicListIndex)
                    playClicked()
                }else if(uiState.value.uiIsShuffle && uiState.value.uiRepeat == RepeatOptions.Current){
                    //Si ambos estan activos caso 1
                    setUiCurrentMusicListIndex(uiState.value.uiCurrentMusicListIndex)
                    playClicked()
                }else{
                    if (uiState.value.uiIsShuffle && uiState.value.uiRepeat == RepeatOptions.All) {
                        //Si ambos estan activos caso 2
                        setUiCurrentMusicListIndex(indexRandom[uiState.value.uiCountIndex])
                        playClicked()
                        setUiCountIndex(uiState.value.uiCountIndex + 1)
                    } else if (uiState.value.uiIsShuffle && uiState.value.uiRepeat == RepeatOptions.Off) {
                        //Si aleatorio esta activo
                        setUiCurrentMusicListIndex(indexRandom[uiState.value.uiCountIndex])
                        playClicked()
                        setUiCountIndex(uiState.value.uiCountIndex + 1)
                    } else if (uiState.value.uiRepeat == RepeatOptions.All && !uiState.value.uiIsShuffle) {
                        //Si repetir toda la lista esta activa
                        setUiCurrentMusicListIndex(uiState.value.uiCurrentMusicListIndex + 1)
                        playClicked()
                    } else setUiIsPause(true) //Si ninguno esta activo
                }
            }
        }
    }
    /** Reproduce o pausa la música actual **/
    fun playClicked(){
        musicClicked()
        if (mp.isPlaying) {
            mp.pause()
            setUiIsPause(true)
        } else {
            mp.start()
            setUiIsPause(false)
        }
        setUiAutoDurationValue()
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
        viewModelScope.launch(Dispatchers.IO){
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
            )?.use { cursor ->
                //Obtiene la información de los audios del dispositivo móvil
                val musicPathColumn = cursor.getColumnIndex(MediaStore.Audio.Media.DATA)
                val musicDurationColumn = cursor.getColumnIndex(MediaStore.Audio.Media.DURATION)
                val musicNameColumn = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)
                val artistNameColumn = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)
                val albumNameColumn = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM)
                val albumIdColumn = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)
                while (cursor.moveToNext()) {
                    val thisMusicPath = cursor.getString(musicPathColumn)
                    val thisMusicDuration = cursor.getLong(musicDurationColumn)
                    val thisMusicName = cursor.getString(musicNameColumn)
                    val thisArtistName = cursor.getString(artistNameColumn)
                    val thisAlbumName = cursor.getString(albumNameColumn)
                    val thisAlbumId = cursor.getLong(albumIdColumn)
                    val albumUri = ContentUris.withAppendedId(subUri, thisAlbumId).toString()
                    val index = artistList.indexOfLast { it.name == thisArtistName }
                    if (index == -1) {
                        //Si no hay el artista indicado
                        val musicList = listOf(
                            MusicModel(
                                musicPath = thisMusicPath,
                                musicDuration = thisMusicDuration,
                                musicDurationFormat = durationFormat(thisMusicDuration),
                                musicName = thisMusicName,
                                artistName = thisArtistName,
                                albumName = thisAlbumName,
                                albumUri = albumUri
                            )
                        )
                        artistList.add(
                            MusicListModel(
                                name = thisArtistName,
                                musicList = musicList
                            )
                        )
                    } else {
                        //Si hay el artista indicado
                        val musicList = artistList[index].musicList.toMutableList()
                        musicList.add(
                            MusicModel(
                                musicPath = thisMusicPath,
                                musicDuration = thisMusicDuration,
                                musicDurationFormat = durationFormat(thisMusicDuration),
                                musicName = thisMusicName,
                                artistName = thisArtistName,
                                albumName = thisAlbumName,
                                albumUri = albumUri
                            )
                        )
                        artistList[index] = MusicListModel(
                            name = thisArtistName,
                            musicList = musicList
                        )
                    }
                }
            }
            withContext(Dispatchers.Main){
                setUiArtistList(artistList)
            }
        }
    }
    /** Obtiene las listas de reproducción guardadas en la BD local **/
    fun getPlayList(){
        viewModelScope.launch(Dispatchers.IO){
            val data = db.getPlayList()
            withContext(Dispatchers.Main){
                setUiPlayList(data)
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
    /** Guarda datos persistentes para botones aleatorio y repetir **/
    fun saveConfig(
        applicationContext: Context,
        valueRepeat: Int? = null,
        valueShuffle: Boolean? = null
    ){
        val sp = applicationContext.getSharedPreferences("MyConfig", Context.MODE_PRIVATE)
        sp.edit().apply {
            if(valueRepeat != null) putInt("repeat", valueRepeat)
            if(valueShuffle != null) putBoolean("shuffle", valueShuffle)
            apply()
        }
    }
    /** Obtiene datos persistentes de botones aleatorio y repetir **/
    fun getConfig(applicationContext: Context){
        val sp = applicationContext.getSharedPreferences("MyConfig", Context.MODE_PRIVATE)
        sp.apply {
            setUiRepeat(getInt("repeat", 2))
            setUiIsShuffle(getBoolean("shuffle", false))
        }
    }
}