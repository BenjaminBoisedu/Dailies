package com.example.daily.presentations.navigation

sealed class Screen(val route: String) {
    data object SplashScreen : Screen(route = "splash_screen")
    data object DailiesListScreen : Screen(route = "dailies_list_screen")
    data object AddEditDailyScreen : Screen(route = "add_edit_dailies_screen")
    data object DetailDailyScreen : Screen(route = "detail_daily_screen")
}

