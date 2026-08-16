package web.watch.ui.screens

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import web.watch.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    isDarkMode: Boolean,
    onDarkModeChange: (Boolean) -> Unit,
    isAmoledMode: Boolean,
    onAmoledModeChange: (Boolean) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Appearance", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Dark Mode", style = MaterialTheme.typography.bodyLarge)
                    Text("Enable dark theme UI", style = MaterialTheme.typography.bodySmall)
                }
                Switch(checked = isDarkMode, onCheckedChange = onDarkModeChange)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("AMOLED Mode", style = MaterialTheme.typography.bodyLarge)
                    Text("Pure pitch black background in dark mode", style = MaterialTheme.typography.bodySmall)
                }
                Switch(checked = isAmoledMode, onCheckedChange = onAmoledModeChange)
            }

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            Text("Permission Manager", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)

            Button(
                onClick = {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.parse("package:${context.packageName}")
                    }
                    context.startActivity(intent)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Manage App Permissions & Battery Settings")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    versionName: String,
    onBack: () -> Unit
) {
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("About") },
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
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_eye_globe),
                contentDescription = "WebWatch Logo",
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
            )

            Text("WebWatch", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text("Version: $versionName", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

            Text(
                "WebWatch monitors websites in the background and notifies you immediately whenever content changes or updates are detected.",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Developer Information", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Text("Abdullah Bari Asif", style = MaterialTheme.typography.bodyMedium)
                    Text("From Bangladesh", style = MaterialTheme.typography.bodySmall)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text("Social Links", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Facebook
                IconButton(onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://fb.com/abdullahbariasif"))
                    context.startActivity(intent)
                }) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Facebook",
                        tint = Color(0xFF1877F2),
                        modifier = Modifier.size(32.dp)
                    )
                }

                // WhatsApp
                IconButton(onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/+8801538310838"))
                    context.startActivity(intent)
                }) {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = "WhatsApp",
                        tint = Color(0xFF25D366),
                        modifier = Modifier.size(32.dp)
                    )
                }

                // Email
                IconButton(onClick = {
                    val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:abdullah.bari.2028@gmail.com"))
                    context.startActivity(intent)
                }) {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = "Email",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreditsScreen(
    onBack: () -> Unit
) {
    val openSourceLibs = listOf(
        Triple("Jetpack Compose", "Apache License 2.0", "Android's modern toolkit for building native UI."),
        Triple("Kotlin / Coroutines", "Apache License 2.0", "JetBrains Kotlin programming language and async framework."),
        Triple("Room Database", "Apache License 2.0", "Android persistence library providing an abstraction layer over SQLite."),
        Triple("WorkManager", "Apache License 2.0", "Android background task scheduler for deferred and periodic execution."),
        Triple("Ktor Client", "Apache License 2.0", "Asynchronous HTTP client framework for Kotlin."),
        Triple("java-diff-utils", "Apache License 2.0", "Generates diffs and comparisons between strings and HTML files.")
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Credits & Licenses") },
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
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Open Source Libraries Used", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)

            openSourceLibs.forEach { (name, license, desc) ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = MaterialTheme.shapes.extraSmall
                            ) {
                                Text(
                                    license,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                        Text(desc, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}
