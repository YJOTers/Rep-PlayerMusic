package com.example.playermusic.ui.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import com.example.playermusic.R
import com.example.playermusic.ui.model.MusicListModel

@Composable
fun AddPlayList(
    modifier: Modifier = Modifier,
    filter: List<MusicListModel>,
    playListName: String,
    playListNameChange: (String)-> Unit,
    playListSearch: ()-> Unit,
    addPlayListClicked: (String)-> Unit
){
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        TextField(
            modifier = Modifier.fillMaxWidth(0.9f),
            label = {
                Text(
                    text = stringResource(id = R.string.app_search),
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            value = playListName,
            onValueChange = { name -> playListNameChange(name) },
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = ImeAction.Search,
                keyboardType = KeyboardType.Text
            ),
            keyboardActions = KeyboardActions(
                onSearch = { playListSearch() }
            )
        )
        Spacer(modifier = Modifier.sizeIn(
            minHeight = dimensionResource(id = R.dimen.short_dp_1),
            maxHeight = dimensionResource(id = R.dimen.short_dp_2)
        ))
        IconButton(onClick = { addPlayListClicked(playListName) }) {
            Icon(
                imageVector = ImageVector.vectorResource(R.drawable.playlist_add_48),
                contentDescription = null
            )
        }
        Spacer(modifier = Modifier.sizeIn(
            minHeight = dimensionResource(id = R.dimen.short_dp_1),
            maxHeight = dimensionResource(id = R.dimen.short_dp_2)
        ))
        LazyColumn(
            modifier = Modifier.fillMaxWidth(0.9f),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ){
            items(filter){ item ->
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                        .padding(dimensionResource(id = R.dimen.short_dp_2))
                        .clickable { addPlayListClicked(item.name) }
                )
            }
        }
    }
}