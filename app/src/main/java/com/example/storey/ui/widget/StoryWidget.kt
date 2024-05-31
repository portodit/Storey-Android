package com.example.storey.ui.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.RemoteViews
import androidx.core.net.toUri
import com.example.storey.BuildConfig
import com.example.storey.R
import com.example.storey.data.model.ListStoryItem
import com.example.storey.ui.detail.DetailActivity


class StoryWidget : AppWidgetProvider() {
    companion object {
        private const val DETAIL_ACTION = "${BuildConfig.APPLICATION_ID}.DETAIL_ACTION"

        private fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val intent = Intent(context, StackWidgetService::class.java)
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            intent.data = intent.toUri(Intent.URI_INTENT_SCHEME).toUri()

            val views = RemoteViews(context.packageName, R.layout.story_widget)
            views.setRemoteAdapter(R.id.stack_view, intent)
            views.setEmptyView(R.id.stack_view, R.id.empty_view)

            val toastIntent = Intent(context, StoryWidget::class.java)
            toastIntent.action = DETAIL_ACTION
            toastIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)

            val toastPendingIntent = PendingIntent.getBroadcast(
                context, 0, toastIntent,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
                else 0
            )

            views.setPendingIntentTemplate(R.id.stack_view, toastPendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        if (intent.action != null) {
            if (intent.action == DETAIL_ACTION) {
                val story = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                    intent.getParcelableExtra(DetailActivity.EXTRA_STORY)
                } else {
                    intent.getParcelableExtra(DetailActivity.EXTRA_STORY, ListStoryItem::class.java)
                }
                val intentDetail = Intent(Intent.ACTION_VIEW)
                intentDetail.putExtra(DetailActivity.EXTRA_STORY, story)
                intentDetail.setClassName(
                    BuildConfig.APPLICATION_ID,
                    "${BuildConfig.APPLICATION_ID}.ui.detail.DetailActivity"
                )
                intentDetail.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(intentDetail)
            }
        }
        super.onReceive(context, intent)
    }
}
