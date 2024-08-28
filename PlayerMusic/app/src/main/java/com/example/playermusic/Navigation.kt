package com.example.playermusic

import android.widget.Toast
import androidx.annotation.StringRes
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.playermusic.ui.model.MusicListModel
import com.example.playermusic.ui.model.MusicModel
import com.example.playermusic.ui.model.RepeatOptions
import com.example.playermusic.ui.view.AddPlayList
import com.example.playermusic.ui.view.ArtistList
import com.example.playermusic.ui.view.CurrentMusic
import com.example.playermusic.ui.view.CurrentPlayList
import com.example.playermusic.ui.view.MusicList
import com.example.playermusic.ui.view.MyAlertDialog
import com.example.playermusic.ui.view.PlayList
import com.example.playermusic.ui.viewModel.PlayerMusicViewModel

private enum class RouteViews(@StringRes val idTitle: Int){
    ArtistList(idTitle = R.string.app_artistList),
    MusicList(idTitle = R.string.app_musicList),
    CurrentMusic1(idTitle = R.string.app_currentMusic),
    PlayList(idTitle = R.string.app_playList),
    CurrentPlayList(idTitle = R.string.app_musicList),
    CurrentMusic2(idTitle = R.string.app_currentMusic),
    AddPlayList(idTitle = R.string.app_addPlayList);
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ToolBarMenu(
    routeTitles: RouteViews,
    routeArg: String,
    canNavigateBack: Boolean,
    navigateUp: ()-> Unit,
    goPlayList: ()-> Unit
){
    TopAppBar(
        title = {
            Text(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                text = stringResource(routeTitles.idTitle, routeArg),
                style = MaterialTheme.typography.titleLarge,
                maxLines = 1,
                overflow = TextOverflow.Clip
            )
        },
        navigationIcon = {
            if(canNavigateBack){
                IconButton(
                    onClick = navigateUp,
                    modifier = Modifier.size(dimensionResource(id = R.dimen.short_dp_5))
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = null,
                        modifier = Modifier.size(dimensionResource(id = R.dimen.short_dp_4))
                    )
                }
            }
        },
        actions = {
            if(routeTitles.name == RouteViews.ArtistList.name){
                IconButton(
                    onClick = goPlayList,
                    modifier = Modifier.size(dimensionResource(id = R.dimen.short_dp_5))
                ) {
                    Icon(
                        imageVector = ImageVector.vectorResource(R.drawable.playlist_48),
                        contentDescription = null,
                        modifier = Modifier.size(dimensionResource(id = R.dimen.short_dp_5))
                    )
                }
            }
        }
    )
}

