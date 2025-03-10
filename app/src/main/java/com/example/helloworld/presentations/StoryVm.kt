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
    fun toInt(): Int = when (this) {
        is HighPriority -> 1
        is StandardPriority -> 0
        com.example.helloworld.presentations.HighPriority -> 1
        com.example.helloworld.presentations.StandardPriority -> 2
    }

    data object HighPriority:PriorityType(
        LightBlueDark, GrayDarker
    )
    data object StandardPriority:PriorityType(
        LightBlue, PurpleGrey40
    )

    companion object {
        fun fromInt(value: Int): PriorityType = when (value) {
            1 -> HighPriority
            else -> StandardPriority
        }
    }
}

data object HighPriority:PriorityType(
    LightBlueDark, GrayDarker)
data object StandardPriority:PriorityType(
    LightBlue, PurpleGrey40)
