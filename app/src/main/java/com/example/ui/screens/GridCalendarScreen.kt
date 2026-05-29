package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.DailyEntry
import com.example.ui.MainViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

/**
 * Computes the number of lived days within a specific calendar year.
 */
fun computeDaysLivedInYear(year: Int, dob: LocalDate?, today: LocalDate): Long {
    if (dob == null) return 0
    val startOfYear = LocalDate.of(year, 1, 1)
    val endOfYear = LocalDate.of(year, 12, 31)
    val start = if (dob.isAfter(startOfYear)) dob else startOfYear
    val end = if (today.isBefore(endOfYear)) today else endOfYear
    if (start.isAfter(end)) return 0
    return ChronoUnit.DAYS.between(start, end) + 1
}

/**
 * Computes the sequential day number of a target date relative to the user's date of birth.
 */
fun computeDayOfLife(dob: LocalDate?, target: LocalDate): Long? {
    if (dob == null) return null
    if (target.isBefore(dob)) return null
    return ChronoUnit.DAYS.between(dob, target) + 1
}

/**
 * Maps the daily entry color hex to its corresponding localized string resource label.
 */
fun getVibeLabelRes(hex: String?): Int? {
    return when (hex?.uppercase()) {
        "#A8DADC" -> R.string.vibe_peaceful
        "#F4A261" -> R.string.vibe_joyful
        "#2A9D8F" -> R.string.vibe_productive
        "#8D99AE" -> R.string.vibe_tired
        "#E76F51" -> R.string.vibe_anxious
        else -> null
    }
}

