package com.example.cunyclassroomfinder.ui.scrape

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.cunyclassroomfinder.R
import com.example.cunyclassroomfinder.ui.AppViewModelProvider

@Composable
fun ScrapeScreen(
    navToHome: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ScrapeScreenViewModel = viewModel(factory = AppViewModelProvider.factory)
) {
    val scrapeScreenUiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.parseSchoolsAndSetTerms(viewModel.getAvailableSchoolsAndTerms())
    }

    Box(modifier = modifier) {
        Column(modifier = modifier) {
            if (scrapeScreenUiState.availableSchools.isNotEmpty() && scrapeScreenUiState.availableTerms.isNotEmpty()) {
                NameWithValueDropDown(
                    label = "Select School",
                    options = scrapeScreenUiState.availableSchools,
                    selectOption = {
                        viewModel.setCurrentSchool(it)
                    }
                )
                NameWithValueDropDown(
                    label = "Select Term",
                    options = scrapeScreenUiState.availableTerms,
                    selectOption = {
                        viewModel.setCurrentTerm(it)
                    }
                )
            }



            Button(
                onClick = {viewModel.getAvailableCourses()},
                modifier = modifier
            ) { }
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
fun NameWithValueDropDown(
    label: String,
    options: List<NameWithValueOption>,
    selectOption: (NameWithValueOption) -> Unit
) {
    var selected by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = {expanded = it}
    ) {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            label = {Text(label)},
            modifier = Modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = {expanded = false}
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = {Text(option.selectedName)},
                    onClick = {
                        selected = option.selectedName
                        expanded = false
                        selectOption(option)
                    }
                )
            }
        }
    }

}