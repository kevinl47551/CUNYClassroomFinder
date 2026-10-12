package com.example.cunyclassroomfinder.ui.downloadClasses

import androidx.compose.material3.Button
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.cunyclassroomfinder.ui.AppViewModelProvider

@Composable
fun DownloadScreen(
    modifier: Modifier = Modifier,
    viewModel: DownloadScreenViewModel = viewModel(factory = AppViewModelProvider.factory)
) {
    Button(
        onClick = {viewModel.getAvailableCourses()},
        modifier = modifier
    ) { }
}