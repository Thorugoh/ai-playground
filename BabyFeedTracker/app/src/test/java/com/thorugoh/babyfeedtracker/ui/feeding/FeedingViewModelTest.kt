package com.thorugoh.babyfeedtracker.ui.feeding

import com.thorugoh.babyfeedtracker.model.FeedingSide
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FeedingViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `startFeeding should initialize session and start timer`() = runTest {
        val viewModel = FeedingViewModel()
        viewModel.startFeeding(FeedingSide.LEFT)

        val state = viewModel.uiState.value
        assertTrue(state.isActive)
        assertEquals(FeedingSide.LEFT, state.currentSide)
        assertNotNull(state.currentSession)
        assertEquals(1, state.currentSession?.intervals?.size)
        
        viewModel.stopFeeding()
    }

    @Test
    fun `switchSide should update intervals and current side`() = runTest {
        val viewModel = FeedingViewModel()
        viewModel.startFeeding(FeedingSide.LEFT)
        
        viewModel.switchSide()

        val state = viewModel.uiState.value
        assertEquals(FeedingSide.RIGHT, state.currentSide)
        assertEquals(2, state.currentSession?.intervals?.size)
        assertNotNull(state.currentSession?.intervals?.get(0)?.endTime)
        
        viewModel.stopFeeding()
    }

    @Test
    fun `stopFeeding should finish session and stop timer`() = runTest {
        val viewModel = FeedingViewModel()
        viewModel.startFeeding(FeedingSide.LEFT)
        viewModel.stopFeeding()

        val state = viewModel.uiState.value
        assertTrue(!state.isActive)
        assertNotNull(state.currentSession?.endTime)
        assertEquals(1, state.currentSession?.intervals?.size)
        assertNotNull(state.currentSession?.intervals?.get(0)?.endTime)
    }
}
