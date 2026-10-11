package com.example.cunyclassroomfinder

import android.app.Application
import com.example.cunyclassroomfinder.data.AppContainer

class CUNYClassroomFinderApplication: Application() {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}