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
import androidx.room.Room
import com.example.helloworld.data.source.StoriesDatabase
import com.example.helloworld.presentations.addedit.AddEditStoryScreen
import com.example.helloworld.presentations.details.DetailStoryScreen
import com.example.helloworld.presentations.list.ListStoriesScreen
import com.example.helloworld.presentations.list.ListStoriesViewModel
import com.example.helloworld.presentations.navigation.Screen
import com.example.helloworld.ui.theme.HelloWorldTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val db by lazy {
        Room.databaseBuilder(
            applicationContext,
            StoriesDatabase::class.java,
            StoriesDatabase.DATABASE_NAME
        )
            .addMigrations(
                StoriesDatabase.MIGRATION_1_2,
                StoriesDatabase.MIGRATION_2_3,
                StoriesDatabase.MIGRATION_3_4,
                StoriesDatabase.MIGRATION_4_5,
                StoriesDatabase.MIGRATION_3_5,
                StoriesDatabase.MIGRATION_5_6
            )
            .build()
    }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        runNotificationWorkerNow(applicationContext)

        scheduleNotificationWorker(applicationContext)

        enableEdgeToEdge()
        setContent {
            HelloWorldTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { padding ->
                    val navController = rememberNavController()

                    NavHost(
                        navController = navController,
                        startDestination = Screen.StoriesListScreen.route,
                        modifier = Modifier.padding()
                    ) {
                        composable(Screen.StoriesListScreen.route) {
                            ListStoriesScreen(navController)
                        }
                        composable(Screen.AddEditStoryScreen.route + "?storyId={storyId}",
                            arguments = listOf(
                                navArgument("storyId")
                                { type = NavType.IntType
                                defaultValue = -1}
                            )
                        ) {
                            AddEditStoryScreen(navController)
                        }
                        composable(Screen.DetailStoryScreen.route) {
                            val viewModel: ListStoriesViewModel = viewModel()
                            DetailStoryScreen(navController, viewModel)
                        }
                    }
                }
            }
        }
    }
}



