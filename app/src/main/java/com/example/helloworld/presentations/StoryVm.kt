package com.example.helloworld.presentations

import androidx.compose.ui.graphics.Color
import com.example.helloworld.ui.theme.GrayDarker
import com.example.helloworld.ui.theme.LightBlue
import com.example.helloworld.ui.theme.LightBlueDark

import com.example.helloworld.ui.theme.PurpleGrey40

sealed class PriorityType (
    val backgroundColor: Color,
    val foregroundColor: Color
) {
    data object HighPriority:PriorityType(
        LightBlueDark, GrayDarker)
    data object StandardPriority:PriorityType(
        LightBlue, PurpleGrey40)
}

data object HighPriority:PriorityType(
    LightBlueDark, GrayDarker)
data object StandardPriority:PriorityType(
    LightBlue, PurpleGrey40)
