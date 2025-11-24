package com.example.uniforlibrary.service

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

/**
 * NotificationWorker - Removido temporariamente
 * Worker para verificações em background será implementado futuramente
 */
class NotificationWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        // Será implementado quando necessário
        return Result.success()
    }
}
