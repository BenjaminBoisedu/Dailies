package com.example.daily.domain.useCases

import com.example.daily.FakeDatabase
import com.example.daily.domain.model.Daily
import com.example.daily.utils.DailyException
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class DeleteDailyUseCaseTest {

    private lateinit var fakeDailiesDao: FakeDatabase
    private lateinit var deleteDailyUseCase: DeleteDailyUseCase

    @Before
    fun setUp() {
        fakeDailiesDao = FakeDatabase()
        deleteDailyUseCase = DeleteDailyUseCase(fakeDailiesDao)
    }

    @Test
    fun `test delete daily with null daily throws exception`() {
        try {
            runBlocking {
                deleteDailyUseCase(null)
            }
            fail("DailyException attendue mais non lancée")
        } catch (_: DailyException) {
            // Test réussi, l'exception a été lancée
        }
    }

    @Test
    fun `test delete daily with null id throws exception`() {
        // Given
        val daily = Daily(
            id = null,
            title = "Test",
            description = "Test description",
            date = "2023-06-15",
            time = "14:30",
            done = false,
            priority = 0,
            latitude = null,
            longitude = null,
            locationName = null,
            recurringType = "",
            recurringDays = null,
            isRecurring = false,
            notificationTime = "30"
        )

        // When/Then
        try {
            runBlocking {
                deleteDailyUseCase(daily)
            }
            fail("DailyException attendue mais non lancée")
        } catch (_: DailyException) {
            // Test réussi, l'exception a été lancée
        }
    }

    @Test
    fun `test delete daily successful`() = runBlocking {
        // Given
        val daily = Daily(
            id = 1,
            title = "Test",
            description = "Test description",
            date = "2023-06-15",
            time = "14:30",
            done = false,
            priority = 0,
            latitude = null,
            longitude = null,
            locationName = null,
            recurringType = "",
            recurringDays = null,
            isRecurring = false,
            notificationTime = "30"
        )
        fakeDailiesDao.shouldReturnSuccessForDelete = true

        // When
        val result = deleteDailyUseCase(daily)

        // Then
        assertTrue(result)
        assertEquals(1, fakeDailiesDao.lastDeletedId)
    }

    @Test
    fun `test delete daily not found`() = runBlocking {
        // Given
        val daily = Daily(
            id = 1,
            title = "Test",
            description = "Test description",
            date = "2023-06-15",
            time = "14:30",
            done = false,
            priority = 0,
            latitude = null,
            longitude = null,
            locationName = null,
            recurringType = "",
            recurringDays = null,
            isRecurring = false,
            notificationTime = "30"
        )
        fakeDailiesDao.shouldReturnSuccessForDelete = false

        // When
        val result = deleteDailyUseCase(daily)

        // Then
        assertFalse(result)
        assertEquals(1, fakeDailiesDao.lastDeletedId)
    }

    @Test
    fun `test delete daily database error`() {
        // Given
        val daily = Daily(
            id = 1,
            title = "Test",
            description = "Test description",
            date = "2023-06-15",
            time = "14:30",
            done = false,
            priority = 0,
            latitude = null,
            longitude = null,
            locationName = null,
            recurringType = "",
            recurringDays = null,
            isRecurring = false,
            notificationTime = "30"
        )
        fakeDailiesDao.shouldThrowExceptionOnDelete = true

        // When/Then
        try {
            runBlocking {
                deleteDailyUseCase(daily)
            }
            fail("DailyException attendue mais non lancée")
        } catch (e: DailyException) {
            assertTrue(e.message?.contains("Unable to delete daily") == true)
            assertTrue(e.cause is RuntimeException)
        }
    }

}