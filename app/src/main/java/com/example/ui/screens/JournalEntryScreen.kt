package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.DailyEntry
import com.example.ui.MainViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.example.R

data class Vibe(val hex: String, val labelRes: Int)

val VIBES = listOf(
    Vibe("#A8DADC", R.string.vibe_peaceful),
    Vibe("#F4A261", R.string.vibe_joyful),
    Vibe("#2A9D8F", R.string.vibe_productive),
    Vibe("#8D99AE", R.string.vibe_tired),
    Vibe("#E76F51", R.string.vibe_anxious)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JournalEntryScreen(
    date: String,
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    var storyText by remember { mutableStateOf("") }
    var selectedVibe by remember { mutableStateOf<String?>(null) }
    
    val isError = storyText.length > 280

    // Attempt to load existing entry
    val existingEntry by viewModel.getEntryByDate(date).collectAsState(initial = null)
    
    LaunchedEffect(existingEntry) {
        existingEntry?.let {
            storyText = it.textStory
            selectedVibe = it.vibeColor
        }
    }

    val displayDate = try {
        LocalDate.parse(date).format(DateTimeFormatter.ofPattern("MMMM d, yyyy", java.util.Locale.getDefault()))
    } catch (e: Exception) {
        date
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top App Bar
        TopAppBar(
            title = {
                Text(
                    text = displayDate.uppercase(java.util.Locale.getDefault()),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = stringResource(id = R.string.cancel_button))
                }
            },
            actions = {
                Button(
                    onClick = {
                        viewModel.insertEntry(
                            DailyEntry(
                                date = date,
                                textStory = storyText,
                                vibeColor = selectedVibe
                            )
                        )
                        onBack()
                    },
                    enabled = !isError,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier.padding(end = 8.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(stringResource(id = R.string.save), fontWeight = FontWeight.Bold)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.background
            )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = stringResource(id = R.string.how_was_your_vibe),
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )
            
            Spacer(modifier = Modifier.height(12.dp))

            // Vibe Selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                VIBES.forEach { vibe ->
                    val isSelected = selectedVibe == vibe.hex
                    val bgColor = Color(android.graphics.Color.parseColor(vibe.hex))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .shadow(if (isSelected) 4.dp else 0.dp, CircleShape)
                                .clip(CircleShape)
                                .background(bgColor)
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.onBackground else Color.Transparent,
                                    shape = CircleShape
                                )
                                .clickable {
                                    selectedVibe = if (isSelected) null else vibe.hex
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = stringResource(id = vibe.labelRes),
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(bottom = 24.dp)
            ) {
                OutlinedTextField(
                    value = storyText,
                    onValueChange = { storyText = it },
                    modifier = Modifier.fillMaxSize(),
                    placeholder = { 
                        Text(
                            text = stringResource(id = R.string.prompt_text), 
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            fontSize = 18.sp
                        ) 
                    },
                    textStyle = LocalTextStyle.current.copy(
                        fontSize = 18.sp,
                        lineHeight = 28.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    ),
                    isError = isError,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = if(isError) MaterialTheme.colorScheme.error else Color.Transparent,
                        unfocusedBorderColor = if(isError) MaterialTheme.colorScheme.error else Color.Transparent,
                        disabledBorderColor = Color.Transparent,
                        errorBorderColor = MaterialTheme.colorScheme.error,
                        cursorColor = MaterialTheme.colorScheme.primary
                    )
                )
                
                Text(
                    text = "${storyText.length} / 280",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(bottom = 16.dp, end = 16.dp)
                )
            }
        }
    }
}
