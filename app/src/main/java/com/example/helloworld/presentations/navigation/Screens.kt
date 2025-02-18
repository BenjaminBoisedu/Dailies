package com.example.helloworld.presentations.navigation

sealed class Screen(val route: String) {
    data object SplashScreen : Screen(route = "splash_screen")
    data object StoriesListScreen : Screen(route = "stories_list_screen")
    data object AddEditStoryScreen : Screen(route = "add_edit_stories_screen")
    data object DetailStoryScreen : Screen(route = "detail_story_screen")

}