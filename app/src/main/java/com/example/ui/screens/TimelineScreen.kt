package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.example.R
import com.example.data.DailyEntry
import com.example.ui.MainViewModel
import androidx.compose.ui.graphics.Color
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import kotlin.random.Random

fun getDayOfLife(dobString: String?, dateString: String): Long? {
    if (dobString == null) return null
    return try {
        val dob = LocalDate.parse(dobString)
        val entryDate = LocalDate.parse(dateString)
        if (entryDate.isBefore(dob)) return null
        ChronoUnit.DAYS.between(dob, entryDate) + 1
    } catch (e: Exception) {
        null
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimelineScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit,
    onNavigateToEntry: (String) -> Unit
) {
    val entries by viewModel.allEntries.collectAsState()
    val dob by viewModel.userDob.collectAsState()
    
    val randomMemory by viewModel.randomEntry.collectAsState()
    
    LaunchedEffect(entries) {
        if (randomMemory == null && entries.isNotEmpty()) {
            viewModel.fetchRandomEntry()
        }
    }

    var selectedEntry by remember { mutableStateOf<DailyEntry?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showBottomSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    if (showDeleteDialog && selectedEntry != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(text = stringResource(id = R.string.delete_confirm_title)) },
            text = { Text(text = stringResource(id = R.string.delete_confirm_msg)) },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    viewModel.deleteEntry(selectedEntry!!.date)
                    selectedEntry = null
                }) {
                    Text(stringResource(id = R.string.delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(id = R.string.cancel_button))
                }
            }
        )
    }

    if (showBottomSheet && selectedEntry != null) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false },
            sheetState = sheetState
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Button(
                    onClick = {
                        showBottomSheet = false
                        onNavigateToEntry(selectedEntry!!.date)
                    },
                    modifier = Modifier.fillMaxWidth().height(64.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit")
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(stringResource(id = R.string.edit_entry), fontSize = 20.sp)
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        showBottomSheet = false
                        showDeleteDialog = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.fillMaxWidth().height(64.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete")
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(stringResource(id = R.string.delete_entry), fontSize = 20.sp)
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TopAppBar(
            title = {
                Text(
                    text = stringResource(id = R.string.timeline),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(id = R.string.cancel_button))
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.background
            )
        )
        
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            if (entries.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillParentMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(id = R.string.no_memories),
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                randomMemory?.let { memory ->
                item {
                    val displayDate = try {
                        LocalDate.parse(memory.date).format(DateTimeFormatter.ofPattern("MMMM d, yyyy"))
                    } catch (e: Exception) {
                        memory.date
                    }
                    val dayOfLife = getDayOfLife(dob, memory.date)
                    
                    val blendedColor = memory.vibeColor?.let {
                        Color(android.graphics.Color.parseColor(it))
                    } ?: MaterialTheme.colorScheme.surfaceVariant

                    val memoryFormatStr = stringResource(id = R.string.random_memory_format)
                    val memoryText = if (dayOfLife != null) {
                        String.format(memoryFormatStr, displayDate, dayOfLife, memory.textStory)
                    } else {
                        "On $displayDate, you wrote: \"${memory.textStory}\""
                    }

                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = stringResource(id = R.string.random_memory_title),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    letterSpacing = 1.sp
                                )
                                TextButton(
                                    onClick = { viewModel.fetchRandomEntry() },
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh, 
                                        contentDescription = "Refresh",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = stringResource(id = R.string.see_another_day), 
                                        fontSize = 12.sp, 
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = memoryText,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                lineHeight = 22.sp
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = stringResource(id = R.string.vibe_was),
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    fontSize = 14.sp
                                )
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clip(CircleShape)
                                        .background(blendedColor)
                                )
                                val labelRes = getVibeLabelRes(memory.vibeColor)
                                val labelStr = labelRes?.let { stringResource(id = it) } ?: "Recorded"
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = labelStr,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            items(entries.sortedByDescending { it.date }) { entry ->
                val displayDate = try {
                    LocalDate.parse(entry.date).format(DateTimeFormatter.ofPattern("MMMM d, yyyy"))
                } catch (e: Exception) {
                    entry.date
                }
                
                val dayOfLife = getDayOfLife(dob, entry.date)
                
                val dateLabel = if (dayOfLife != null) {
                    String.format(stringResource(id = R.string.timeline_date_format), dayOfLife, displayDate)
                } else {
                    displayDate
                }
                
                val blendedColor = entry.vibeColor?.let {
                    Color(android.graphics.Color.parseColor(it))
                } ?: MaterialTheme.colorScheme.surfaceVariant

                val labelRes = getVibeLabelRes(entry.vibeColor)
                val labelStr = labelRes?.let { stringResource(id = it) } ?: "Recorded"

                Row(
                    modifier = Modifier.fillMaxWidth()
                        .clickable { 
                            selectedEntry = entry
                            showBottomSheet = true 
                        }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(CircleShape)
                                    .background(blendedColor)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "$labelStr  •  $dateLabel",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground,
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = entry.textStory,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 20.sp
                        )
                    }
                }
            }
        }
        }
    }
}