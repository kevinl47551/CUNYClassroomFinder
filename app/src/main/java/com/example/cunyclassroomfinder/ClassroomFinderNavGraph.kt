package com.example.cunyclassroomfinder

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.cunyclassroomfinder.ui.HomeScreen
import com.example.cunyclassroomfinder.ui.room.FindRoomScreen
import com.example.cunyclassroomfinder.ui.scrape.ScrapeScreen

enum class ClassroomFinderScreen(val title: String) {
    Home("CUNY Classroom Finder"),
    Scrape("Update class data"),
    Find("Find a free room")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CUNYClassroomFinderNavHost(
    navController: NavHostController = rememberNavController()
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentScreen = ClassroomFinderScreen.entries
        .find { it.name == backStackEntry?.destination?.route }
        ?: ClassroomFinderScreen.Home

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(currentScreen.title, fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    if (currentScreen != ClassroomFinderScreen.Home) {
                        IconButton(onClick = { navController.navigateUp() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = ClassroomFinderScreen.Home.name,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            composable(route = ClassroomFinderScreen.Home.name) {
                HomeScreen(
                    navToScrape = { navController.navigate(ClassroomFinderScreen.Scrape.name) },
                    navToRoomFinder = { navController.navigate(ClassroomFinderScreen.Find.name) }
                )
            }
            composable(route = ClassroomFinderScreen.Scrape.name) {
                ScrapeScreen()
            }
            composable(route = ClassroomFinderScreen.Find.name) {
                FindRoomScreen()
            }
        }
    }
}
