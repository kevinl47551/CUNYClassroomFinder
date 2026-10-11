package com.example.cunyclassroomfinder.ui

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.cunyclassroomfinder.CUNYClassroomFinderApplication
import com.example.cunyclassroomfinder.ui.DownloadClasses.DownloadScreenViewModel

object AppViewModelProvider {
    val factory = viewModelFactory {
        initializer { DownloadScreenViewModel(
            application().container.cunyRepo,
            application().container.databaseRepo
        ) }
    }
}

fun CreationExtras.application(): CUNYClassroomFinderApplication =
    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as CUNYClassroomFinderApplication)