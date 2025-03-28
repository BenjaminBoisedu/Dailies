package com.example.helloworld

import android.app.AlertDialog
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
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


    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Permission granted, schedule notifications
            scheduleNotificationWorker(applicationContext)
        } else {
            // Permission denied - you could show a message explaining
            // that notifications won't work
        }
    }

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

        askNotificationPermission()

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
    private fun askNotificationPermission() {
        // This is only necessary for API level >= 33 (TIRAMISU)
        if (Build.VERSION.SDK_INT >= 33) {  // Build.VERSION_CODES.TIRAMISU = 33
            val postNotificationPermission = "android.permission.POST_NOTIFICATIONS"

            if (ContextCompat.checkSelfPermission(
                    this,
                    postNotificationPermission
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                // Permission is already granted, schedule notifications
                runNotificationWorkerNow(applicationContext)
                scheduleNotificationWorker(applicationContext)
            } else if (shouldShowRequestPermissionRationale(postNotificationPermission)) {
                // Directly request permission without dialog
                requestPermissionLauncher.launch(postNotificationPermission)
            } else {
                // Directly request permission
                requestPermissionLauncher.launch(postNotificationPermission)
            }
        } else {
            // For Android < 13, notification permission is granted automatically
            runNotificationWorkerNow(applicationContext)
            scheduleNotificationWorker(applicationContext)
        }
    }
}



