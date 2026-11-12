package com.example.o2assignment.ui.card

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.Description

@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(
    val testDispatcher: TestDispatcher = UnconfinedTestDispatcher()
) : TestWatcher() {
    override fun starting(description: Description) {
        Dispatchers.setMain(testDispatcher)
    }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class CardViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun initialState_isCorrect() {
        val viewModel = CardViewModel()
        assertEquals(CardState.NEW, viewModel.state.value)
        assertEquals("", viewModel.codeState.value)
        assertEquals(UiState.EMPTY, viewModel.uiState.value)
    }

    @Test
    fun onScratch_whenStateIsNew_scratchesCardAfterDelay() =
        runTest(mainDispatcherRule.testDispatcher) {
            val viewModel = CardViewModel()

            viewModel.onScratch()

            //after onScratch, uiState should be LOADING
            assertEquals(UiState.LOADING, viewModel.uiState.value)
            assertEquals(CardState.NEW, viewModel.state.value)

            testScheduler.advanceUntilIdle()

            assertEquals(CardState.SCRATCHED, viewModel.state.value)
            assertTrue(viewModel.codeState.value.isNotEmpty())
            assertEquals(UiState.EMPTY, viewModel.uiState.value)
        }

    @Test
    fun onScratch_whenStateIsNotNew_doesNothing() = runTest(mainDispatcherRule.testDispatcher) {
        val viewModel = CardViewModel()

        //scratch and wait for completion
        viewModel.onScratch()
        testScheduler.advanceUntilIdle()
        val initialCode = viewModel.codeState.value
        assertEquals(CardState.SCRATCHED, viewModel.state.value)

        //try scratching when state is SCRATCHED
        viewModel.onScratch()
        testScheduler.advanceUntilIdle()

        //code and state remain unchanged
        assertEquals(initialCode, viewModel.codeState.value)
        assertEquals(CardState.SCRATCHED, viewModel.state.value)
    }

    @Test
    fun stopScratch_cancelsOngoingScratch() = runTest(mainDispatcherRule.testDispatcher) {
        val viewModel = CardViewModel()

        viewModel.onScratch()
        assertEquals(UiState.LOADING, viewModel.uiState.value)

        //stop scratch before delay completes
        viewModel.stopScratch()

        testScheduler.advanceUntilIdle()

        //should not have completed scratch
        assertEquals(CardState.NEW, viewModel.state.value)
        assertEquals("", viewModel.codeState.value)
        assertEquals(UiState.EMPTY, viewModel.uiState.value)
    }

    @Test
    fun onReset_resetsStateToNew() = runTest(mainDispatcherRule.testDispatcher) {
        val viewModel = CardViewModel()

        viewModel.onScratch()
        testScheduler.advanceUntilIdle()

        assertEquals(CardState.SCRATCHED, viewModel.state.value)
        assertNotEquals("", viewModel.codeState.value)

        viewModel.onReset()

        assertEquals(CardState.NEW, viewModel.state.value)
        assertEquals("", viewModel.codeState.value)
        assertEquals(UiState.EMPTY, viewModel.uiState.value)
    }

    @Test
    fun onDismiss_resetsUiStateToEmpty() = runTest(mainDispatcherRule.testDispatcher) {
        val viewModel = CardViewModel()

        viewModel.onScratch()
        assertEquals(UiState.LOADING, viewModel.uiState.value)

        viewModel.onDismiss()
        assertEquals(UiState.EMPTY, viewModel.uiState.value)
    }

    @Test
    fun onActivate_whenStateIsNotScratched_doesNothing() =
        runTest(mainDispatcherRule.testDispatcher) {
            val viewModel = CardViewModel()

            assertEquals(CardState.NEW, viewModel.state.value)
            viewModel.onActivate()
            testScheduler.advanceUntilIdle()

            assertEquals(CardState.NEW, viewModel.state.value)
            assertEquals(UiState.EMPTY, viewModel.uiState.value)
        }

    @Test
    fun onActivate_whenStateIsScratched_triggersLoadingState() =
        runTest(mainDispatcherRule.testDispatcher) {
            val viewModel = CardViewModel()

            viewModel.onScratch()
            testScheduler.advanceUntilIdle()
            assertEquals(CardState.SCRATCHED, viewModel.state.value)

            viewModel.onActivate()

            //triggers activation and sets UI state to LOADING
            assertEquals(UiState.LOADING, viewModel.uiState.value)
        }
}
