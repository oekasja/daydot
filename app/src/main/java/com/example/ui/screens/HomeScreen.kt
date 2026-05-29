package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.compose.runtime.rememberCoroutineScope
import com.example.ui.MainViewModel
import com.example.utils.captureComposableAsBitmap
import com.example.utils.shareBitmap
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import com.example.R

@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    navigateToEntry: (String) -> Unit,
    navigateToGrid: () -> Unit,
    navigateToTimeline: () -> Unit
) {
    val daysLived by viewModel.daysLived.collectAsState()
    val daysRemaining by viewModel.daysRemaining.collectAsState()
    val dob by viewModel.userDob.collectAsState()
    val allEntries by viewModel.allEntries.collectAsState()

    val todayIso = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
    val todayEntryExists = allEntries.any { it.date == todayIso }
    var showDialog by remember { mutableStateOf(false) }

    val totalDays = (daysLived ?: 0) + (daysRemaining ?: 0)
    val percentage = if (totalDays > 0) ((daysLived ?: 0).toFloat() / totalDays.toFloat()) else 0f
    
    val daysLivedStr = daysLived?.toString() ?: "..."
    val daysRemainingStr = daysRemaining?.toString() ?: "..."
    val percentageStr = if (totalDays > 0) String.format("%.1f%%", percentage * 100) else "..."
    
    val currentDateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM d, yyyy", java.util.Locale.getDefault())).uppercase(java.util.Locale.getDefault())

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(text = stringResource(id = R.string.modify_entry_title)) },
            text = { Text(text = stringResource(id = R.string.modify_entry_msg)) },
            confirmButton = {
                TextButton(onClick = {
                    showDialog = false
                    navigateToEntry(todayIso)
                }) {
                    Text(stringResource(id = R.string.modify_button))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text(stringResource(id = R.string.cancel_button))
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp)
    ) {
        // Top App Bar Area
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp, bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = currentDateStr,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 1.sp
                )
                Text(
                    text = stringResource(id = R.string.app_name),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                androidx.compose.material3.IconButton(
                    onClick = {
                        scope.launch {
                            val bitmap = captureComposableAsBitmap(context) {
                                StoryExportContent(daysLivedStr, daysRemainingStr, percentageStr, percentage.toFloat())
                            }
                            shareBitmap(context, bitmap)
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Share, 
                        contentDescription = "Share",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Main Content - Huge Numbers
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(vertical = 32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(id = R.string.days_lived).uppercase(),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = daysLivedStr,
                fontSize = 80.sp,
                fontWeight = FontWeight.Light,
                color = MaterialTheme.colorScheme.onBackground,
                lineHeight = 80.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Progress Bar between the numbers
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(4.dp)
                    .background(MaterialTheme.colorScheme.outlineVariant, CircleShape)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(if (percentage > 0f) percentage.coerceIn(0.001f, 1f) else 0.001f)
                        .height(4.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = stringResource(id = R.string.percentage_completed, percentageStr),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.sp,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = daysRemainingStr,
                fontSize = 80.sp,
                fontWeight = FontWeight.Light,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                lineHeight = 80.sp
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = stringResource(id = R.string.days_remaining).uppercase(),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
        }

        // Action Button Area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 16.dp, bottom = 32.dp)
                .zIndex(10f),
            contentAlignment = Alignment.CenterEnd
        ) {
            FloatingActionButton(
                onClick = {
                    if (todayEntryExists) {
                        showDialog = true
                    } else {
                        navigateToEntry(todayIso)
                    }
                },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(imageVector = Icons.Default.Create, contentDescription = stringResource(id = R.string.add_entry))
            }
        }
    }
}
