package com.example.cunyclassroomfinder.ui.room

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TimeInput
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDialog
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.cunyclassroomfinder.R
import com.example.cunyclassroomfinder.ui.AppViewModelProvider

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FindRoomScreen(
    navToHome: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: FindRoomViewModel = viewModel(factory = AppViewModelProvider.factory)
) {
    val findRoomUiState by viewModel.uiState.collectAsState()
    val startTimeState = rememberTimePickerState()
    val endTimeState = rememberTimePickerState()

    Box() {
        Column(
            modifier = modifier
        ) {
            TimeInput(
                state = startTimeState,
            )
            TimeInput(
                state = endTimeState,
            )
            DayDropDown(selectDay = {viewModel.updateDay(it)})
            Button(onClick = {
                viewModel.getRooms(
                    startHour = startTimeState.hour,
                    startMin = startTimeState.minute,
                    endHour = endTimeState.hour,
                    endMin = endTimeState.minute
                )
            }) {}
            Text(findRoomUiState.rooms.size.toString())
            LazyColumn() {
                items(findRoomUiState.rooms) { room ->
                    Text(text = room.room)
                }
            }
        }

        Button(
            onClick = navToHome,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp)
        ) {
            Text(stringResource(R.string.home))
        }
    }

}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DayDropDown(
    selectDay: (String) -> Unit
) {
    val days = listOf<String>(
        "Monday",
        "Tuesday",
        "Wednesday",
        "Thursday",
        "Friday",
        "Saturday",
        "Sunday"
    )
    var selected by remember { mutableStateOf("Monday") }
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = {expanded = it}
    ) {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            label = {Text("day of week")},
            modifier = Modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = {expanded = false}
        ) {
            days.forEach { day ->
                DropdownMenuItem(
                    text = {Text(day)},
                    onClick = {
                        selected = day
                        expanded = false
                        selectDay(day)
                    }
                )
            }
        }
    }

}