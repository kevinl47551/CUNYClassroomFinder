package com.example.cunyclassroomfinder.ui.room

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.cunyclassroomfinder.data.CunyRoom
import com.example.cunyclassroomfinder.ui.AppViewModelProvider
import com.example.cunyclassroomfinder.ui.components.OptionDropDown
import com.example.cunyclassroomfinder.ui.components.displaySchoolName
import java.time.LocalTime
import java.time.format.DateTimeFormatter

// Bits match Course.days
private val days = listOf(
    "Mon" to 1, "Tue" to 2, "Wed" to 4, "Thu" to 8, "Fri" to 16, "Sat" to 32, "Sun" to 64
)

private val timeFormatter = DateTimeFormatter.ofPattern("h:mm a")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FindRoomScreen(
    modifier: Modifier = Modifier,
    viewModel: FindRoomViewModel = viewModel(factory = AppViewModelProvider.factory)
) {
    val uiState by viewModel.uiState.collectAsState()

    // Default to the current hour through the next hour
    val now = remember { LocalTime.now() }
    val startTimeState = rememberTimePickerState(initialHour = now.hour, initialMinute = 0)
    val endTimeState = rememberTimePickerState(
        initialHour = (now.hour + 1).coerceAtMost(23),
        initialMinute = if (now.hour == 23) 59 else 0
    )
    var showStartPicker by remember { mutableStateOf(false) }
    var showEndPicker by remember { mutableStateOf(false) }

    val validRange = endTimeState.hour * 60 + endTimeState.minute >
        startTimeState.hour * 60 + startTimeState.minute
    val visibleRooms = remember(uiState.rooms, uiState.searchQuery) { uiState.filteredRooms }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (uiState.availableSchools.isEmpty()) {
                        Text(
                            text = "No class data yet. Go to \"Update class data\" on the home screen and download a school first.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        OptionDropDown(
                            label = "School",
                            options = uiState.availableSchools,
                            selected = uiState.currentSchool.ifEmpty { null },
                            optionLabel = ::displaySchoolName,
                            onSelect = viewModel::setCurrentSchool
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        SectionLabel("Day")
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            days.forEach { (label, bit) ->
                                FilterChip(
                                    selected = uiState.selectedDay == bit,
                                    onClick = { viewModel.updateDay(bit) },
                                    label = { Text(label) }
                                )
                            }
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        SectionLabel("Time")
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            TimeField(
                                label = "From",
                                state = startTimeState,
                                onClick = { showStartPicker = true },
                                modifier = Modifier.weight(1f)
                            )
                            TimeField(
                                label = "To",
                                state = endTimeState,
                                onClick = { showEndPicker = true },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        if (!validRange) {
                            Text(
                                text = "End time must be after the start time.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }

                    Button(
                        onClick = {
                            viewModel.getRooms(
                                startHour = startTimeState.hour,
                                startMin = startTimeState.minute,
                                endHour = endTimeState.hour,
                                endMin = endTimeState.minute
                            )
                        },
                        enabled = validRange && uiState.currentSchool.isNotEmpty() && !uiState.isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = LocalContentColor.current
                            )
                        } else {
                            Icon(Icons.Filled.Search, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Find free rooms")
                        }
                    }
                }
            }
        }

        if (uiState.hasSearched) {
            item {
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = viewModel::updateSearchQuery,
                    placeholder = { Text("Filter rooms, e.g. HW 7") },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                    trailingIcon = {
                        if (uiState.searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                                Icon(Icons.Filled.Clear, contentDescription = "Clear search")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(28.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                )
            }

            item {
                Text(
                    text = if (uiState.searchQuery.isBlank()) "${uiState.rooms.size} free rooms"
                        else "${visibleRooms.size} of ${uiState.rooms.size} free rooms match",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            if (visibleRooms.isEmpty()) {
                item {
                    Text(
                        text = if (uiState.rooms.isEmpty()) "No rooms are free at this time."
                            else "No free rooms match \"${uiState.searchQuery.trim()}\".",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp)
                    )
                }
            }

            items(visibleRooms, key = { it.school + "|" + it.room }) { room ->
                RoomCard(room)
            }
        }
    }

    if (showStartPicker) {
        TimeSelectDialog("Start time", startTimeState) { showStartPicker = false }
    }
    if (showEndPicker) {
        TimeSelectDialog("End time", endTimeState) { showEndPicker = false }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimeField(
    label: String,
    state: TimePickerState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedCard(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = LocalTime.of(state.hour, state.minute).format(timeFormatter),
                style = MaterialTheme.typography.titleLarge
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimeSelectDialog(
    title: String,
    state: TimePickerState,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Done") }
        },
        title = { Text(title) },
        text = { TimePicker(state = state) }
    )
}

@Composable
private fun RoomCard(room: CunyRoom) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Filled.Place, contentDescription = null, modifier = Modifier.size(20.dp))
                }
            }
            Text(
                text = room.room,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "Free",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.tertiary
            )
        }
    }
}
