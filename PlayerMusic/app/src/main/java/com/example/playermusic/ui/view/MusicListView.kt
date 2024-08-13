package com.example.playermusic.ui.view

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import com.example.playermusic.R
import com.example.playermusic.ui.model.MusicModel
import com.example.playermusic.ui.viewModel.PlayerMusicViewModel

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MusicList(
    modifier: Modifier = Modifier,
    vmPlayerMusic: PlayerMusicViewModel,
    musicList: List<MusicModel> = listOf(),
    itemClicked: (MusicModel)-> Unit,
    addPlayListClicked: (MusicModel)-> Unit
){
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        items(musicList){ itemMusic ->
            ItemMusic(
                modifier = Modifier
                    .padding(dimensionResource(id = R.dimen.short_dp_1))
                    .fillMaxWidth()
                    .combinedClickable(
                        onClick = { itemClicked(itemMusic) },
                        onLongClick = { addPlayListClicked(itemMusic) }
                    ),
                title = itemMusic.musicName,
                artist = itemMusic.artistName,
                duration = vmPlayerMusic.durationFormat(itemMusic.musicDuration)
            )
        }
    }
}

@Composable
private fun ItemMusic(
    modifier: Modifier = Modifier,
    title: String,
    artist: String,
    duration: String
){
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(dimensionResource(id = R.dimen.short_dp_3))
    ){
        Column(
            modifier = Modifier.padding(dimensionResource(id = R.dimen.short_dp_2)),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ){
            Text(
                text = title.uppercase(),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.sizeIn(
                minHeight = dimensionResource(id = R.dimen.short_dp_1),
                maxHeight = dimensionResource(id = R.dimen.short_dp_2)
            ))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ){
                Text(
                    text = artist.uppercase(),
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = duration,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}