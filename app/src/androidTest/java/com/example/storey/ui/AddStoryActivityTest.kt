package com.example.storey.ui

import android.app.Instrumentation
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.example.storey.R
import com.example.storey.ui.addstory.AddStoryActivity
import com.example.storey.util.EspressoIdlingResource
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class AddStoryActivityTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(AddStoryActivity::class.java)

    @Before
    fun setUp() {
        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
        Intents.init()
    }

    @After
    fun tearDown() {
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
    }

    @Test
    fun testUploadStorySuccess() {
        // Mock data: set image and description
        Intents.intending(IntentMatchers.hasAction(Intent.ACTION_GET_CONTENT)).respondWith(
            Instrumentation.ActivityResult(
                AppCompatActivity.RESULT_OK,
                Intent().setData(Uri.parse("android.resource://com.example.storey/drawable/placeholder_account"))
            )
        )
        onView(withId(R.id.btn_gallery)).perform(click())

        // Input Description
        onView(withId(R.id.ed_desc)).perform(typeText("description"))

        // Click Upload button
        onView(withId(R.id.btn_upload)).perform(click())

        // Verify destroyed Activity
        runBlocking {
            delay(2000)
        }
        assertTrue(activityRule.scenario.state.isAtLeast(androidx.lifecycle.Lifecycle.State.DESTROYED))
    }
}
