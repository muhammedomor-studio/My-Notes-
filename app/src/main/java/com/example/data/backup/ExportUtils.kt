package com.example.data.backup

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.example.data.local.entity.JournalEntity
import com.example.data.local.entity.NoteEntity
import com.example.data.local.entity.TaskEntity
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ExportUtils {

    fun shareText(context: Context, title: String, content: String) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, title)
            putExtra(Intent.EXTRA_TEXT, "$title\n\n$content")
        }
        context.startActivity(Intent.createChooser(shareIntent, "Share via"))
    }

    fun exportNoteAsTxt(context: Context, note: NoteEntity): File {
        val fileName = "Note_${sanitizeFileName(note.title)}_${System.currentTimeMillis()}.txt"
        val file = File(context.cacheDir, fileName)
        val formattedDate = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(note.updatedAt))
        
        file.writeText(
            """
            Title: ${note.title}
            Date: $formattedDate
            Tags: ${note.tags}
            ----------------------------------------
            ${note.content}
            """.trimIndent()
        )
        return file
    }

    fun exportNoteAsPdf(context: Context, note: NoteEntity): File {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        val titlePaint = Paint().apply {
            color = Color.rgb(24, 32, 51)
            textSize = 20f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val metaPaint = Paint().apply {
            color = Color.rgb(123, 132, 153)
            textSize = 11f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            isAntiAlias = true
        }

        val bodyPaint = Paint().apply {
            color = Color.rgb(40, 50, 70)
            textSize = 13f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            isAntiAlias = true
        }

        val linePaint = Paint().apply {
            color = Color.rgb(231, 234, 243)
            strokeWidth = 1.5f
        }

        val dateStr = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()).format(Date(note.updatedAt))

        var y = 60f
        canvas.drawText(note.title.ifBlank { "Untitled Note" }, 40f, y, titlePaint)
        y += 24f
        canvas.drawText("Updated: $dateStr ${if (note.tags.isNotBlank()) " | Tags: ${note.tags}" else ""}", 40f, y, metaPaint)
        y += 18f
        canvas.drawLine(40f, y, 555f, y, linePaint)
        y += 30f

        val lines = note.content.split("\n")
        for (line in lines) {
            val words = line.split(" ")
            var currentLine = ""
            for (word in words) {
                val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
                if (bodyPaint.measureText(testLine) > 515f) {
                    canvas.drawText(currentLine, 40f, y, bodyPaint)
                    y += 20f
                    currentLine = word
                    if (y > 800f) break
                } else {
                    currentLine = testLine
                }
            }
            if (currentLine.isNotEmpty()) {
                canvas.drawText(currentLine, 40f, y, bodyPaint)
                y += 20f
            }
            if (y > 800f) break
        }

        document.finishPage(page)

        val fileName = "Note_${sanitizeFileName(note.title)}_${System.currentTimeMillis()}.pdf"
        val file = File(context.cacheDir, fileName)
        FileOutputStream(file).use { out ->
            document.writeTo(out)
        }
        document.close()
        return file
    }

    fun exportJournalAsTxt(context: Context, journal: JournalEntity): File {
        val fileName = "Journal_${journal.entryDate}_${System.currentTimeMillis()}.txt"
        val file = File(context.cacheDir, fileName)
        file.writeText(
            """
            Date: ${journal.entryDate}
            Title: ${journal.title}
            Mood: ${journal.mood ?: "Not specified"}
            Tags: ${journal.tags}
            ----------------------------------------
            ${journal.content}
            """.trimIndent()
        )
        return file
    }

    fun exportJournalAsPdf(context: Context, journal: JournalEntity): File {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        val titlePaint = Paint().apply {
            color = Color.rgb(24, 32, 51)
            textSize = 20f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val metaPaint = Paint().apply {
            color = Color.rgb(108, 99, 255)
            textSize = 12f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            isAntiAlias = true
        }

        val bodyPaint = Paint().apply {
            color = Color.rgb(40, 50, 70)
            textSize = 13f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            isAntiAlias = true
        }

        var y = 60f
        canvas.drawText(journal.title.ifBlank { "Diary Entry" }, 40f, y, titlePaint)
        y += 24f
        canvas.drawText("Date: ${journal.entryDate}  |  Mood: ${journal.mood ?: "Neutral"}", 40f, y, metaPaint)
        y += 24f

        val lines = journal.content.split("\n")
        for (line in lines) {
            val words = line.split(" ")
            var currentLine = ""
            for (word in words) {
                val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
                if (bodyPaint.measureText(testLine) > 515f) {
                    canvas.drawText(currentLine, 40f, y, bodyPaint)
                    y += 20f
                    currentLine = word
                    if (y > 800f) break
                } else {
                    currentLine = testLine
                }
            }
            if (currentLine.isNotEmpty()) {
                canvas.drawText(currentLine, 40f, y, bodyPaint)
                y += 20f
            }
            if (y > 800f) break
        }

        document.finishPage(page)
        val fileName = "Journal_${journal.entryDate}_${System.currentTimeMillis()}.pdf"
        val file = File(context.cacheDir, fileName)
        FileOutputStream(file).use { out ->
            document.writeTo(out)
        }
        document.close()
        return file
    }

    fun exportTasksAsTxt(context: Context, tasks: List<TaskEntity>): File {
        val fileName = "Tasks_Export_${System.currentTimeMillis()}.txt"
        val file = File(context.cacheDir, fileName)
        val sb = StringBuilder()
        sb.append("MY NOTES - TASK LIST\n")
        sb.append("Exported: ${SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())}\n")
        sb.append("========================================\n\n")

        for (task in tasks) {
            val status = if (task.isCompleted) "[✓] COMPLETED" else "[ ] PENDING"
            sb.append("$status: ${task.title}\n")
            if (task.description.isNotBlank()) {
                sb.append("  Note: ${task.description}\n")
            }
            sb.append("  Priority: ${task.priority}\n")
            if (task.dueDate != null) {
                val d = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(task.dueDate))
                sb.append("  Due: $d ${task.dueTime ?: ""}\n")
            }
            sb.append("\n")
        }

        file.writeText(sb.toString())
        return file
    }

    fun shareFile(context: Context, file: File, mimeType: String) {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share file"))
    }

    private fun sanitizeFileName(name: String): String {
        return name.replace(Regex("[^a-zA-Z0-9_]"), "_").take(30).ifBlank { "Untitled" }
    }
}
