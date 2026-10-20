package com.example.cunyclassroomfinder.ui.scrape

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.cunyclassroomfinder.R
import com.example.cunyclassroomfinder.ui.AppViewModelProvider

@Composable
fun ScrapeScreen(
    navToHome: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ScrapeScreenViewModel = viewModel(factory = AppViewModelProvider.factory)
) {
    Box(modifier = modifier) {
        Column(modifier = modifier) {
            Button(
                onClick = {viewModel.getAvailableCourses()},
                modifier = modifier
            ) { }
        }
        Button(
            onClick = navToHome,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp)
        ) {
            Text(stringResource(R.string.home))
        }
    }

}