package com.example.helloworld

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

import com.example.helloworld.presentations.addedit.AddEditStoryScreen
import com.example.helloworld.presentations.addedit.AddEditStoryViewModel
import com.example.helloworld.presentations.details.DetailStoryScreen
import com.example.helloworld.presentations.list.ListStoriesScreen
import com.example.helloworld.presentations.list.ListStoriesViewModel
import com.example.helloworld.presentations.navigation.Screen
import com.example.helloworld.ui.theme.HelloWorldTheme


class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            HelloWorldTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { _ ->
                    val navController = rememberNavController()

                    NavHost(
                        navController = navController,
                        startDestination = Screen.StoriesListScreen.route,
                        modifier = Modifier.padding()
                    ) {
                        composable(Screen.StoriesListScreen.route) {
                            val stories = viewModel<ListStoriesViewModel>()
                            ListStoriesScreen(navController, stories)
                        }
                        composable(Screen.AddEditStoryScreen.route + "?storyId={storyId}",
                            arguments = listOf(
                                navArgument("storyId")
                                { type = NavType.IntType
                                defaultValue = -1}
                            )
                        ) { navBackStackEntry ->
                            val storyId = navBackStackEntry.arguments?.getInt("storyId") ?: -1
                            val story = viewModel<AddEditStoryViewModel>{
                                AddEditStoryViewModel(storyId)
                            }
                            AddEditStoryScreen(navController, story)
                        }
                        composable(Screen.DetailStoryScreen.route) {
                            val viewModel: ListStoriesViewModel = viewModel()
                            DetailStoryScreen(navController, viewModel)
                        }
                    }
                }
            }
        }
        scheduleNotificationWorker(applicationContext)

    }
}



