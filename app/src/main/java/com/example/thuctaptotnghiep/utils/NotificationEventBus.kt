package com.example.thuctaptotnghiep.utils

import com.example.thuctaptotnghiep.data.model.AppNotification
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object NotificationEventBus {
    private val _events = MutableSharedFlow<AppNotification>()
    val events = _events.asSharedFlow()

    suspend fun emitNewNotification(notification: AppNotification) {
        _events.emit(notification)
    }
}