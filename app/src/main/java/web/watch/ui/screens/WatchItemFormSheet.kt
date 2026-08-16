package web.watch.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import web.watch.data.WatchItem
import web.watch.network.CompatibilityResult
import web.watch.network.WebFetcher

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WatchItemFormSheet(
    existingItem: WatchItem? = null,
    onDismiss: () -> Unit,
    onSave: (title: String, url: String, frequencyMinutes: Int, highPriority: Boolean) -> Unit
) {
    var title by remember { mutableStateOf(existingItem?.title ?: "") }
    var url by remember { mutableStateOf(existingItem?.url ?: "") }
    var frequencyText by remember { mutableStateOf(existingItem?.checkFrequencyMinutes?.toString() ?: "15") }
    var notificationPriority by remember { mutableStateOf(existingItem?.notificationPriority ?: false) }

    var compatibilityState by remember { mutableStateOf<CompatibilityResult?>(null) }
    var isCheckingCompatibility by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    val modalSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = modalSheetState,
        modifier = Modifier.fillMaxHeight(0.7f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 10.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = if (existingItem == null) "Add Website to Watch" else "Edit Watch Item",
                style = MaterialTheme.typography.headlineSmall
            )

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title") },
                placeholder = { Text("e.g. Price Tracker") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = url,
                onValueChange = { url = it },
                label = { Text("URL") },
                placeholder = { Text("https://example.com") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = frequencyText,
                onValueChange = { if (it.all { char -> char.isDigit() }) frequencyText = it },
                label = { Text("Check Frequency (Minutes)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Notification Priority", style = MaterialTheme.typography.titleMedium)
                    Text("Get instant alerts bypassing mute settings", style = MaterialTheme.typography.bodySmall)
                }
                Switch(
                    checked = notificationPriority,
                    onCheckedChange = { notificationPriority = it }
                )
            }

            OutlinedButton(
                onClick = {
                    if (url.isNotBlank()) {
                        isCheckingCompatibility = true
                        compatibilityState = null
                        coroutineScope.launch {
                            val res = WebFetcher.checkCompatibility(url)
                            compatibilityState = res
                            isCheckingCompatibility = false
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isCheckingCompatibility && url.isNotBlank()
            ) {
                if (isCheckingCompatibility) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Checking compatibility...")
                } else {
                    Text("Compatibility Check")
                }
            }

            compatibilityState?.let { res ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (res.isCompatible) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = res.message,
                        modifier = Modifier.padding(12.dp),
                        color = if (res.isCompatible) Color(0xFF2E7D32) else Color(0xFFC62828),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        val freq = frequencyText.toIntOrNull() ?: 15
                        if (title.isNotBlank() && url.isNotBlank()) {
                            onSave(title, url, freq, notificationPriority)
                        }
                    },
                    enabled = title.isNotBlank() && url.isNotBlank()
                ) {
                    Text("Start Watching")
                }
            }
        }
    }
}
