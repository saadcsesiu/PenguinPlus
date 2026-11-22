package com.example

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.DownloadRepository
import com.example.data.UserPreferences
import com.example.model.DownloadHistoryItem
import com.example.model.PlatformType
import com.example.model.VideoResolution
import com.example.viewmodel.MainViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Penguin Plus", appName)
  }

  @Test
  fun `verify secret upgrade logic and persistence`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val prefs = UserPreferences(context)

    assertFalse(prefs.subscriptionState.value.isPro)

    prefs.activateLifetimePro(email = "tester@example.org", isSecret = true)

    val updatedState = prefs.subscriptionState.value
    assertTrue(updatedState.isPro)
    assertEquals("Active Lifetime User", updatedState.statusLabel)
    assertEquals("tester@example.org", updatedState.email)
  }

  @Test
  fun `verify platform detection and resolution lock state`() {
    assertEquals(PlatformType.YOUTUBE, PlatformType.detectPlatform("https://youtube.com/shorts/xyz"))
    assertEquals(PlatformType.INSTAGRAM, PlatformType.detectPlatform("https://instagram.com/reel/123"))
    assertEquals(PlatformType.TIKTOK, PlatformType.detectPlatform("https://tiktok.com/@user/video/456"))

    assertFalse(VideoResolution.RES_360P.isProRequired)
    assertFalse(VideoResolution.RES_720P.isProRequired)

    assertTrue(VideoResolution.RES_1080P.isProRequired)
    assertTrue(VideoResolution.RES_4K.isProRequired)
  }

  @Test
  fun `verify download history and redownload capability`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val testDispatcher = UnconfinedTestDispatcher()
    val testScope = TestScope(testDispatcher)
    val repo = DownloadRepository(context, testScope)

    val initialHistory = repo.downloadHistory.value
    assertTrue("Initial history should not be empty", initialHistory.isNotEmpty())

    // Redownload an item
    val firstItem = initialHistory.first()
    repo.redownloadHistoryItem(firstItem)

    // Verify active downloads contains redownloaded item
    val active = repo.activeDownloads.value
    assertTrue("Active downloads should contain redownloaded item", active.any { it.title == firstItem.title })

    // Test delete single history item
    repo.deleteHistoryItem(firstItem.id)
    assertFalse(repo.downloadHistory.value.any { it.id == firstItem.id })

    // Test clear all history
    repo.clearAllHistory()
    assertTrue(repo.downloadHistory.value.isEmpty())
  }

  @Test
  fun `verify backend url validation rejects cleartext http and placeholder values`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val prefs = UserPreferences(context)

    prefs.appsScriptUrl = "invalid-token"
    assertFalse(prefs.isBackendConfigured())

    prefs.appsScriptUrl = "http://script.google.com/macros/s/AKfycbxRealDeploymentId123/exec"
    assertFalse(prefs.isBackendConfigured())

    prefs.appsScriptUrl = "https://script.google.com/macros/s/YOUR_SCRIPT_ID_HERE/exec"
    assertFalse(prefs.isBackendConfigured())

    prefs.appsScriptUrl = "https://script.google.com/macros/s/AKfycbxRealDeploymentId123/exec"
    assertTrue(prefs.isBackendConfigured())
  }

  @Test
  fun `verify url parsing progress triggers on valid url and resets on clear`() {
    val app = ApplicationProvider.getApplicationContext<Application>()
    val viewModel = MainViewModel(app)

    assertFalse(viewModel.isParsingUrl.value)

    viewModel.onUrlChanged("https://youtube.com/watch?v=dQw4w9WgXcQ")
    assertTrue(viewModel.isParsingUrl.value)

    viewModel.clearUrl()
    assertFalse(viewModel.isParsingUrl.value)
  }
}
