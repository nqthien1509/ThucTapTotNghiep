package com.example.thuctaptotnghiep.utils

import com.example.thuctaptotnghiep.BuildConfig
import io.socket.client.IO
import io.socket.client.Socket
import java.net.URISyntaxException

object SocketHandler {
    private var mSocket: Socket? = null

    @Synchronized
    fun setSocket() {
        try {
            // Tận dụng BASE_URL từ BuildConfig mà bạn đã cấu hình rất chuẩn!
            mSocket = IO.socket(BuildConfig.BASE_URL)
        } catch (e: URISyntaxException) {
            e.printStackTrace()
        }
    }

    @Synchronized
    fun getSocket(): Socket? {
        return mSocket
    }

    @Synchronized
    fun establishConnection() {
        mSocket?.connect()
    }

    @Synchronized
    fun closeConnection() {
        mSocket?.disconnect()
    }
}