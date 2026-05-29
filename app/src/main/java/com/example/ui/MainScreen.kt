package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.List
import androidx.compose.material.icons.outlined.ViewList
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.data.AppDatabase
import com.example.data.DailyEntryRepository
import com.example.data.UserPreferencesRepository
import com.example.data.dataStore
import com.example.ui.navigation.LifeTrackerNavGraph
import com.example.ui.navigation.Routes

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    
    val context = LocalContext.current
    val mainViewModel: MainViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val database = AppDatabase.getDatabase(context.applicationContext)
                val entryRepo = DailyEntryRepository(database.dailyEntryDao())
                val userPrefsRepo = UserPreferencesRepository(context.applicationContext.dataStore)
                return MainViewModel(entryRepo, userPrefsRepo) as T
            }
        }
    )

    // Hide Bottom Nav when in onboarding or journal entry
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomNav = currentRoute in listOf(Routes.HOME, Routes.GRID_CALENDAR, Routes.TIMELINE, Routes.SETTINGS)

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (showBottomNav) {
                BottomNavigationBar(navController = navController, currentRoute = currentRoute ?: Routes.HOME)
            }
        }
    ) { innerPadding ->
        LifeTrackerNavGraph(
            navController = navController,
            viewModel = mainViewModel,
            modifier = Modifier.padding(innerPadding)
        )
    }
}

@Composable
fun BottomNavigationBar(navController: NavHostController, currentRoute: String) {
    val outlineVariant = MaterialTheme.colorScheme.outlineVariant
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .drawBehind {
                drawLine(
                    color = outlineVariant,
                    start = Offset(0f, 0f),
                    end = Offset(size.width, 0f),
                    strokeWidth = 1.dp.toPx()
                )
            }
            .navigationBarsPadding()
            .height(80.dp)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        BottomNavItem(
            label = "HOME",
            icon = Icons.Filled.Home,
            iconOutlined = Icons.Outlined.Home,
            isSelected = currentRoute == Routes.HOME,
            onClick = {
                navController.navigate(Routes.HOME) {
                    popUpTo(Routes.HOME) { inclusive = true }
                }
            }
        )
        BottomNavItem(
            label = "GRID",
            icon = Icons.Filled.GridView,
            iconOutlined = Icons.Outlined.GridView,
            isSelected = currentRoute == Routes.GRID_CALENDAR,
            onClick = {
                navController.navigate(Routes.GRID_CALENDAR) {
                    popUpTo(Routes.HOME)
                }
            }
        )
        // We'll use a placeholder icon for Timeline, e.g. ViewList
        BottomNavItem(
            label = "TIMELINE",
            icon = Icons.Filled.ViewList,
            iconOutlined = Icons.Outlined.ViewList,
            isSelected = currentRoute == Routes.TIMELINE,
            onClick = {
                navController.navigate(Routes.TIMELINE) {
                    popUpTo(Routes.HOME)
                }
            }
        )
        BottomNavItem(
            label = "SETTINGS",
            icon = Icons.Filled.Settings,
            iconOutlined = Icons.Outlined.Settings,
            isSelected = currentRoute == Routes.SETTINGS,
            onClick = {
                navController.navigate(Routes.SETTINGS) {
                    popUpTo(Routes.HOME)
                }
            }
        )
    }
}

@Composable
fun BottomNavItem(
    label: String,
    icon: ImageVector,
    iconOutlined: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .alpha(if (isSelected) 1f else 0.6f)
    ) {
        if (isSelected) {
            Column(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(percent = 50))
                    .padding(horizontal = 20.dp, vertical = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        } else {
            Icon(
                imageVector = iconOutlined,
                contentDescription = label,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
