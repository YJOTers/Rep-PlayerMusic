package com.example.playermusic.ui.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import com.example.playermusic.R
import com.example.playermusic.ui.model.MusicListModel

@Composable
fun ArtistList(
    modifier: Modifier = Modifier,
    artistList: List<MusicListModel> = listOf(),
    itemClicked: (MusicListModel)-> Unit
){
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        items(artistList){ itemArtist ->
            ItemArtist(
                modifier = Modifier
                    .padding(dimensionResource(id = R.dimen.short_dp_1))
                    .fillMaxWidth()
                    .clickable { itemClicked(itemArtist) },
                artistName = itemArtist.name,
                totalArtistMusic = itemArtist.totalArtistMusic,
                totalArtistAlbum = itemArtist.totalArtistAlbum
            )
        }
    }
}

@Composable
private fun ItemArtist(
    modifier: Modifier = Modifier,
    artistName: String,
    totalArtistMusic: String,
    totalArtistAlbum: String
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
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ){
                Text(
                    text = artistName.uppercase(),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier
                        .weight(1f)
                        .horizontalScroll(rememberScrollState()),
                    maxLines = 1,
                    overflow = TextOverflow.Clip
                )
            }
            Spacer(modifier = Modifier.sizeIn(
                minHeight = dimensionResource(id = R.dimen.short_dp_1),
                maxHeight = dimensionResource(id = R.dimen.short_dp_2)
            ))
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ){
                Text(
                    text = totalArtistMusic.uppercase(),
                    textAlign = TextAlign.Start,
                    fontWeight = FontWeight.Normal,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = totalArtistAlbum.uppercase(),
                    textAlign = TextAlign.End,
                    fontWeight = FontWeight.Normal,
                    fontSize = 16.sp
                )
            }
        }
    }
}