@Composable
fun SelectedDayInfoPanel(
    selectedDate: LocalDate?,
    entry: DailyEntry?,
    dob: LocalDate?
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            if (selectedDate == null) {
                Text(
                    text = stringResource(id = R.string.select_day_details),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            } else {
                val displayDate = selectedDate.format(DateTimeFormatter.ofPattern("MMMM d, yyyy", java.util.Locale.getDefault()))
                val dayOfLife = computeDayOfLife(dob, selectedDate)
                
                Text(
                    text = displayDate,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.height(4.dp))
                if (dayOfLife != null) {
                    Text(
                        text = stringResource(id = R.string.day_of_life, dayOfLife),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                
                if (entry != null && entry.vibeColor != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .clip(CircleShape)
                                .background(Color(android.graphics.Color.parseColor(entry.vibeColor)))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        val labelRes = getVibeLabelRes(entry.vibeColor)
                        val labelStr = labelRes?.let { stringResource(id = it) } ?: "Recorded"
                        Text(
                            text = labelStr,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    if (entry.textStory.isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = entry.textStory,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                } else {
                    Text(
                        text = stringResource(id = R.string.no_entry_recorded),
                        fontSize = 16.sp,
                        fontStyle = FontStyle.Italic,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

data class GridCellData(
    val date: LocalDate,
    val isSelected: Boolean,
    val isGhost: Boolean,
    val color: Color,
    val isEmpty: Boolean
)

/**
 * A structurally optimized heat-map calendar representing a single year.
 * Renders localized blocks dynamically through drawBehind to eliminate layout overhead and applies
 * boundary logic (ghosting future days and pre-birth days) aligned with dual-theme capabilities.
 */
@Composable
fun GithubStyleYearGrid(
    year: Int, 
    entries: List<DailyEntry>, 
    selectedDate: LocalDate?,
    today: LocalDate,
    birthDate: LocalDate?,
    onDateClick: (LocalDate) -> Unit
) {
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant
    
    val weeks = remember(year, entries, selectedDate, today, birthDate, surfaceVariant) {
        val entriesByDate = entries.filter { it.date.startsWith("$year-") }.associateBy { it.date } 
        val firstDayOfYear = LocalDate.of(year, 1, 1)
        val daysOffset = firstDayOfYear.dayOfWeek.value - 1 // 0 for Monday, 6 for Sunday

        val totalDays = firstDayOfYear.lengthOfYear()
        val totalCells = daysOffset + totalDays
        val totalWeeks = Math.ceil(totalCells / 7.0).toInt()

        (0 until totalWeeks).map { weekIndex ->
            (0..6).map { dayIndex ->
                val cellIndex = weekIndex * 7 + dayIndex
                val dayOfYear = cellIndex - daysOffset + 1
                if (dayOfYear in 1..totalDays) {
                    val date = firstDayOfYear.plusDays((dayOfYear - 1).toLong())
                    val dateString = date.format(DateTimeFormatter.ISO_LOCAL_DATE)
                    val entry = entriesByDate[dateString]
                    
                    val isSelected = date == selectedDate
                    val isFuture = date.isAfter(today)
                    val isBeforeBirth = birthDate != null && date.isBefore(birthDate)
                    val isGhost = isFuture || isBeforeBirth

                    val isEmpty = entry == null
                    val boxColor = entry?.vibeColor?.let {
                        Color(android.graphics.Color.parseColor(it))
                    } ?: surfaceVariant

                    GridCellData(
                        date = date,
                        isSelected = isSelected,
                        isGhost = isGhost,
                        color = boxColor,
                        isEmpty = isEmpty
                    )
                } else {
                    null
                }
            }
        }
    }

    val monthLabels = remember(year, weeks) {
        var lastMonth = -1
        weeks.map { week ->
            val firstDate = week.firstOrNull { it != null }?.date
            if (firstDate != null && firstDate.monthValue != lastMonth) {
                lastMonth = firstDate.monthValue
                firstDate.format(DateTimeFormatter.ofPattern("MMM", java.util.Locale.getDefault())).lowercase().replaceFirstChar { it.uppercase() }
            } else {
                ""
            }
        }
    }

    val dashedPathEffect = remember { PathEffect.dashPathEffect(floatArrayOf(8f, 4f), 0f) }
    val outlineVariant = MaterialTheme.colorScheme.outlineVariant
    val primaryColor = MaterialTheme.colorScheme.primary

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
    ) {
        item {
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.padding(top = 20.dp, end = 4.dp)
            ) {
                for (day in 0..6) {
                    val label = when (day) {
                        0 -> stringResource(id = R.string.mon)
                        2 -> stringResource(id = R.string.wed)
                        4 -> stringResource(id = R.string.fri)
                        else -> ""
                    }
                    Box(
                        modifier = Modifier.height(24.dp),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        Text(
                            text = label,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        items(count = weeks.size, key = { index -> "${year}_$index" }) { index ->
            val week = weeks[index]
            val monthLabel = monthLabels[index]
            
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = monthLabel,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.height(16.dp)
                )

                for (cell in week) {
                    if (cell != null) {
                        Spacer(
                            modifier = Modifier
                                .size(24.dp)
                                .drawBehind {
                                    val cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx())
                                    if (cell.isGhost) {
                                        drawRoundRect(
                                            color = outlineVariant.copy(alpha = 0.5f),
                                            style = Stroke(width = 1.dp.toPx(), pathEffect = dashedPathEffect),
                                            cornerRadius = cornerRadius
                                        )
                                        drawRoundRect(
                                            color = outlineVariant.copy(alpha = 0.05f),
                                            cornerRadius = cornerRadius
                                        )
                                    } else {
                                        drawRoundRect(
                                            color = cell.color,
                                            cornerRadius = cornerRadius
                                        )
                                        if (cell.isSelected) {
                                            drawRoundRect(
                                                color = primaryColor,
                                                style = Stroke(width = 2.dp.toPx()),
                                                cornerRadius = cornerRadius
                                            )
                                        } else if (cell.isEmpty) {
                                            drawRoundRect(
                                                color = outlineVariant.copy(alpha = 0.6f),
                                                style = Stroke(width = 1.dp.toPx()),
                                                cornerRadius = cornerRadius
                                            )
                                        }
                                    }
                                }
                                .clickable(enabled = !cell.isGhost) { onDateClick(cell.date) }
                        )
                    } else {
                        Spacer(modifier = Modifier.size(24.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun YearCard(
    year: Int,
    daysLived: Long,
    isExpanded: Boolean,
    onClick: () -> Unit
) {
    val daysLivedString = stringResource(id = R.string.days_lived)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 72.dp)
                .padding(horizontal = 24.dp, vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "$year",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "$daysLived $daysLivedString",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = if (isExpanded) "Collapse" else "Expand",
                modifier = Modifier.size(36.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GridCalendarScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val entries by viewModel.allEntries.collectAsState()
    val dobString by viewModel.userDob.collectAsState()
    val dob = try { dobString?.let { LocalDate.parse(it) } } catch (e: Exception) { null }
    val today = LocalDate.now()
    val currentYear = today.year
    val birthYear = dob?.year ?: currentYear
    
    var expandedYears by remember { mutableStateOf(setOf(currentYear)) }
    
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }

    val yearsList = (currentYear downTo birthYear).toList()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TopAppBar(
            title = {
                Text(
                    text = stringResource(id = R.string.vibe_grid),
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

        SelectedDayInfoPanel(
            selectedDate = selectedDate,
            entry = selectedDate?.let { date -> entries.find { it.date == date.format(DateTimeFormatter.ISO_LOCAL_DATE) } },
            dob = dob
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(yearsList, key = { it }) { year ->
                val isExpanded = expandedYears.contains(year)
                val daysLived = computeDaysLivedInYear(year, dob, today)
                
                Column {
                    YearCard(
                        year = year,
                        daysLived = daysLived,
                        isExpanded = isExpanded,
                        onClick = {
                            expandedYears = if (isExpanded) expandedYears - year else expandedYears + year
                        }
                    )

                    if (isExpanded) {
                        GithubStyleYearGrid(
                            year = year,
                            entries = entries,
                            selectedDate = selectedDate,
                            today = today,
                            birthDate = dob,
                            onDateClick = { 
                                selectedDate = it
                            }
                        )
                    }
                }
            }
        }
    }
}