@Composable
fun AppScreen(
    vmPlayerMusic: PlayerMusicViewModel,
    navController: NavHostController = rememberNavController()
){
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val uiStatePlayerMusic by vmPlayerMusic.uiState.collectAsState()
    //Vista ToolBarMenu
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentScreen = RouteViews.valueOf(
        backStackEntry?.destination?.route ?: RouteViews.ArtistList.name
    )
    //Indices
    val index0 = uiStatePlayerMusic.uiCurrentArtistListIndex
    val index1 = uiStatePlayerMusic.uiCurrentPlayListIndex
    val index2 = uiStatePlayerMusic.uiCurrentMusicListIndex
    //Vista ArtistList
    val artistList = uiStatePlayerMusic.uiArtistList
    //Vista PlayList
    val playList = uiStatePlayerMusic.uiPlayList
    //Lista de reproduccion
    val musicList = uiStatePlayerMusic.uiMusicList
    //Es playlist o artistlist
    val isPlayList = uiStatePlayerMusic.uiIsPlayList
    //Variables locales
    var playListName by remember{ mutableStateOf("") }
    var selectedMusic by remember{ mutableStateOf(MusicModel()) }
    var selectedItem by remember{ mutableStateOf(MusicModel()) }
    var selectedPlaylist by remember{ mutableStateOf(MusicListModel()) }
    var editPlaylistName by remember{ mutableStateOf(false) }
    var removePlaylist by remember{ mutableStateOf(false) }
    var removeMusic by remember{ mutableStateOf(false) }
    var filter by remember{ mutableStateOf(listOf<MusicListModel>()) }
    filter = playList
    //Texto de AlertDialogs
    val smsTitle = stringResource(R.string.ad_title)
    val smsMessage1 = stringResource(R.string.ad_editNamePlayList)
    val smsAlert1 = stringResource(R.string.toast_editNamePlayList)
    val smsEditName = stringResource(R.string.ad_editName)
    val smsMessage2 = stringResource(R.string.ad_removePlayList)
    val smsAlert2 = stringResource(R.string.toast_removePlayList, playListName)
    val smsMessage3 = stringResource(R.string.ad_deleteMusicPlayList)
    val smsAlert3 = stringResource(R.string.toast_deleteMusicPlayList)
    val smsYes = stringResource(R.string.ad_yes)
    val smsNo = stringResource(R.string.ad_no)
    Scaffold(
        topBar = {
            ToolBarMenu(
                routeTitles = currentScreen,
                routeArg = when(currentScreen){
                                RouteViews.MusicList -> artistList[index0].name
                                RouteViews.CurrentPlayList -> playList[index1].name
                                else -> ""
                           },
                canNavigateBack = navController.previousBackStackEntry != null,
                navigateUp = {
                    //Navega hacia atras
                    navController.navigateUp()
                },
                goPlayList = {
                    //Navega a la lista de playlist
                    navController.navigate(RouteViews.PlayList.name)
                }
            )
        }
    ){ innerPadding ->
        LaunchedEffect(
            key1 = uiStatePlayerMusic.uiIsCompletion,
            block = {
                if (uiStatePlayerMusic.uiIsCompletion) {
                    selectedMusic = if(isPlayList) playList[index1].musicList[index2]
                                    else artistList[index0].musicList[index2]
                    vmPlayerMusic.setUiIsCompletion(false)
                    vmPlayerMusic.refreshNotification()
                    vmPlayerMusic.startServiceMusicPlayer(context.applicationContext)
                }
            }
        )
        NavHost(
            navController = navController,
            startDestination = RouteViews.ArtistList.name,
            modifier = Modifier.padding(innerPadding))
        {
            composable(route = RouteViews.ArtistList.name){
                vmPlayerMusic.setUiIsPlayList(false)
                ArtistList(
                    modifier = Modifier.fillMaxSize(),
                    artistList = artistList,
                    itemClicked = { itemArtist ->
                        val index = artistList.indexOf(itemArtist)
                        vmPlayerMusic.setUiCurrentArtistListIndex(index)
                        //Navega a la lista de música del artista seleccionado
                        navController.navigate(RouteViews.MusicList.name)
                    }
                )
            }
            composable(route = RouteViews.MusicList.name){
                MusicList(
                    modifier = Modifier.fillMaxSize(),
                    musicList = artistList[index0].musicList,
                    itemPlaying = selectedMusic,
                    itemClicked = { itemMusic ->
                        val index = artistList[index0].musicList.indexOf(itemMusic)
                        if(itemMusic != selectedMusic){
                            vmPlayerMusic.setUiMusicList(artistList[index0].musicList)
                            vmPlayerMusic.setUiCurrentMusicListIndex(index)
                        }else{
                            if(artistList[index0].musicList != musicList){
                                vmPlayerMusic.setUiMusicList(artistList[index0].musicList)
                                vmPlayerMusic.setUiCurrentMusicListIndex(index)
                            }
                        }
                        //Navega a la música seleccionada
                        navController.navigate(RouteViews.CurrentMusic1.name)
                    },
                    addPlayListClicked = { itemMusic ->
                        selectedItem = itemMusic
                        navController.navigate(RouteViews.AddPlayList.name)
                    }
                )
            }
            composable(route = RouteViews.CurrentMusic1.name){
                CurrentMusic(
                    modifier = Modifier.fillMaxSize(),
                    artistName = artistList[index0].musicList[index2].artistName,
                    musicName = artistList[index0].musicList[index2].musicName,
                    albumName = artistList[index0].musicList[index2].albumName,
                    albumUri = vmPlayerMusic.getAlbumUri(
                        context.applicationContext,
                        artistList[index0].musicList[index2].albumUri
                    ),
                    clickedPlayList = {
                        //Navega a la lista de playlist
                        navController.navigate(RouteViews.PlayList.name)
                    },
                    clickedShuffle = {
                        if(!uiStatePlayerMusic.uiIsShuffle){
                            vmPlayerMusic.saveConfig(context.applicationContext, isShuffle = true)
                            Toast.makeText(context, context.getString(R.string.toast_shuffle1), Toast.LENGTH_SHORT).show()
                        }else{
                            vmPlayerMusic.saveConfig(context.applicationContext, isShuffle = false)
                            Toast.makeText(context, context.getString(R.string.toast_shuffle2), Toast.LENGTH_SHORT).show()
                        }
                    },
                    clickedRepeat = {
                        when(uiStatePlayerMusic.uiRepeat){
                            RepeatOptions.Current -> {
                                vmPlayerMusic.saveConfig(context.applicationContext, valueRepeat = 1)
                                Toast.makeText(context, context.getString(R.string.toast_repeat1), Toast.LENGTH_SHORT).show()
                            }
                            RepeatOptions.All -> {
                                vmPlayerMusic.saveConfig(context.applicationContext, valueRepeat = 2)
                                Toast.makeText(context, context.getString(R.string.toast_repeat2), Toast.LENGTH_SHORT).show()
                            }
                            RepeatOptions.Off -> {
                                vmPlayerMusic.saveConfig(context.applicationContext, valueRepeat = 0)
                                Toast.makeText(context, context.getString(R.string.toast_repeat3), Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    clickedPrevious = {
                        vmPlayerMusic.setUiCurrentMusicListIndex(index2 - 1)
                        vmPlayerMusic.refreshNotification()
                        vmPlayerMusic.startServiceMusicPlayer(context.applicationContext)
                    },
                    clickedPlay = {
                        selectedMusic = artistList[index0].musicList[index2]
                        vmPlayerMusic.playClicked()
                        vmPlayerMusic.refreshNotification()
                        vmPlayerMusic.startServiceMusicPlayer(context.applicationContext)
                    },
                    clickedNext = {
                        vmPlayerMusic.setUiCurrentMusicListIndex(index2 + 1)
                        vmPlayerMusic.refreshNotification()
                        vmPlayerMusic.startServiceMusicPlayer(context.applicationContext)
                    },
                    isPause = uiStatePlayerMusic.uiIsPause,
                    isShuffle = uiStatePlayerMusic.uiIsShuffle,
                    isRepeat = uiStatePlayerMusic.uiRepeat,
                    duration = artistList[index0].musicList[index2].musicDurationFormat,
                    currentDuration = vmPlayerMusic.durationFormat(uiStatePlayerMusic.uiCurrentDuration),
                    durationValue = (uiStatePlayerMusic.uiCurrentDuration/1000).toFloat(),
                    onDurationChange = { vmPlayerMusic.setUiManualDurationValue(it.toLong()) },
                    valueRange = 0f..(artistList[index0].musicList[index2].musicDuration/1000).toFloat(),
                    steps = artistList[index0].musicList[index2].musicDuration.toInt()/1000
                )
            }
            composable(route = RouteViews.PlayList.name){
                vmPlayerMusic.setUiIsPlayList(true)
                //Aviso para editar el nombre de playlist seleccionada
                if(editPlaylistName){
                    MyAlertDialog(
                        confirm = smsYes,
                        dismiss = smsNo,
                        title = smsTitle,
                        message = smsMessage1,
                        confirmClicked = {
                            val item = MusicListModel(
                                id = playList[index1].id,
                                name = playListName,
                                musicList = playList[index1].musicList
                            )
                            vmPlayerMusic.updatePlayList(item)
                            Toast.makeText(context, smsAlert1, Toast.LENGTH_SHORT).show()
                            editPlaylistName = false
                            playListName = ""
                        },
                        dismissClicked = {
                            editPlaylistName = false
                            playListName = ""
                        },
                        case = 2,
                        value = playListName,
                        label = smsEditName,
                        onValue = { name -> playListName = name }
                    )
                }
                //Aviso para eliminar la playlist seleccionada
                if(removePlaylist){
                    MyAlertDialog(
                        confirm = smsYes,
                        dismiss = smsNo,
                        title = smsTitle,
                        message = smsMessage2,
                        confirmClicked = {
                            vmPlayerMusic.deletePlayList(selectedPlaylist)
                            Toast.makeText(context, smsAlert2, Toast.LENGTH_SHORT).show()
                            removePlaylist = false
                            playListName = ""
                            selectedPlaylist = MusicListModel()
                        },
                        dismissClicked = {
                            removePlaylist = false
                            playListName = ""
                            selectedPlaylist = MusicListModel()
                        },
                    )
                }
                PlayList(
                    modifier = Modifier.fillMaxSize(),
                    playList = playList,
                    itemClicked = { itemPlayList ->
                        val index = playList.indexOf(itemPlayList)
                        vmPlayerMusic.setUiCurrentPlayListIndex(index)
                        //Navega a la lista de música del playlist seleccionado
                        navController.navigate(RouteViews.CurrentPlayList.name)
                    },
                    editNamePlayListClicked = {
                        editPlaylistName = true
                    },
                    removePlayListClicked = { itemPlayList ->
                        removePlaylist = true
                        playListName = itemPlayList.name
                        selectedPlaylist = itemPlayList
                    }
                )
            }
            composable(route = RouteViews.CurrentPlayList.name){
                //Aviso para eliminar una música de la playlist seleccionada
                if(removeMusic){
                    MyAlertDialog(
                        confirm = smsYes,
                        dismiss = smsNo,
                        title = smsTitle,
                        message = smsMessage3,
                        confirmClicked = {
                            val list = playList[index1].musicList.toMutableList()
                            list.remove(selectedItem)
                            val item = MusicListModel(
                                id = playList[index1].id,
                                name = playList[index1].name,
                                musicList = list
                            )
                            vmPlayerMusic.updatePlayList(item)
                            vmPlayerMusic.setUiMusicList(playList[index1].musicList)
                            Toast.makeText(context, smsAlert3, Toast.LENGTH_SHORT).show()
                            removeMusic = false
                            selectedItem = MusicModel()
                        },
                        dismissClicked = {
                            removeMusic = false
                            selectedItem = MusicModel()
                        },
                    )
                }
                CurrentPlayList(
                    modifier = Modifier.fillMaxSize(),
                    playListMusic = playList[index1].musicList,
                    itemPlaying = selectedMusic,
                    itemClicked = { itemPlayListMusic ->
                        val index = playList[index1].musicList.indexOf(itemPlayListMusic)
                        if(itemPlayListMusic != selectedMusic) {
                            vmPlayerMusic.setUiMusicList(playList[index1].musicList)
                            vmPlayerMusic.setUiCurrentMusicListIndex(index)
                        }else{
                            if(playList[index1].musicList != musicList){
                                vmPlayerMusic.setUiMusicList(playList[index1].musicList)
                                vmPlayerMusic.setUiCurrentMusicListIndex(index)
                            }
                        }
                        //Navega a la música seleccionada
                        navController.navigate(RouteViews.CurrentMusic2.name)
                    },
                    removeMusicClicked = { itemPlayListMusic ->
                        removeMusic = true
                        selectedItem = itemPlayListMusic
                    }
                )
            }
            composable(route = RouteViews.CurrentMusic2.name){
                CurrentMusic(
                    modifier = Modifier.fillMaxSize(),
                    artistName = playList[index1].musicList[index2].artistName,
                    musicName = playList[index1].musicList[index2].musicName,
                    albumName = playList[index1].musicList[index2].albumName,
                    albumUri = vmPlayerMusic.getAlbumUri(
                        context.applicationContext,
                        playList[index1].musicList[index2].albumUri
                    ),
                    clickedPlayList = {
                        //Navega al inicio ArtistView
                        navController.popBackStack(RouteViews.ArtistList.name, false)
                    },
                    clickedShuffle = {
                        if(!uiStatePlayerMusic.uiIsShuffle){
                            vmPlayerMusic.saveConfig(context.applicationContext, isShuffle = true)
                            Toast.makeText(context, context.getString(R.string.toast_shuffle1), Toast.LENGTH_SHORT).show()
                        }else{
                            vmPlayerMusic.saveConfig(context.applicationContext, isShuffle = false)
                            Toast.makeText(context, context.getString(R.string.toast_shuffle2), Toast.LENGTH_SHORT).show()
                        }
                    },
                    clickedRepeat = {
                        when(uiStatePlayerMusic.uiRepeat){
                            RepeatOptions.Current -> {
                                vmPlayerMusic.saveConfig(context.applicationContext, valueRepeat = 1)
                                Toast.makeText(context, context.getString(R.string.toast_repeat1), Toast.LENGTH_SHORT).show()
                            }
                            RepeatOptions.All -> {
                                vmPlayerMusic.saveConfig(context.applicationContext, valueRepeat = 2)
                                Toast.makeText(context, context.getString(R.string.toast_repeat2), Toast.LENGTH_SHORT).show()
                            }
                            RepeatOptions.Off -> {
                                vmPlayerMusic.saveConfig(context.applicationContext, valueRepeat = 0)
                                Toast.makeText(context, context.getString(R.string.toast_repeat3), Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    clickedPrevious = {
                        vmPlayerMusic.setUiCurrentMusicListIndex(index2 - 1)
                        vmPlayerMusic.refreshNotification()
                        vmPlayerMusic.startServiceMusicPlayer(context.applicationContext)
                    },
                    clickedPlay = {
                        selectedMusic = playList[index1].musicList[index2]
                        vmPlayerMusic.playClicked()
                        vmPlayerMusic.refreshNotification()
                        vmPlayerMusic.startServiceMusicPlayer(context.applicationContext)
                    },
                    clickedNext = {
                        vmPlayerMusic.setUiCurrentMusicListIndex(index2 + 1)
                        vmPlayerMusic.refreshNotification()
                        vmPlayerMusic.startServiceMusicPlayer(context.applicationContext)
                    },
                    isPause = uiStatePlayerMusic.uiIsPause,
                    isShuffle = uiStatePlayerMusic.uiIsShuffle,
                    isRepeat = uiStatePlayerMusic.uiRepeat,
                    duration = playList[index1].musicList[index2].musicDurationFormat,
                    currentDuration = vmPlayerMusic.durationFormat(uiStatePlayerMusic.uiCurrentDuration),
                    durationValue = (uiStatePlayerMusic.uiCurrentDuration/1000).toFloat(),
                    onDurationChange = { vmPlayerMusic.setUiManualDurationValue(it.toLong()) },
                    valueRange = 0f..(playList[index1].musicList[index2].musicDuration/1000).toFloat(),
                    steps = playList[index1].musicList[index2].musicDuration.toInt()/1000
                )
            }
            composable(route = RouteViews.AddPlayList.name){
                AddPlayList(
                    modifier = Modifier.fillMaxSize(),
                    filter = filter,
                    playListName = playListName,
                    playListNameChange = { name -> playListName = name },
                    playListSearch = {
                        //Busqueda por nombre de playlist
                        filter = playList.filter{ it.name == playListName }
                            .ifEmpty { playList }
                        keyboardController?.hide()
                    },
                    addPlayListClicked = { name ->
                        //Agrega o actualiza una playlist
                        val existIndex = playList.indexOfLast { it.name == name }
                        val message: String
                        if(existIndex == -1){
                            message = if(name.isEmpty()){
                                context.getString(R.string.toast_playListName)
                            }else{
                                //Caso 1 si agrego una música a una nueva playlist
                                val list = listOf(selectedItem)
                                val item = MusicListModel(
                                    name = name,
                                    musicList = list
                                )
                                vmPlayerMusic.insertPlayList(item)
                                context.getString(R.string.toast_insertPlayList, name)
                            }
                        }else{
                            //Caso 2 si agrego una música a una playlist existente
                            val list = playList[existIndex].musicList.toMutableList()
                            list.add(selectedItem)
                            val item = MusicListModel(
                                id = playList[existIndex].id,
                                name = playList[existIndex].name,
                                musicList = list
                            )
                            vmPlayerMusic.updatePlayList(item)
                            message = context.getString(R.string.toast_addMusicPlayList)
                        }
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                        playListName = ""
                        selectedItem = MusicModel()
                    }
                )
            }
        }
    }
}