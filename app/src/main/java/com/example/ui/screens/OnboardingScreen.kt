package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.example.ui.MainViewModel
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    viewModel: MainViewModel,
    onFinish: () -> Unit
) {
    val initialDob by viewModel.userDob.collectAsState()
    
    // Auto-navigate if returning user
    LaunchedEffect(initialDob) {
        if (!initialDob.isNullOrEmpty()) {
            onFinish()
        }
    }

    var localDob by remember { mutableStateOf("") }
    var localLifespan by remember { mutableStateOf("80") }
    var isDateValid by remember { mutableStateOf(true) }
    
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(id = com.example.R.string.welcome),
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(id = com.example.R.string.welcome_desc),
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(48.dp))

        Text(
            text = stringResource(id = com.example.R.string.date_of_birth),
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        
        OutlinedTextField(
            value = localDob,
            onValueChange = { 
                localDob = it
                try {
                    if (it.length == 10) {
                        LocalDate.parse(it)
                        isDateValid = true
                    } else {
                        isDateValid = true
                    }
                } catch (e: Exception) {
                    isDateValid = false
                }
            },
            placeholder = { Text("1990-01-25") },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            ),
            modifier = Modifier.fillMaxWidth(),
            textStyle = LocalTextStyle.current.copy(fontSize = 24.sp, fontWeight = FontWeight.Medium),
            shape = RoundedCornerShape(16.dp),
            isError = !isDateValid,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            )
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = stringResource(id = com.example.R.string.expected_lifespan),
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = localLifespan,
            onValueChange = { localLifespan = it },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    keyboardController?.hide()
                    if (localDob.isNotEmpty() && isDateValid) {
                        try {
                            LocalDate.parse(localDob)
                            val ls = localLifespan.toIntOrNull() ?: 80
                            viewModel.saveUserLifespan(ls)
                            viewModel.saveUserDob(localDob)
                        } catch (e: Exception) {
                            isDateValid = false
                        }
                    }
                }
            ),
            modifier = Modifier.fillMaxWidth(),
            textStyle = LocalTextStyle.current.copy(fontSize = 24.sp, fontWeight = FontWeight.Medium),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            )
        )

        Spacer(modifier = Modifier.height(48.dp))

        Button(
            onClick = {
                keyboardController?.hide()
                if (localDob.isNotEmpty()) {
                    try {
                        LocalDate.parse(localDob)
                        isDateValid = true
                        val ls = localLifespan.toIntOrNull() ?: 80
                        viewModel.saveUserLifespan(ls)
                        viewModel.saveUserDob(localDob)
                    } catch (e: Exception) {
                        isDateValid = false
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ),
            enabled = localDob.isNotEmpty() && isDateValid
        ) {
            Text(stringResource(id = com.example.R.string.start_journey), fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
    }
}
