package web.watch.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.difflib.DiffUtils
import com.github.difflib.patch.AbstractDelta
import com.github.difflib.patch.DeltaType
import web.watch.data.HistoryRecord
import web.watch.ui.theme.DiffAddBackground
import web.watch.ui.theme.DiffAddBackgroundDark
import web.watch.ui.theme.DiffAddText
import web.watch.ui.theme.DiffAddTextDark
import web.watch.ui.theme.DiffDeleteBackground
import web.watch.ui.theme.DiffDeleteBackgroundDark
import web.watch.ui.theme.DiffDeleteText
import web.watch.ui.theme.DiffDeleteTextDark
import web.watch.util.TimeUtils

data class DiffLine(
    val type: DeltaType,
    val text: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiffViewerScreen(
    record: HistoryRecord,
    url: String,
    onBack: () -> Unit
) {
    val isDark = isSystemInDarkTheme()

    val diffLines = remember(record) {
        val oldLines = record.oldContent.lines()
        val newLines = record.newContent.lines()
        val patch = DiffUtils.diff(oldLines, newLines)

        val result = mutableListOf<DiffLine>()

        var oldIndex = 0
        var newIndex = 0

        for (delta in patch.deltas) {
            val sourcePos = delta.source.position
            // Lines before delta
            while (oldIndex < sourcePos) {
                result.add(DiffLine(DeltaType.EQUAL, oldLines[oldIndex]))
                oldIndex++
                newIndex++
            }

            when (delta.type) {
                DeltaType.CHANGE -> {
                    for (line in delta.source.lines) {
                        result.add(DiffLine(DeltaType.DELETE, "- $line"))
                    }
                    for (line in delta.target.lines) {
                        result.add(DiffLine(DeltaType.INSERT, "+ $line"))
                    }
                    oldIndex += delta.source.lines.size
                    newIndex += delta.target.lines.size
                }
                DeltaType.DELETE -> {
                    for (line in delta.source.lines) {
                        result.add(DiffLine(DeltaType.DELETE, "- $line"))
                    }
                    oldIndex += delta.source.lines.size
                }
                DeltaType.INSERT -> {
                    for (line in delta.target.lines) {
                        result.add(DiffLine(DeltaType.INSERT, "+ $line"))
                    }
                    newIndex += delta.target.lines.size
                }
                else -> {}
            }
        }

        while (oldIndex < oldLines.size) {
            result.add(DiffLine(DeltaType.EQUAL, oldLines[oldIndex]))
            oldIndex++
        }

        result
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = TimeUtils.formatExactDateTime(record.timestamp),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = url,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                diffLines.forEach { line ->
                    val (bgColor, textColor) = when (line.type) {
                        DeltaType.INSERT -> if (isDark) Pair(DiffAddBackgroundDark, DiffAddTextDark) else Pair(DiffAddBackground, DiffAddText)
                        DeltaType.DELETE -> if (isDark) Pair(DiffDeleteBackgroundDark, DiffDeleteTextDark) else Pair(DiffDeleteBackground, DiffDeleteText)
                        else -> Pair(Color.Unspecified, MaterialTheme.colorScheme.onBackground)
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(bgColor)
                            .padding(horizontal = 12.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = line.text,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            color = textColor
                        )
                    }
                }
            }
        }
    }
}
