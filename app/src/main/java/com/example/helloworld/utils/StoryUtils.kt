package com.example.helloworld.utils

import com.example.helloworld.presentations.HighPriority
import com.example.helloworld.presentations.StandardPriority
import com.example.helloworld.presentations.list.StoryVM
import kotlinx.coroutines.flow.flow

private val storiesList: MutableList<StoryVM> = mutableListOf(
    StoryVM(
        id = 1,
        title = "Revisez vos cours",
        description = "Rappel pour réviser les cours",
        done = false,
        priority = StandardPriority,
        date = "21-02-2025",
        time = "20:38"
    ),
    StoryVM(
        id = 2,
        title = "Faire mes courses",
        description = "Rappel pour faire les courses",
        done = true,
        priority = HighPriority,
        date = "18-02-2025",
        time = "12:00"
    ),
    StoryVM(
        id = 3,
        title = "Notifications",
        description = "En tant que abonné, je veux recevoir des notifications",
        done = true,
        priority = HighPriority,
        date = "13-02-2025",
        time = "12:00"
    ),
    StoryVM(
        id = 4,
        title = "Recherche d’articles",
        description = "En tant que utilisateur, je veux voir des articles",
        done = true,
        priority = HighPriority,
        date = "13-02-2025",
        time = "12:00"
    )
)
class StoryException(message: String, e: Exception) : Throwable(message)

fun getStories() = flow {
    emit(storiesList)
}

fun addOrUpdateStory(story: StoryVM) {
    if (story.title.isBlank() || story.description?.isBlank() != false || story.date.isBlank() || story.time.isBlank() || story.priority == null) {
        throw StoryException("Unable to save story: Title cannot be empty", Exception("Title cannot be empty"))
    }

    val existingStory = storiesList.find { it.id == story.id }
    existingStory?.let {
        storiesList.remove(it)
    }
    storiesList.add(story)
}

fun findStory(storyId: Int) : StoryVM? {
    return storiesList.find { it .id == storyId }
}
