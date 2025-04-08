package com.example.daily

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.room.Room
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.daily.data.source.DailiesDatabase
import com.example.daily.presentations.addedit.AddEditDailyScreen
import com.example.daily.presentations.details.DetailDailyScreen
import com.example.daily.presentations.graph.StatsScreen
import com.example.daily.presentations.graph.StatsViewModel
import com.example.daily.presentations.list.ListDailiesScreen
import com.example.daily.presentations.list.ListDailiesViewModel
import com.example.daily.presentations.meteo.MeteoScreen
import com.example.daily.presentations.meteo.MeteoViewModel
import com.example.daily.presentations.navigation.Screen
import com.example.daily.presentations.splashscreen.SplashScreen
import com.example.daily.sensor.LocationScreen
import com.example.daily.sensor.LocationViewModel
import com.example.daily.ui.theme.HelloWorldTheme
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.TimeUnit

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    companion object {
        const val NOTIFICATION_WORK_NAME = "notification_periodic_work"
        const val NOTIFICATION_IMMEDIATE_WORK_NAME = "notification_immediate_work"
    }

    private val requestNotificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Permission accordée, programmer les notifications
            NotificationUtils.runNotificationWorkerNow(applicationContext)
            NotificationUtils.scheduleNotificationWorker(applicationContext)
            Toast.makeText(
                this,
                "Les notifications sont maintenant activées",
                Toast.LENGTH_SHORT
            ).show()
        } else {
            // Permission refusée
            Toast.makeText(
                this,
                "Les notifications sont désactivées",
                Toast.LENGTH_LONG
            ).show()
        }
        askLocalisationPermission()
    }

    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false

        if (fineLocationGranted || coarseLocationGranted) {
            // Permissions accordées
            Toast.makeText(
                this,
                "Les fonctionnalités de localisation sont maintenant activées",
                Toast.LENGTH_SHORT
            ).show()
        } else {
            // Permissions refusées
            Toast.makeText(
                this,
                "Les fonctionnalités de localisation sont désactivées",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private val db by lazy {
        Room.databaseBuilder(
            applicationContext,
            DailiesDatabase::class.java,
            DailiesDatabase.DATABASE_NAME
        )
            .addMigrations(
                DailiesDatabase.MIGRATION_1_2,
                DailiesDatabase.MIGRATION_2_3,
                DailiesDatabase.MIGRATION_3_4,
                DailiesDatabase.MIGRATION_4_5,
                DailiesDatabase.MIGRATION_3_5,
                DailiesDatabase.MIGRATION_5_6,
                DailiesDatabase.MIGRATION_6_7
            )
            .build()
    }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        askNotificationPermission()

        enableEdgeToEdge()

        enableEdgeToEdge()
        setContent {
            HelloWorldTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { padding ->
                    val navController = rememberNavController()

                    NavHost(
                        navController = navController,
                        startDestination = Screen.SplashScreen.route,
                        modifier = Modifier.padding()
                    ) {
                        composable(Screen.SplashScreen.route) {
                            SplashScreen(navController)
                        }
                        composable(Screen.DailiesListScreen.route) {
                            ListDailiesScreen(navController)
                        }
                        composable(Screen.AddEditDailyScreen.route + "?dailyId={dailyId}",
                            arguments = listOf(
                                navArgument("dailyId") {
                                    type = NavType.IntType
                                    defaultValue = -1
                                }
                            )
                        ) {
                            AddEditDailyScreen(navController)
                        }
                        composable(Screen.DetailDailyScreen.route) {
                            val viewModel: ListDailiesViewModel = viewModel()
                            DetailDailyScreen(navController, viewModel)
                        }
                        composable(Screen.LocationScreen.route) {
                            val viewModel: LocationViewModel = viewModel()
                            val hasLocationPermission = hasLocationPermissions()
                            LocationScreen(
                                hasLocationPermission = hasLocationPermission,
                                viewModel = viewModel,
                                onRequestPermission = {
                                    checkAndRequestLocationPermissions()
                                }
                            )
                        }
                        composable(Screen.MeteoScreen.route) {
                            val meteoviewmodel: MeteoViewModel = viewModel()
                            val hasLocationPermission = hasLocationPermissions()
                            MeteoScreen(
                                hasLocationPermission = hasLocationPermission,
                                onRequestPermission = {
                                    checkAndRequestLocationPermissions()
                                },
                                meteoviewmodel = meteoviewmodel,
                                navController = navController
                            )
                        }
                        composable(Screen.StatsScreen.route) {
                            val viewModel: ListDailiesViewModel = hiltViewModel()
                            val statsViewModel: StatsViewModel = hiltViewModel()
                            StatsScreen(
                                viewModel = viewModel,
                                statsViewModel = statsViewModel,
                                navController = navController
                            )
                        }
                    }
                }
            }
        }
    }

    private fun hasLocationPermissions(): Boolean {
        val fineLocationGranted = ContextCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarseLocationGranted = ContextCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        return fineLocationGranted || coarseLocationGranted
    }

    private fun checkAndRequestLocationPermissions() {
        if (!hasLocationPermissions()) {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    private fun askNotificationPermission() {
        // Ceci est nécessaire uniquement pour API level >= 33 (TIRAMISU)
        if (Build.VERSION.SDK_INT >= 33) {  // Build.VERSION_CODES.TIRAMISU = 33
            val postNotificationPermission = "android.permission.POST_NOTIFICATIONS"

            if (ContextCompat.checkSelfPermission(
                    this,
                    postNotificationPermission
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                // Permission déjà accordée, programmer les notifications
                NotificationUtils.runNotificationWorkerNow(applicationContext)
                NotificationUtils.scheduleNotificationWorker(applicationContext)

                // Passer à la demande de localisation
                askLocalisationPermission()
            } else if (shouldShowRequestPermissionRationale(postNotificationPermission)) {
                // Demander la permission de notification
                requestNotificationPermissionLauncher.launch(postNotificationPermission)
                // Ne pas appeler askLocalisationPermission() ici car elle sera appelée dans le callback
            } else {
                // Demander la permission de notification
                requestNotificationPermissionLauncher.launch(postNotificationPermission)
                // Ne pas appeler askLocalisationPermission() ici car elle sera appelée dans le callback
            }
        } else {
            // Pour les versions < Android 13, pas besoin de permission pour les notifications
            NotificationUtils.runNotificationWorkerNow(applicationContext)
            NotificationUtils.scheduleNotificationWorker(applicationContext)

            // Passer directement à la demande de localisation
            askLocalisationPermission()
        }
    }

    private fun askLocalisationPermission() {
        val fineLocationPermission = Manifest.permission.ACCESS_FINE_LOCATION
        val coarseLocationPermission = Manifest.permission.ACCESS_COARSE_LOCATION

        if (ContextCompat.checkSelfPermission(this, fineLocationPermission) != PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(this, coarseLocationPermission) != PackageManager.PERMISSION_GRANTED
        ) {
            locationPermissionLauncher.launch(
                arrayOf(fineLocationPermission, coarseLocationPermission)
            )
        } else {
            // Les permissions de localisation sont déjà accordées
            Toast.makeText(
                this,
                "Les fonctionnalités de localisation sont déjà activées",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}

// Classe d'utilitaires pour gérer les notifications
object NotificationUtils {
    fun runNotificationWorkerNow(context: Context) {
        val workRequest = OneTimeWorkRequestBuilder<NotificationWorker>().build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            MainActivity.NOTIFICATION_IMMEDIATE_WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
    }

    fun scheduleNotificationWorker(context: Context) {
        val workRequest = PeriodicWorkRequestBuilder<NotificationWorker>(
            15, TimeUnit.MINUTES  // Vérifier toutes les 15 minutes
        ).build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            MainActivity.NOTIFICATION_WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            workRequest
        )
    }
}