package com.example.cunyclassroomfinder.ui.room

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.cunyclassroomfinder.ui.AppViewModelProvider

@Composable
fun FindRoomScreen(
    modifier: Modifier = Modifier,
    viewModel: FindRoomViewModel = viewModel(factory = AppViewModelProvider.factory)
) {

}