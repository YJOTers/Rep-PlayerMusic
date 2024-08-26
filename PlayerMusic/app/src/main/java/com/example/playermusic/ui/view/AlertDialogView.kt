package com.example.playermusic.ui.view

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable

@Composable
fun MyAlertDialog(
    confirm: String,
    dismiss: String,
    title: String,
    message: String,
    confirmClicked: ()-> Unit,
    dismissClicked: ()-> Unit,
    case: Int = 1,
    value: String = "",
    label: String = "",
    onValue: (String)-> Unit = {}
){
    AlertDialog(
        onDismissRequest = dismissClicked,
        confirmButton = { Button(onClick = confirmClicked){ Text(confirm) }},
        dismissButton = { Button(onClick = dismissClicked){ Text(dismiss) }},
        title = { Text(title) },
        text = {
            when(case){
                1 -> { Text(message) }
                2 -> {
                    Column{
                        Text(message)
                        TextField(
                            value = value,
                            label = { Text(label) },
                            onValueChange = { onValue(it) }
                        )
                    }
                }
            }
        }
    )
}