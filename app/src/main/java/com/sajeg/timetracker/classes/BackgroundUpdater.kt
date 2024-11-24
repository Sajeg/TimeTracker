package com.sajeg.timetracker.classes

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters

class BackgroundUpdater(appContext: Context, workerParams: WorkerParameters) :
    Worker(appContext, workerParams) {

    override fun doWork(): Result {
        try {
            UsageStatsFetcher(applicationContext).updateDatabase {}
        } catch (e: Exception) {
            return Result.failure()
        }
        return Result.success()
    }
}