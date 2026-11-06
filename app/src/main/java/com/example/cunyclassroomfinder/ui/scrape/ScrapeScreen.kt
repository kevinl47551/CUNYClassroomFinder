package com.example.cunyclassroomfinder.ui.scrape

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.cunyclassroomfinder.ui.AppViewModelProvider
import com.example.cunyclassroomfinder.ui.components.OptionDropDown
import com.example.cunyclassroomfinder.ui.components.displaySchoolName

@Composable
fun ScrapeScreen(
    modifier: Modifier = Modifier,
    viewModel: ScrapeScreenViewModel = viewModel(factory = AppViewModelProvider.factory)
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadSchoolsAndTerms()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Download class schedules",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Pick a school and term to download in-person class times from CUNY Global Search. " +
                "This replaces any data already saved for that school.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        if (uiState.availableSchools.isNotEmpty() && uiState.availableTerms.isNotEmpty()) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OptionDropDown(
                        label = "School",
                        options = uiState.availableSchools,
                        selected = uiState.currentSchool.takeIf { it.value.isNotEmpty() },
                        optionLabel = { displaySchoolName(it.selectedName) },
                        onSelect = viewModel::setCurrentSchool,
                        enabled = !uiState.isLoading
                    )
                    OptionDropDown(
                        label = "Term",
                        options = uiState.availableTerms,
                        selected = uiState.currentTerm.takeIf { it.value.isNotEmpty() },
                        optionLabel = { it.selectedName },
                        onSelect = viewModel::setCurrentTerm,
                        enabled = !uiState.isLoading
                    )
                    Button(
                        onClick = viewModel::getAvailableCourses,
                        enabled = !uiState.isLoading &&
                            uiState.currentSchool.value.isNotEmpty() &&
                            uiState.currentTerm.value.isNotEmpty(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp)
                            .height(52.dp)
                    ) {
                        Icon(Icons.Filled.Refresh, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Download classes")
                    }
                }
            }
        }

        if (uiState.isLoading) {
            StatusCard(
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
            ) {
                Text(uiState.loadingMessage, style = MaterialTheme.typography.bodyLarge)
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
        }

        uiState.errorMessage?.let { error ->
            StatusCard(
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer
            ) {
                IconMessage(Icons.Filled.Warning, error)
                // Schools never loaded, so there's nothing else on screen to retry with
                if (uiState.availableSchools.isEmpty() && !uiState.isLoading) {
                    FilledTonalButton(onClick = viewModel::loadSchoolsAndTerms) {
                        Text("Retry")
                    }
                }
            }
        }

        if (!uiState.isLoading && uiState.scrapedSubjects.isNotEmpty()) {
            StatusCard(
                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                contentColor = MaterialTheme.colorScheme.onTertiaryContainer
            ) {
                IconMessage(
                    Icons.Filled.CheckCircle,
                    "Downloaded ${uiState.scrapedSubjects.size} subjects."
                )
                if (uiState.failedToScrapeSubjects.isNotEmpty()) {
                    Text(
                        text = "${uiState.failedToScrapeSubjects.size} subjects failed: " +
                            uiState.failedToScrapeSubjects.joinToString { it.first },
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
private fun StatusCard(
    containerColor: Color,
    contentColor: Color,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            content = content
        )
    }
}

@Composable
private fun IconMessage(icon: ImageVector, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(icon, contentDescription = null)
        Text(text, style = MaterialTheme.typography.bodyLarge)
    }
}
