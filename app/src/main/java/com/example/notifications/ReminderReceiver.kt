package com.example.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class ReminderReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_TASK_REMINDER = "com.example.ACTION_TASK_REMINDER"
        const val ACTION_JOURNAL_REMINDER = "com.example.ACTION_JOURNAL_REMINDER"
        const val EXTRA_TASK_ID = "extra_task_id"
        const val EXTRA_TITLE = "extra_title"
    }

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ACTION_TASK_REMINDER -> {
                val taskId = intent.getLongExtra(EXTRA_TASK_ID, 0L)
                val title = intent.getStringExtra(EXTRA_TITLE) ?: "Task Reminder"
                NotificationHelper.showTaskNotification(context, taskId, title, "")
            }
            ACTION_JOURNAL_REMINDER -> {
                NotificationHelper.showJournalNotification(context)
            }
        }
    }
}
