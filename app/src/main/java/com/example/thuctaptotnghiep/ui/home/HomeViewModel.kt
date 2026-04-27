package com.example.thuctaptotnghiep.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.thuctaptotnghiep.data.model.Document
import com.example.thuctaptotnghiep.data.repository.DocumentRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: DocumentRepository
) : ViewModel() {

    // LẤY TÊN THẬT TỪ FIREBASE ĐỂ TRUYỀN SANG GIAO DIỆN
    private val currentUser = FirebaseAuth.getInstance().currentUser
    val userName = currentUser?.displayName ?: "Người dùng"

    // 1. Cải tiến: Khai báo 3 "Kho chứa" dữ liệu riêng biệt cho 3 section
    private val _latestDocs = MutableStateFlow<List<Document>>(emptyList())
    val latestDocs: StateFlow<List<Document>> = _latestDocs.asStateFlow()

    private val _popularDocs = MutableStateFlow<List<Document>>(emptyList())
    val popularDocs: StateFlow<List<Document>> = _popularDocs.asStateFlow()

    private val _recommendedDocs = MutableStateFlow<List<Document>>(emptyList())
    val recommendedDocs: StateFlow<List<Document>> = _recommendedDocs.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        fetchDocuments()
    }

    fun fetchDocuments() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val allDocs = repository.getAllDocuments()
                distributeDocuments(allDocs) // Gọi hàm phân bổ dữ liệu
            } catch (e: Exception) {
                _errorMessage.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun refreshDocuments() {
        viewModelScope.launch {
            _isRefreshing.value = true
            _errorMessage.value = null
            try {
                val allDocs = repository.getAllDocuments()
                distributeDocuments(allDocs) // Gọi hàm phân bổ dữ liệu
            } catch (e: Exception) {
                _errorMessage.value = e.message
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    // 2. Cải tiến: Hàm xử lý logic phân loại tài liệu
    private fun distributeDocuments(allDocs: List<Document>) {
        if (allDocs.isEmpty()) return

        // MỤC 1: Tài liệu mới nhất (Giả sử lấy 5 bài cuối cùng hoặc sort theo ngày)
        // Nếu Model Document có trường ngày (vd: createdAt), bạn dùng:
        // _latestDocs.value = allDocs.sortedByDescending { it.createdAt }.take(5)
        _latestDocs.value = allDocs.reversed().take(5)

        // MỤC 2: Tài liệu phổ biến (Giả sử sort theo lượt xem)
        // Nếu Model Document có trường views, bạn dùng:
        // _popularDocs.value = allDocs.sortedByDescending { it.views }.take(5)
        // Tạm thời mình dùng shuffled() ở đây để tránh lỗi compile, bạn nhớ đổi lại nhé!
        _popularDocs.value = allDocs.shuffled().take(5)

        // MỤC 3: Dành riêng cho bạn (Gợi ý ngẫu nhiên nhưng KHÔNG trùng với 2 mục trên)
        val excludeIds = (_latestDocs.value + _popularDocs.value).map { it.id }.toSet()
        val remainingDocs = allDocs.filterNot { it.id in excludeIds }

        // Nếu số lượng còn lại quá ít, lấy thêm từ danh sách gốc để đảm bảo UI không bị trống
        _recommendedDocs.value = if (remainingDocs.size >= 5) {
            remainingDocs.shuffled().take(5)
        } else {
            (remainingDocs + allDocs).distinctBy { it.id }.take(5)
        }
    }
}