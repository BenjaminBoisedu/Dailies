package com.example.daily.presentations.addedit

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Shapes
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.daily.presentations.HighPriority
import com.example.daily.presentations.StandardPriority
import com.example.daily.presentations.navigation.Screen
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SelectableChipColors
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.flow.collectLatest
import java.util.Calendar
import java.text.SimpleDateFormat
import java.util.Locale


@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("InvalidColorHexValue", "RememberReturnType", "CoroutineCreationDuringComposition",
    "SuspiciousIndentation"
)
@Composable
fun AddEditDailyScreen(
    navController: NavController,
    viewModel: AddEditDailyViewModel = hiltViewModel()
) {
    val listPriority = mapOf(
        StandardPriority to "Standard",
        HighPriority to "Haute"
    )
    var selectedPriority by remember { mutableStateOf(viewModel.daily.value.priority) }

    val snackbarHostState = remember { SnackbarHostState() }
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding -> Modifier.padding(padding) }
    LaunchedEffect(true) {
        viewModel.eventFlow.collectLatest { event ->
            when (event) {
                is AddEditDailyUiEvent.SavedDaily -> {
                    navController.navigate(Screen.DailiesListScreen.route)
                }
                is AddEditDailyUiEvent.ShowMessage -> {
                    snackbarHostState.showSnackbar(event.message)
                }
            }
        }
    }
    Column (
        Modifier
            .background(Color(0xFF303030))
            .fillMaxSize (),
    ) {
        LaunchedEffect(true) {
            viewModel.eventFlow.collectLatest { event ->
                when (event) {
                    is AddEditDailyUiEvent.SavedDaily -> {
                        navController.navigate(Screen.DailiesListScreen.route)
                    }
                    is AddEditDailyUiEvent.ShowMessage -> {
                        snackbarHostState.showSnackbar(event.message)
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(26.dp))
        Row (
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically

        ) {
            FloatingActionButton(
                onClick = {
                    navController.navigate(Screen.DailiesListScreen.route)
                },
                modifier = Modifier
                    .padding(8.dp)
                    .align(Alignment.CenterVertically),
                containerColor = Color(0xFF7684A7),
                contentColor = Color.Black,
                elevation = FloatingActionButtonDefaults.elevation(5.dp, 0.dp, 7.dp),
                shape = Shapes().extraLarge
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back"
                )
            }
            Text(
                text = "Ajouter une daily",
                modifier = Modifier
                    .padding(15.dp)
                    .align(Alignment.CenterVertically),
                style = TextStyle(
                    fontSize = 28.sp,
                    textAlign = TextAlign.Center,
                    letterSpacing = 0.15.sp,
                    color = Color.White,
                )
            )
        }
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(10.dp)
        ) {
            item{
                val daily = viewModel.daily.value
                OutlinedTextField(
                    value = daily.title,
                    placeholder = { Text("Titre") },
                    onValueChange = {
                        viewModel.onEvent(AddEditDailyEvent.EnteredTitle(it))
                    },
                    modifier = Modifier.fillMaxWidth()
                        .border(3.dp, Color.White, Shapes().medium),
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = Color.Black,
                        errorTextColor = Color(0xFF6E4586),
                    ),
                    singleLine = true,
                    shape = Shapes().medium
                )

                Spacer(modifier = Modifier.height(16.dp))
                daily.description?.let { it ->
                    OutlinedTextField(
                        value  = it,
                        onValueChange = { viewModel.onEvent(AddEditDailyEvent.EnteredDescription(it)) },
                        placeholder = { Text("Description") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .border(8.dp, Color.White, Shapes().medium),
                        colors = TextFieldDefaults.colors(
                            focusedTextColor = Color.Black,
                            errorTextColor = Color(0xFF6E4586),
                        ),
                        singleLine = false,
                        shape = Shapes().medium
                    )

                }
                Spacer(modifier = Modifier.height(16.dp))
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "Priorité",
                        fontSize = 15.sp,
                        fontWeight = FontWeight(700),
                        color = Color(0xFF303030),
                        textAlign = TextAlign.Center,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.align(Alignment.Start)
                            .offset(y = (-24).dp, x = (-7).dp)
                            .padding(horizontal = 8.dp)
                            .height(40.dp)
                            .width(100.dp)
                            .background(Color.White, RoundedCornerShape(bottomEnd = 12.dp, bottomStart = 12.dp)),
                        style = MaterialTheme.typography.headlineMedium.copy(
                            color = Color.Black
                        )
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        listPriority.forEach { priority ->
                            FilterChip(
                                onClick = { selectedPriority = priority.key
                                    viewModel.onEvent(AddEditDailyEvent.PrioritySelected(priority.key)) },
                                label = {
                                    Text(
                                        text = priority.value,
                                        style = TextStyle(
                                            fontSize = 16.sp,
                                            color = daily.priority?.foregroundColor ?: Color.Black,
                                        ),
                                        modifier = Modifier.padding(8.dp)
                                    )
                                },
                                colors = SelectableChipColors(
                                    containerColor = priority.key.backgroundColor,
                                    labelColor = priority.key.foregroundColor,
                                    leadingIconColor = priority.key.foregroundColor,
                                    trailingIconColor = priority.key.foregroundColor,
                                    disabledContainerColor = priority.key.backgroundColor,
                                    disabledLabelColor = priority.key.foregroundColor,
                                    disabledLeadingIconColor = priority.key.foregroundColor,
                                    disabledTrailingIconColor = priority.key.foregroundColor,
                                    disabledSelectedContainerColor = priority.key.backgroundColor,
                                    selectedContainerColor = priority.key.backgroundColor,
                                    selectedLabelColor = priority.key.foregroundColor,
                                    selectedLeadingIconColor = priority.key.foregroundColor,
                                    selectedTrailingIconColor = priority.key.foregroundColor,
                                ),
                                shape = Shapes().small,
                                selected = selectedPriority == priority.key,
                                modifier = Modifier.padding(8.dp),
                                leadingIcon = if (selectedPriority == priority.key) {
                                    {
                                        Icon(
                                            imageVector = Icons.Filled.Done,
                                            contentDescription = "Done icon",
                                            modifier = Modifier.size(FilterChipDefaults.IconSize)
                                                .background(Color.Black, Shapes().medium),
                                            tint = priority.key.backgroundColor

                                        )
                                    }
                                } else {
                                    null
                                },
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    var showDatePicker by remember { mutableStateOf(false) }
                    var selectedDate by remember(viewModel.daily.value.date) {
                        mutableStateOf(viewModel.daily.value.date)
                    }

                    if (showDatePicker) {
                        val calendar = Calendar.getInstance()
                        val datePickerState = rememberDatePickerState(
                            initialSelectedDateMillis = calendar.timeInMillis
                        )

                        DatePickerDialog(
                            onDismissRequest = { showDatePicker = false },
                            confirmButton = {
                                TextButton(onClick = {
                                    datePickerState.selectedDateMillis?.let { dateMillis ->
                                        val calendar = Calendar.getInstance().apply {
                                            timeInMillis = dateMillis
                                            add(Calendar.DAY_OF_MONTH, 1)
                                        }
                                        val newDate = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
                                            .format(calendar.time)
                                        selectedDate = newDate
                                        viewModel.onEvent(AddEditDailyEvent.DateSelected(newDate))
                                    }
                                    showDatePicker = false
                                }) {
                                    Text("OK")
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { showDatePicker = false }) {
                                    Text("Cancel")
                                }
                            }
                        ) {
                            DatePicker(
                                state = datePickerState,
                                modifier = Modifier.padding(6.dp),
                                showModeToggle = false
                            )
                        }
                    }

                    // Champ de date amélioré
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showDatePicker = true }
                    ) {
                        TextField(
                            value = selectedDate,
                            onValueChange = { },
                            label = { Text("Date") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(3.dp, Color.White, Shapes().medium),
                            readOnly = true,
                            enabled = false,  // Abstractive interaction direct
                            colors = TextFieldDefaults.colors(
                                focusedTextColor = Color.Black,
                                disabledTextColor = Color.Black,  // Garde le texte visible quand désactivé
                                disabledLabelColor = Color.Gray,
                                disabledIndicatorColor = Color.Transparent
                            ),
                            shape = Shapes().medium,
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Default.DateRange,
                                    contentDescription = "Select date"
                                )
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    // Champ d'heure amélioré
                    val dailyTime = daily.time
                    val dailyTimeMinutes = dailyTime.substringAfter(":").toIntOrNull() ?: 0
                    val dailyTimeHours = dailyTime.substringBefore(":").toIntOrNull() ?: 0
                    var showTimePicker by remember { mutableStateOf(false) }
                    var selectedTime by remember(viewModel.daily.value.time) {
                        mutableStateOf(viewModel.daily.value.time)
                    }
                    if (showTimePicker) {
                        val timePickerState = rememberTimePickerState(
                            initialHour = dailyTimeHours,
                            initialMinute = dailyTimeMinutes,
                            is24Hour = true
                        )

                        AlertDialog(
                            onDismissRequest = { showTimePicker = false },
                            confirmButton = {
                                TextButton(onClick = {
                                    val hour = timePickerState.hour.toString().padStart(2, '0')
                                    val minute = timePickerState.minute.toString().padStart(2, '0')
                                    selectedTime = "$hour:$minute"
                                    viewModel.onEvent(AddEditDailyEvent.TimeSelected(selectedTime))
                                    showTimePicker = false
                                }) {
                                    Text("OK")
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { showTimePicker = false }) {
                                    Text("Cancel")
                                }
                            },
                            text = {
                                TimePicker(
                                    state = timePickerState,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        )
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showTimePicker = true }
                    ) {
                        TextField(
                            value = selectedTime,
                            onValueChange = { },
                            label = { Text("Horaire") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(3.dp, Color.White, Shapes().medium),
                            readOnly = true,
                            enabled = false,  // Désactive l'interaction directe
                            colors = TextFieldDefaults.colors(
                                focusedTextColor = Color.Black,
                                disabledTextColor = Color.Black,  // Garde le texte visible quand désactivé
                                disabledLabelColor = Color.Gray,
                                disabledIndicatorColor = Color.Transparent
                            ),
                            shape = Shapes().medium,
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Selectionnez un horaire"
                                )
                            }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(30.dp))

                // Listes des jours de la semaine
                val daysOfWeek = listOf("Lu", "Ma", "Me", "Je", "Ve", "Sa", "Di")
                val selectedDays = viewModel.daily.value.recurringDays?.split(",")?.map { it.trim() } ?: emptyList()
                val selectedDaysSet = selectedDays.toSet()

                Text(
                    text = "Jours de la semaine",
                    fontSize = 20.sp,
                    modifier = Modifier.padding(start = 16.dp),
                    style = MaterialTheme.typography.headlineMedium.copy(
                        color = Color.White
                            .copy(alpha = 0.8f)
                    ),
                    fontWeight = MaterialTheme.typography.headlineMedium.fontWeight,
                    fontFamily = MaterialTheme.typography.headlineMedium.fontFamily
                )

                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(daysOfWeek.size) { index ->
                        val day = daysOfWeek[index]
                        FilterChip(
                            onClick = {
                                viewModel.onEvent(AddEditDailyEvent.RecurringDaysChanged(day))
                            },
                            label = { Text(day) },
                            selected = selectedDaysSet.contains(day),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF99A4BE),
                                selectedLabelColor = Color.White,
                                disabledContainerColor = Color.LightGray,
                                disabledLabelColor = Color.Black,
                                containerColor = Color.LightGray,
                                labelColor = Color.Black,
                            ),
                        )
                    }
                }


                Spacer(modifier = Modifier.height(8.dp))

                val options = listOf(15, 30, 60, 120, 1440) // 15min, 30min, 1h, 2h, 1 jour
                val optionLabels = listOf("15 min", "30 min", "1 heure", "2 heures", "1 jour")

                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    Text(
                        text = "Quand recevoir la notification:",
                        style = MaterialTheme.typography.labelLarge
                    )

                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(options.size) { index ->
                            val selectedTime = viewModel.daily.value.notificationTime
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (selectedTime == options[index].toString())
                                            Color(0xFF99A4BE)
                                        else
                                            Color.LightGray
                                    )
                                    .clickable {
                                        viewModel.onEvent(AddEditDailyEvent.NotificationTimeSelected(options[index].toString()))
                                    }
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = optionLabels[index],
                                    color = if (selectedTime == options[index].toString())
                                        Color.White
                                    else
                                        Color.Black,
                                    style = MaterialTheme.typography.labelLarge
                                        .copy(
                                            color = Color.Black
                                        )
                                        .copy(
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        .copy(
                                            fontFamily = MaterialTheme.typography.headlineMedium.fontFamily
                                        )
                                )
                            }
                        }
                    }
                }
                // Après la section "Done"
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = viewModel.daily.value.done,
                        onCheckedChange = {
                            viewModel.onEvent(AddEditDailyEvent.DailyDone)
                        },
                        modifier = Modifier.align(Alignment.CenterVertically),
                        colors = CheckboxDefaults.colors(
                            checkedColor = Color(0xFF99A4BE),
                            uncheckedColor = Color(0xFF99A4BE)
                        )
                    )
                    Text(
                        text = "Effectuée",
                        fontSize = 20.sp,
                        modifier = Modifier.align(Alignment.CenterVertically),
                        style = MaterialTheme.typography.headlineMedium.copy(
                            color = Color.White
                                .copy(alpha = 0.8f)
                        ),
                        fontWeight = MaterialTheme.typography.headlineMedium.fontWeight,
                        fontFamily = MaterialTheme.typography.headlineMedium.fontFamily
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    LaunchedEffect(true) {
                        viewModel.eventFlow.collectLatest { event ->
                            when (event) {
                                is AddEditDailyUiEvent.SavedDaily -> {
                                    navController.navigate(Screen.DailiesListScreen.route)
                                }
                                is AddEditDailyUiEvent.ShowMessage -> {
                                    snackbarHostState.showSnackbar(event.message)
                                }
                            }
                        }
                    }
                    Button(
                        onClick = {
                            when {
                                viewModel.daily.value.title.isBlank() -> {
                                    viewModel.onEvent(AddEditDailyEvent.SaveDaily) // This will trigger ShowMessage event
                                }
                                viewModel.daily.value.description?.isBlank() != false -> {
                                    viewModel.onEvent(AddEditDailyEvent.SaveDaily) // This will trigger ShowMessage event
                                }
                                viewModel.daily.value.date.isBlank() -> {
                                    viewModel.onEvent(AddEditDailyEvent.SaveDaily) // This will trigger ShowMessage event
                                }
                                viewModel.daily.value.time.isBlank() -> {
                                    viewModel.onEvent(AddEditDailyEvent.SaveDaily) // This will trigger ShowMessage event
                                }
                                viewModel.daily.value.priority == null -> {
                                    viewModel.onEvent(AddEditDailyEvent.SaveDaily) // This will trigger ShowMessage event
                                }
                                else -> {
                                    viewModel.onEvent(AddEditDailyEvent.SaveDaily)
                                    navController.navigate(Screen.DailiesListScreen.route)
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF99A4BE),
                        ),
                    ) {
                        Text(
                            text = "Sauvegarder",
                            style = TextStyle(
                                fontSize = 25.sp,
                                letterSpacing = 0.15.sp,
                                fontFamily = MaterialTheme.typography.headlineMedium.fontFamily,
                                color = Color.White
                            )
                        )
                    }
                    Spacer(modifier = Modifier.height(130.dp))
                }
            }
        }
    }
}