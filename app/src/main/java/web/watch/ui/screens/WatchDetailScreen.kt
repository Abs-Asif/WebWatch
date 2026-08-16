package web.watch.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import web.watch.data.HistoryRecord
import web.watch.data.WatchItem
import web.watch.data.WatchStatus
import web.watch.util.TimeUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WatchDetailScreen(
    watchItem: WatchItem,
    historyList: List<HistoryRecord>,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onShowDiff: (HistoryRecord) -> Unit
) {
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(watchItem.title, maxLines = 1) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Tracker")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // URL with External link icon
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = watchItem.url,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f),
                    maxLines = 2
                )
                IconButton(onClick = {
                    val formattedUrl = if (!watchItem.url.startsWith("http://") && !watchItem.url.startsWith("https://")) {
                        "https://${watchItem.url}"
                    } else watchItem.url
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(formattedUrl))
                    context.startActivity(intent)
                }) {
                    Icon(Icons.Default.Share, contentDescription = "Open in browser")
                }
            }

            // Current Status Box
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Current status", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        StatusTag(status = if (watchItem.isPaused) WatchStatus.PAUSED else watchItem.lastStatus)
                        Text(
                            text = "${TimeUtils.formatRelativeTime(watchItem.lastCheckTime)} (every ${watchItem.checkFrequencyMinutes} minutes)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Text(
                "Change history",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp)
            )

            if (historyList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No recorded changes yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    itemsIndexed(historyList, key = { _, item -> item.id }) { index, record ->
                        HistoryStepItem(
                            record = record,
                            isFirst = index == 0,
                            isLast = index == historyList.size - 1,
                            onShowDiff = { onShowDiff(record) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HistoryStepItem(
    record: HistoryRecord,
    isFirst: Boolean,
    isLast: Boolean,
    onShowDiff: () -> Unit
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val lineColor = MaterialTheme.colorScheme.outlineVariant

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        // Vertical Timeline line and circle stop
        Box(
            modifier = Modifier
                .width(32.dp)
                .height(110.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val circleCenterY = 20.dp.toPx()
                val radius = 6.dp.toPx()

                if (!isFirst) {
                    drawLine(
                        color = lineColor,
                        start = Offset(size.width / 2, 0f),
                        end = Offset(size.width / 2, circleCenterY - radius),
                        strokeWidth = 3f
                    )
                }
                if (!isLast) {
                    drawLine(
                        color = lineColor,
                        start = Offset(size.width / 2, circleCenterY + radius),
                        end = Offset(size.width / 2, size.height),
                        strokeWidth = 3f
                    )
                }

                drawCircle(
                    color = primaryColor,
                    radius = radius,
                    center = Offset(size.width / 2, circleCenterY)
                )
            }
        }

        // Card Content
        Card(
            modifier = Modifier
                .weight(1f)
                .padding(bottom = 12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = record.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (record.isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = TimeUtils.formatHistoryTime(record.timestamp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (record.errorMessage != null) {
                    Text(
                        text = record.errorMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                if (!record.isError) {
                    Button(
                        onClick = onShowDiff,
                        modifier = Modifier
                            .align(Alignment.End)
                            .padding(top = 4.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text("Show diff", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}
