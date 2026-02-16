package com.seretail.inventarios.ui.rfid

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seretail.inventarios.data.local.entity.ActivoFijoSessionEntity
import com.seretail.inventarios.data.local.entity.RfidTagEntity
import com.seretail.inventarios.data.repository.ActivoFijoRepository
import com.seretail.inventarios.data.repository.RfidRepository
import com.seretail.inventarios.rfid.RfidState
import com.seretail.inventarios.util.FeedbackManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RfidCaptureUiState(
    val rfidState: RfidState = RfidState.Disconnected,
    val tags: List<RfidTagEntity> = emptyList(),
    val sessions: List<ActivoFijoSessionEntity> = emptyList(),
    val selectedSessionId: Long? = null,
    val power: Int = 20,
    val totalCount: Int = 0,
    val matchedCount: Int = 0,
    val showMatchedOnly: Boolean = false,
    val message: String? = null,
)

@HiltViewModel
class RfidCaptureViewModel @Inject constructor(
    private val rfidRepository: RfidRepository,
    private val activoFijoRepository: ActivoFijoRepository,
    private val feedbackManager: FeedbackManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow(RfidCaptureUiState())
    val uiState: StateFlow<RfidCaptureUiState> = _uiState

    init {
        observeRfidState()
        observeIncomingTags()
        loadSessions()
    }

    private fun observeRfidState() {
        viewModelScope.launch {
            rfidRepository.rfidState.collectLatest { state ->
                _uiState.value = _uiState.value.copy(rfidState = state)
            }
        }
    }

    private fun observeIncomingTags() {
        viewModelScope.launch {
            rfidRepository.incomingTags.collect { readTag ->
                val sessionId = _uiState.value.selectedSessionId ?: return@collect
                val epc = readTag.epcId ?: return@collect
                feedbackManager.playDecode()
                val saved = rfidRepository.saveOrUpdateTag(sessionId, epc, readTag.rssi)
                rfidRepository.matchTagToAsset(saved)
                updateCounts(sessionId)
            }
        }
    }

    private fun loadSessions() {
        viewModelScope.launch {
            activoFijoRepository.observeSessions().collect { sessions ->
                _uiState.value = _uiState.value.copy(sessions = sessions)
            }
        }
    }

    fun selectSession(sessionId: Long) {
        _uiState.value = _uiState.value.copy(selectedSessionId = sessionId)
        viewModelScope.launch {
            rfidRepository.observeTags(sessionId).collect { tags ->
                _uiState.value = _uiState.value.copy(tags = tags)
            }
        }
        viewModelScope.launch { updateCounts(sessionId) }
    }

    fun connect() = rfidRepository.connect()
    fun disconnect() = rfidRepository.disconnect()

    fun startScan() {
        if (_uiState.value.selectedSessionId == null) {
            _uiState.value = _uiState.value.copy(message = "Selecciona una sesión primero")
            return
        }
        rfidRepository.startInventory()
    }

    fun stopScan() = rfidRepository.stopInventory()

    fun setPower(power: Int) {
        rfidRepository.setPower(power)
        _uiState.value = _uiState.value.copy(power = power)
    }

    fun toggleFilter() {
        _uiState.value = _uiState.value.copy(showMatchedOnly = !_uiState.value.showMatchedOnly)
    }

    fun getFilteredTags(): List<RfidTagEntity> {
        val state = _uiState.value
        return if (state.showMatchedOnly) state.tags.filter { it.matched }
        else state.tags
    }

    fun clearSession() {
        val sessionId = _uiState.value.selectedSessionId ?: return
        viewModelScope.launch {
            rfidRepository.clearSession(sessionId)
            updateCounts(sessionId)
        }
    }

    private suspend fun updateCounts(sessionId: Long) {
        val total = rfidRepository.countTags(sessionId)
        val matched = rfidRepository.countMatched(sessionId)
        _uiState.value = _uiState.value.copy(totalCount = total, matchedCount = matched)
    }

    fun clearMessage() { _uiState.value = _uiState.value.copy(message = null) }
}
