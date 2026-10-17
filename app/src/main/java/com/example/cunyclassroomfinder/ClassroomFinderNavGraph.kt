package com.example.cunyclassroomfinder

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.cunyclassroomfinder.ui.HomeScreen
import com.example.cunyclassroomfinder.ui.scrape.ScrapeScreen

enum class ClassroomFinderScreen() {
    Home,
    Scrape,
    Find
}

@Composable
fun CUNYClassroomFinderNavHost(
    navController: NavHostController = rememberNavController()
) {
    val backStackEntry by navController.currentBackStackEntryAsState()

    Scaffold() { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = ClassroomFinderScreen.Home.name,
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
        ) {
            composable(route = ClassroomFinderScreen.Home.name) {
                HomeScreen(
                    navToScrape = {navController.navigate(ClassroomFinderScreen.Scrape.name)},
                    navToRoomFinder = {navController.navigate(ClassroomFinderScreen.Find.name)}
                )
            }
            composable(route = ClassroomFinderScreen.Scrape.name) {
                ScrapeScreen()
            }
            composable(route = ClassroomFinderScreen.Find.name) {

            }
        }
    }
}