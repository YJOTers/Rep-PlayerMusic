package com.example.playermusic.ui.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextOverflow
import com.example.playermusic.R
import com.example.playermusic.ui.model.MusicModel

@Composable
fun MusicList(
    modifier: Modifier = Modifier,
    musicList: List<MusicModel> = listOf(),
    itemPlaying: MusicModel,
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
                    .clickable{ itemClicked(itemMusic) },
                title = itemMusic.musicName,
                artist = itemMusic.artistName,
                duration = itemMusic.musicDurationFormat,
                isPlaying = itemMusic == itemPlaying,
                addPlayListClicked = { addPlayListClicked(itemMusic) }
            )
        }
    }
}

@Composable
private fun ItemMusic(
    modifier: Modifier = Modifier,
    title: String,
    artist: String,
    duration: String,
    isPlaying: Boolean,
    addPlayListClicked: ()-> Unit
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
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ){
                Text(
                    modifier = Modifier
                        .horizontalScroll(rememberScrollState())
                        .weight(0.98f),
                    text = title.uppercase(),
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Clip
                )
                Spacer(modifier = Modifier.weight(0.02f))
                IconButton(
                    onClick = addPlayListClicked,
                    modifier = Modifier.size(dimensionResource(id = R.dimen.short_dp_4))
                ) {
                    Icon(
                        imageVector = ImageVector.vectorResource(id = R.drawable.playlist_add_48),
                        contentDescription = null,
                        modifier = Modifier.size(dimensionResource(id = R.dimen.short_dp_4))
                    )
                }
                if(isPlaying){
                    Icon(
                        imageVector = ImageVector.vectorResource(id = R.drawable.music_48),
                        contentDescription = null,
                        modifier = Modifier.size(dimensionResource(id = R.dimen.short_dp_3))
                    )
                }else{
                    Spacer(modifier = Modifier.size(dimensionResource(id = R.dimen.short_dp_3)))
                }
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ){
                Text(
                    modifier = Modifier
                        .horizontalScroll(rememberScrollState())
                        .weight(0.98f),
                    text = artist.uppercase(),
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Clip
                )
                Spacer(modifier = Modifier.weight(0.02f))
                Text(
                    text = duration,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.size(dimensionResource(id = R.dimen.short_dp_3)))
            }
        }
    }
}