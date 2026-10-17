package com.example.cunyclassroomfinder.ui.scrape

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.cunyclassroomfinder.ui.AppViewModelProvider

@Composable
fun ScrapeScreen(
    modifier: Modifier = Modifier,
    viewModel: ScrapeScreenViewModel = viewModel(factory = AppViewModelProvider.factory)
) {
    Column(modifier = modifier) {
        Button(
            onClick = {viewModel.getAvailableCourses()},
            modifier = modifier
        ) { }

    }
}