package com.example.helloworld.presentations.splashscreen


import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.helloworld.presentations.list.ListStoriesViewModel

@SuppressLint("MutableCollectionMutableState")
@Composable
fun ListStoriesScreen(
    navController: NavController,
    viewModel: ListStoriesViewModel
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    )
    {
        Spacer(modifier = Modifier.height(16.dp))
        Image(
            painter = painterResource(id = com.example.helloworld.R.drawable.ic_launcher_foreground),
            contentDescription = "Logo",
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        )
    }
}