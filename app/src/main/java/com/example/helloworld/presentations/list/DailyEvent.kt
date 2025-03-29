package com.example.helloworld.presentations.list


sealed class DailyEvent {
    data class Delete(val daily: DailyVM) : DailyEvent()
    data class Edit(val daily: DailyVM) : DailyEvent()
    data class Detail(val daily: DailyVM) : DailyEvent()
}