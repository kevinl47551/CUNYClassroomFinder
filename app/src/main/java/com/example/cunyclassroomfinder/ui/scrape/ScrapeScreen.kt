package com.example.cunyclassroomfinder.ui.scrape

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.MaterialTheme
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
        viewModel.loadSchoolsAndTerms()
    }

    Box(modifier = modifier) {
        Column(
            modifier = modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (scrapeScreenUiState.availableSchools.isNotEmpty() && scrapeScreenUiState.availableTerms.isNotEmpty()) {
                NameWithValueDropDown(
                    label = "Select School",
                    options = scrapeScreenUiState.availableSchools,
                    enabled = !scrapeScreenUiState.isLoading,
                    selectOption = {
                        viewModel.setCurrentSchool(it)
                    }
                )
                NameWithValueDropDown(
                    label = "Select Term",
                    options = scrapeScreenUiState.availableTerms,
                    enabled = !scrapeScreenUiState.isLoading,
                    selectOption = {
                        viewModel.setCurrentTerm(it)
                    }
                )

                Button(
                    onClick = {viewModel.getAvailableCourses()},
                    enabled = !scrapeScreenUiState.isLoading
                ) {
                    Text("Scrape courses")
                }
            }

            if (scrapeScreenUiState.isLoading) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    Text(scrapeScreenUiState.loadingMessage)
                }
            }

            scrapeScreenUiState.errorMessage?.let { error ->
                Text(text = error, color = MaterialTheme.colorScheme.error)
                // Schools never loaded, so there's nothing on screen to retry with
                if (scrapeScreenUiState.availableSchools.isEmpty() && !scrapeScreenUiState.isLoading) {
                    Button(onClick = { viewModel.loadSchoolsAndTerms() }) {
                        Text("Retry")
                    }
                }
            }

            if (!scrapeScreenUiState.isLoading && scrapeScreenUiState.scrapedSubjects.isNotEmpty()) {
                Text("Scraped ${scrapeScreenUiState.scrapedSubjects.size} subjects.")
                if (scrapeScreenUiState.failedToScrapeSubjects.isNotEmpty()) {
                    Text(
                        text = "${scrapeScreenUiState.failedToScrapeSubjects.size} subjects failed: " +
                            scrapeScreenUiState.failedToScrapeSubjects.joinToString { it.first },
                        color = MaterialTheme.colorScheme.error
                    )
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
fun NameWithValueDropDown(
    label: String,
    options: List<NameWithValueOption>,
    selectOption: (NameWithValueOption) -> Unit,
    enabled: Boolean = true
) {
    var selected by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = {expanded = it && enabled}
    ) {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            enabled = enabled,
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