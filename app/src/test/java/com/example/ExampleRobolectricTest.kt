package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.GenuinePyqBank
import com.example.data.local.JeeDatabase
import com.example.data.repository.JeeRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("JEE Mock Test", appName)
  }

  @Test
  fun `seed genuine pyqs into database successfully`() = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val db = JeeDatabase.getDatabase(context)
    val repo = JeeRepository(context, db)

    repo.initializeDatabaseIfNeeded()
    val count = db.questionDao().getGenuinePyqCount()
    assertTrue("Should seed genuine PYQs into Room DB", count >= GenuinePyqBank.questions.size)

    val pyqs = repo.getGenuinePyqTest(com.example.data.model.ExamPattern.JEE_MAIN)
    assertTrue("Should fetch genuine PYQs", pyqs.isNotEmpty())
    assertTrue("All fetched questions must be marked genuine", pyqs.all { it.isGenuinePyq })
  }
}
