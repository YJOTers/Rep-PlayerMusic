package com.example.playermusic

import android.app.AlertDialog
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.playermusic.ui.model.MusicListModel
import com.example.playermusic.ui.view.AddPlayList
import com.example.playermusic.ui.view.ArtistList
import com.example.playermusic.ui.view.CurrentMusic
import com.example.playermusic.ui.view.CurrentPlayList
import com.example.playermusic.ui.view.MusicList
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
        title = { Text(
            text = stringResource(routeTitles.idTitle, routeArg),
            style = MaterialTheme.typography.titleLarge
        ) },
        navigationIcon = {
            if(canNavigateBack){
                IconButton(onClick = navigateUp) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = null
                    )
                }
            }
        },
        actions = {
            if(routeTitles.name == RouteViews.ArtistList.name){
                IconButton(onClick = goPlayList) {
                    Icon(
                        imageVector = ImageVector.vectorResource(R.drawable.playlist_48),
                        contentDescription = null
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
    val focusManager = LocalFocusManager.current
    val uiStatePlayerMusic by vmPlayerMusic.uiState.collectAsState()
    //Vista ToolBarMenu
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentScreen = RouteViews.valueOf(
        backStackEntry?.destination?.route ?: RouteViews.ArtistList.name
    )
    //Vista ArtistList
    val artistList = uiStatePlayerMusic.uiArtistList
    val index0 = uiStatePlayerMusic.uiCurrentArtistIndex
    val index1 = uiStatePlayerMusic.uiCurrentPlayListIndex
    //Vista MusicList
    val musicList = uiStatePlayerMusic.uiMusicList
    val index2 = uiStatePlayerMusic.uiCurrentMusicIndex
    //Vista PlayList
    val playList = uiStatePlayerMusic.uiPlayList
    //Vista CurrentPlayList
    val playListMusic = uiStatePlayerMusic.uiPlayListMusic
    //Variables locales
    val playListName = uiStatePlayerMusic.uiPlayListName
    val selectedMusic = uiStatePlayerMusic.uiSelectedMusic
    val filter = uiStatePlayerMusic.uiFilter
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
                    vmPlayerMusic.getPlayList()
                    navController.navigate(RouteViews.PlayList.name)
                }
            )
        }
    ){ innerPadding ->
        NavHost(
            navController = navController,
            startDestination = RouteViews.ArtistList.name,
            modifier = Modifier.padding(innerPadding))
        {
            composable(route = RouteViews.ArtistList.name){
                ArtistList(
                    modifier = Modifier.fillMaxSize(),
                    artistList = artistList,
                    itemClicked = { itemArtist ->
                        vmPlayerMusic.setUiMusicList(itemArtist)
                        //Navega a la lista de música del artista seleccionado
                        navController.navigate(RouteViews.MusicList.name)
                    }
                )
            }
            composable(route = RouteViews.MusicList.name){
                MusicList(
                    modifier = Modifier.fillMaxSize(),
                    vmPlayerMusic = vmPlayerMusic,
                    musicList = musicList,
                    itemClicked = { itemMusic ->
                        val index = musicList.indexOf(itemMusic)
                        vmPlayerMusic.musicClicked(index, false)
                        //Navega a la música seleccionada
                        navController.navigate(RouteViews.CurrentMusic1.name)
                    },
                    addPlayListClicked = { itemMusic ->
                        vmPlayerMusic.setUiSelectedMusic(itemMusic)
                        navController.navigate(RouteViews.AddPlayList.name)
                    }
                )
            }
            composable(route = RouteViews.CurrentMusic1.name){
                CurrentMusic(
                    modifier = Modifier.fillMaxSize(),
                    artistName = musicList[index2].artistName,
                    musicName = musicList[index2].musicName,
                    albumName = musicList[index2].albumName,
                    albumUri = vmPlayerMusic.getAlbumUri(
                        context.applicationContext,
                        musicList[index2].albumUri
                    ),
                    clickedPlayList = {
                        //Navega a la lista de playlist
                        vmPlayerMusic.getPlayList()
                        navController.navigate(RouteViews.PlayList.name)
                    },
                    clickedShuffle = {
                        if(!uiStatePlayerMusic.uiIsShuffle){
                            vmPlayerMusic.setUiIsShuffle(true)
                            Toast.makeText(context, context.getString(R.string.toast_shuffle1), Toast.LENGTH_SHORT).show()
                        }else{
                            vmPlayerMusic.setUiIsShuffle(false)
                            Toast.makeText(context, context.getString(R.string.toast_shuffle2), Toast.LENGTH_SHORT).show()
                        }
                    },
                    clickedRepeat = {
                        if(!uiStatePlayerMusic.uiIsRepeat){
                            vmPlayerMusic.setUiIsRepeat(true)
                            Toast.makeText(context, context.getString(R.string.toast_repeat1), Toast.LENGTH_SHORT).show()
                        }else{
                            vmPlayerMusic.setUiIsRepeat(false)
                            Toast.makeText(context, context.getString(R.string.toast_repeat2), Toast.LENGTH_SHORT).show()
                        }
                    },
                    clickedPrevious = {
                        vmPlayerMusic.musicClicked(index2 - 1, false)
                    },
                    clickedPlay = {
                        vmPlayerMusic.playClicked(false)
                        vmPlayerMusic.startServiceMusicPlayer(context.applicationContext)
                    },
                    clickedNext = {
                        vmPlayerMusic.musicClicked(index2 + 1, false)
                    },
                    isPause = uiStatePlayerMusic.uiIsPause,
                    isShuffle = uiStatePlayerMusic.uiIsShuffle,
                    isRepeat = uiStatePlayerMusic.uiIsRepeat,
                    duration = vmPlayerMusic.durationFormat(musicList[index2].musicDuration),
                    currentDuration = vmPlayerMusic.durationFormat(uiStatePlayerMusic.uiCurrentDuration),
                    durationValue = (uiStatePlayerMusic.uiCurrentDuration/1000).toFloat(),
                    onDurationChange = { vmPlayerMusic.setUiManualDurationValue(it.toLong()) },
                    valueRange = 0f..(musicList[index2].musicDuration/1000).toFloat(),
                    steps = musicList[index2].musicDuration.toInt()/1000
                )
            }
            composable(route = RouteViews.PlayList.name){
                PlayList(
                    modifier = Modifier.fillMaxSize(),
                    playList = playList,
                    itemClicked = { itemPlayList ->
                        vmPlayerMusic.setUiPlayListMusic(itemPlayList)
                        //Navega a la lista de música del artista seleccionado
                        navController.navigate(RouteViews.CurrentPlayList.name)
                    },
                    removePlayListClicked = { itemPlayList ->
                        val smsMessage = context.getString(R.string.alert_dialog_remove)
                        val smsAlert = context.getString(R.string.toast_removePlayList, itemPlayList.name)
                        val smsYes = context.getString(R.string.alert_dialog_yes)
                        val smsNo = context.getString(R.string.alert_dialog_no)
                        //Solicitar confirmacion de borrado
                        AlertDialog.Builder(context)
                            .setMessage(smsMessage)
                            .setPositiveButton(smsYes) { action, _ ->
                                vmPlayerMusic.deletePlayList(itemPlayList)
                                Toast.makeText(context, smsAlert, Toast.LENGTH_SHORT).show()
                                action.dismiss()
                            }
                            .setNegativeButton(smsNo) { action, _ ->
                                action.dismiss()
                            }
                            .show()
                    }
                )
            }
            composable(route = RouteViews.CurrentPlayList.name){
                CurrentPlayList(
                    modifier = Modifier.fillMaxSize(),
                    vmPlayerMusic = vmPlayerMusic,
                    playListMusic = playListMusic,
                    itemClicked = { itemPlayListMusic ->
                        val index = playListMusic.indexOf(itemPlayListMusic)
                        vmPlayerMusic.musicClicked(index, true)
                        //Navega a la música seleccionada
                        navController.navigate(RouteViews.CurrentMusic2.name)
                    }
                )
            }
            composable(route = RouteViews.CurrentMusic2.name){
                CurrentMusic(
                    modifier = Modifier.fillMaxSize(),
                    artistName = playListMusic[index2].artistName,
                    musicName = playListMusic[index2].musicName,
                    albumName = playListMusic[index2].albumName,
                    albumUri = vmPlayerMusic.getAlbumUri(
                        context.applicationContext,
                        playListMusic[index2].albumUri
                    ),
                    clickedPlayList = {
                        //Navega al inicio ArtistView
                        navController.popBackStack(RouteViews.ArtistList.name, false)
                    },
                    clickedShuffle = {
                        if(!uiStatePlayerMusic.uiIsShuffle){
                            vmPlayerMusic.setUiIsShuffle(true)
                            Toast.makeText(context, context.getString(R.string.toast_shuffle1), Toast.LENGTH_SHORT).show()
                        }else{
                            vmPlayerMusic.setUiIsShuffle(false)
                            Toast.makeText(context, context.getString(R.string.toast_shuffle2), Toast.LENGTH_SHORT).show()
                        }
                    },
                    clickedRepeat = {
                        if(!uiStatePlayerMusic.uiIsRepeat){
                            vmPlayerMusic.setUiIsRepeat(true)
                            Toast.makeText(context, context.getString(R.string.toast_repeat1), Toast.LENGTH_SHORT).show()
                        }else{
                            vmPlayerMusic.setUiIsRepeat(false)
                            Toast.makeText(context, context.getString(R.string.toast_repeat2), Toast.LENGTH_SHORT).show()
                        }
                    },
                    clickedPrevious = {
                        vmPlayerMusic.musicClicked(index2 - 1, true)
                    },
                    clickedPlay = {
                        vmPlayerMusic.playClicked(true)
                        vmPlayerMusic.startServiceMusicPlayer(context.applicationContext)
                    },
                    clickedNext = {
                        vmPlayerMusic.musicClicked(index2 + 1, true)
                    },
                    isPause = uiStatePlayerMusic.uiIsPause,
                    isShuffle = uiStatePlayerMusic.uiIsShuffle,
                    isRepeat = uiStatePlayerMusic.uiIsRepeat,
                    duration = vmPlayerMusic.durationFormat(playListMusic[index2].musicDuration),
                    currentDuration = vmPlayerMusic.durationFormat(uiStatePlayerMusic.uiCurrentDuration),
                    durationValue = (uiStatePlayerMusic.uiCurrentDuration/1000).toFloat(),
                    onDurationChange = { vmPlayerMusic.setUiManualDurationValue(it.toLong()) },
                    valueRange = 0f..(playListMusic[index2].musicDuration/1000).toFloat(),
                    steps = playListMusic[index2].musicDuration.toInt()/1000
                )
            }
            composable(route = RouteViews.AddPlayList.name){
                AddPlayList(
                    modifier = Modifier.fillMaxSize(),
                    filter = filter,
                    playListName = playListName,
                    playListNameChange = { name -> vmPlayerMusic.setUiPlayListName(name) },
                    playListSearch = {
                        //Busqueda por nombre de playlist
                        vmPlayerMusic.setUiFilter(
                            if(playListName.isEmpty()) playList
                            else playList.filter{ it.name == playListName }
                        )
                        focusManager.clearFocus()
                    },
                    addPlayListClicked = { name ->
                        val existIndex = playList.indexOfLast { it.name == name }
                        val message: String
                        if(existIndex == -1){
                            message = if(name.isEmpty()){
                                context.getString(R.string.toast_playListName)
                            }else{
                                //Caso 1 si agrego una música a una nueva playlist
                                val list = listOf(selectedMusic)
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
                            list.add(selectedMusic)
                            val item = MusicListModel(
                                id = playList[existIndex].id,
                                name = playList[existIndex].name,
                                musicList = list
                            )
                            vmPlayerMusic.updatePlayList(item)
                            message = context.getString(R.string.toast_updatePlayList, name)
                        }
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                        vmPlayerMusic.setUiPlayListName("")
                    }
                )
            }
        }
    }
}