package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.service.ApiWorkbenchRunner
import com.example.data.service.DatabaseWorkbenchEngine
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context matches app name`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Coding to AI", appName)
  }

  @Test
  fun `database workbench executes select queries`() = runBlocking {
    val dbEngine = DatabaseWorkbenchEngine()
    val res = dbEngine.executeQuery("SELECT * FROM tasks;")
    assertFalse(res.isError)
    assertTrue(res.columns.contains("title"))
    assertTrue(res.rows.isNotEmpty())
  }

  @Test
  fun `api workbench executes mock routes`() = runBlocking {
    val apiRunner = ApiWorkbenchRunner()
    val res = apiRunner.execute("GET", "/api/health", emptyMap(), "", 4100)
    assertEquals(200, res.statusCode)
    assertTrue(res.body.contains("coding-to-ai-sandbox"))
  }
}
