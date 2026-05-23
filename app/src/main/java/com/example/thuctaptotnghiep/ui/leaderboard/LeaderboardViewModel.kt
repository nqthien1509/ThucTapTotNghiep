package com.example.thuctaptotnghiep.ui.leaderboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.thuctaptotnghiep.data.model.Document
import com.example.thuctaptotnghiep.data.model.User
import com.example.thuctaptotnghiep.data.repository.DocumentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LeaderboardViewModel @Inject constructor(
    private val documentRepository: DocumentRepository
) : ViewModel() {

    // Danh sách Top Tài liệu
    private val _topDocuments = MutableStateFlow<List<Document>>(emptyList())
    val topDocuments: StateFlow<List<Document>> = _topDocuments.asStateFlow()

    // Danh sách Top Người đóng góp
    private val _topContributors = MutableStateFlow<List<User>>(emptyList())
    val topContributors: StateFlow<List<User>> = _topContributors.asStateFlow()

    // Trạng thái loading
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        // Tự động load dữ liệu ngay khi vào màn hình
        fetchLeaderboards()
    }

    fun fetchLeaderboards() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // 1. Gọi API lấy Top Tài liệu
                val docResponse = documentRepository.getTopDocuments()
                if (docResponse.isSuccessful) {
                    // Lấy mảng data từ BaseResponse
                    _topDocuments.value = docResponse.body()?.data ?: emptyList()
                }

                // 2. Gọi API lấy Top Người dùng
                val userResponse = documentRepository.getTopContributors()
                if (userResponse.isSuccessful) {
                    _topContributors.value = userResponse.body()?.data ?: emptyList()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }
}