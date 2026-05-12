package com.example.thuctaptotnghiep.ui.chat // Đổi package theo project của bạn nếu cần

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.thuctaptotnghiep.data.model.Conversation
import com.example.thuctaptotnghiep.data.repository.ChatRepository
import com.example.thuctaptotnghiep.utils.UserManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InboxViewModel @Inject constructor(
    private val repository: ChatRepository
) : ViewModel() {

    private val _conversations = MutableStateFlow<List<Conversation>>(emptyList())
    val conversations: StateFlow<List<Conversation>> = _conversations.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Lấy Firebase UID hiện tại để biết ai là "mình", ai là "người kia" trong phòng chat
    val currentUserId = UserManager.userProfile.value?.id ?: ""

    init {
        fetchConversations()
    }

    fun fetchConversations() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = repository.getMyConversations()
                if (response.isSuccessful && response.body()?.success == true) {
                    _conversations.value = response.body()?.data ?: emptyList()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }
}