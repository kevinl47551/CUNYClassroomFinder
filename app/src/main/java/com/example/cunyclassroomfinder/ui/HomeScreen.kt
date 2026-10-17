package com.example.cunyclassroomfinder.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun HomeScreen(
    navToScrape: () -> Unit,
    navToRoomFinder: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
    ) {
        Button(onClick = navToScrape,) {
            Text("Scrape")
        }
        Button(onClick = navToRoomFinder) {
            Text("Find Room")
        }
    }
}