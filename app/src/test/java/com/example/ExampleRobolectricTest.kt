package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.backup.BackupManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("My Notes", appName)
    }

    @Test
    fun `test backup validation with valid json`() {
        val json = """
            {
                "version": 1,
                "createdAt": 1700000000000,
                "appName": "My Notes",
                "notes": [
                    {
                        "id": 1,
                        "title": "Welcome Note",
                        "content": "This is a test note",
                        "color": "#FFFFFF",
                        "isPinned": true,
                        "isFavorite": false,
                        "tags": "test, welcome"
                    }
                ],
                "categories": [
                    {
                        "id": 1,
                        "name": "General",
                        "color": "#4361EE"
                    }
                ],
                "journals": [],
                "tasks": []
            }
        """.trimIndent()

        val result = BackupManager.parseAndValidateBackup(json)
        assertTrue(result.isSuccess)
        val data = result.getOrThrow()
        assertEquals(1, data.notes.size)
        assertEquals("Welcome Note", data.notes[0].title)
        assertEquals(1, data.categories.size)
    }
}
