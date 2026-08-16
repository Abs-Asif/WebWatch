package web.watch.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import web.watch.data.AppDatabase
import web.watch.data.HistoryRecord
import web.watch.data.WatchItem

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = AppDatabase.getDatabase(application).watchDao()

    val watchItems: StateFlow<List<WatchItem>> = dao.getAllWatchItems()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedWatchItem = MutableStateFlow<WatchItem?>(null)
    val selectedWatchItem: StateFlow<WatchItem?> = _selectedWatchItem.asStateFlow()

    private val _historyRecords = MutableStateFlow<List<HistoryRecord>>(emptyList())
    val historyRecords: StateFlow<List<HistoryRecord>> = _historyRecords.asStateFlow()

    private val _selectedHistoryRecord = MutableStateFlow<HistoryRecord?>(null)
    val selectedHistoryRecord: StateFlow<HistoryRecord?> = _selectedHistoryRecord.asStateFlow()

    fun selectWatchItem(item: WatchItem?) {
        _selectedWatchItem.value = item
        if (item != null) {
            viewModelScope.launch {
                dao.getHistoryForWatchItem(item.id).collect { history ->
                    _historyRecords.value = history
                }
            }
        } else {
            _historyRecords.value = emptyList()
        }
    }

    fun selectHistoryRecord(record: HistoryRecord?) {
        _selectedHistoryRecord.value = record
    }

    fun addWatchItem(title: String, url: String, frequencyMinutes: Int, highPriority: Boolean) {
        viewModelScope.launch {
            val newItem = WatchItem(
                title = title,
                url = url,
                checkFrequencyMinutes = frequencyMinutes,
                notificationPriority = highPriority
            )
            dao.insertWatchItem(newItem)
        }
    }

    fun updateWatchItem(item: WatchItem) {
        viewModelScope.launch {
            dao.updateWatchItem(item)
            if (_selectedWatchItem.value?.id == item.id) {
                _selectedWatchItem.value = item
            }
        }
    }

    fun deleteWatchItem(item: WatchItem) {
        viewModelScope.launch {
            dao.deleteWatchItem(item)
            if (_selectedWatchItem.value?.id == item.id) {
                _selectedWatchItem.value = null
            }
        }
    }
}
