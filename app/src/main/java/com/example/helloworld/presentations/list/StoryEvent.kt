package com.example.helloworld.presentations.list


sealed class StoryEvent {
    data class Delete(val story: StoryVM) : StoryEvent()
    data class Edit(val story: StoryVM) : StoryEvent()
    data class Detail(val story: StoryVM) : StoryEvent()
}