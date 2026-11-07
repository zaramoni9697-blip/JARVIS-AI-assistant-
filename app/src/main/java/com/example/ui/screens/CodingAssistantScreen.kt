package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.coding.CodingUiState
import com.example.ui.coding.CodingViewModel
import com.example.ui.theme.JarvisBackground
import com.example.ui.theme.JarvisCardBg
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisNeonMint
import com.example.ui.theme.JarvisSurface
import com.example.ui.theme.JarvisTextPrimary
import com.example.ui.theme.JarvisTextSecondary
import com.example.ui.theme.JarvisWarningRed

@Composable
fun CodingAssistantScreen(
    codingViewModel: CodingViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    val uiState by codingViewModel.uiState.collectAsState()
    val selectedAction by codingViewModel.selectedAction.collectAsState()
    val queryInput by codingViewModel.queryInput.collectAsState()
    val codeSnippetInput by codingViewModel.codeSnippetInput.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        JarvisBackground,
                        Color(0xFF071426),
                        JarvisBackground
                    )
                )
            )
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = JarvisSurface.copy(alpha = 0.95f),
                border = BorderStroke(1.dp, Color(0xFF1B365D))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF10223D))
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = JarvisCyan
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "AI CODING ARCHITECT",
                                color = JarvisCyan,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "Code Generation • Bug Detection • Explanation",
                                color = JarvisNeonMint,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    IconButton(
                        onClick = { codingViewModel.clear() },
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF10223D))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Reset Form",
                            tint = JarvisTextSecondary
                        )
                    }
                }
            }

            // Scrollable Content
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Action Mode Selector
                item {
                    Text(
                        text = "SELECT PROTOCOL",
                        color = JarvisTextSecondary,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CodingActionChip(
                            label = "Generate",
                            icon = Icons.Default.Code,
                            isSelected = selectedAction == "generate",
                            modifier = Modifier.weight(1f),
                            onClick = { codingViewModel.setAction("generate") }
                        )
                        CodingActionChip(
                            label = "Explain",
                            icon = Icons.Default.Psychology,
                            isSelected = selectedAction == "explain",
                            modifier = Modifier.weight(1f),
                            onClick = { codingViewModel.setAction("explain") }
                        )
                        CodingActionChip(
                            label = "Debug",
                            icon = Icons.Default.BugReport,
                            isSelected = selectedAction == "debug",
                            modifier = Modifier.weight(1f),
                            onClick = { codingViewModel.setAction("debug") }
                        )
                        CodingActionChip(
                            label = "Optimize",
                            icon = Icons.Default.Speed,
                            isSelected = selectedAction == "optimize",
                            modifier = Modifier.weight(1f),
                            onClick = { codingViewModel.setAction("optimize") }
                        )
                    }
                }

                // Query / Instructions Input
                item {
                    Text(
                        text = "TASK DESCRIPTION / PROMPT",
                        color = JarvisTextSecondary,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = queryInput,
                        onValueChange = { codingViewModel.setQuery(it) },
                        placeholder = {
                            Text(
                                text = when (selectedAction) {
                                    "generate" -> "e.g. Write a Kotlin Compose biometric auth manager with coroutines..."
                                    "explain" -> "e.g. Explain how this algorithm handles concurrency and memory..."
                                    "debug" -> "e.g. Find race conditions and memory leaks in this code..."
                                    else -> "e.g. Optimize this function for zero allocations..."
                                },
                                color = JarvisTextSecondary.copy(alpha = 0.6f),
                                fontSize = 12.sp
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("coding_prompt_input"),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = JarvisTextPrimary,
                            unfocusedTextColor = JarvisTextPrimary,
                            focusedContainerColor = JarvisCardBg,
                            unfocusedContainerColor = JarvisCardBg,
                            focusedBorderColor = JarvisCyan,
                            unfocusedBorderColor = Color(0xFF1B365D)
                        ),
                        maxLines = 4
                    )
                }

                // Optional Code Snippet Input
                item {
                    Text(
                        text = "CODE SNIPPET (OPTIONAL)",
                        color = JarvisTextSecondary,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = codeSnippetInput,
                        onValueChange = { codingViewModel.setCodeSnippet(it) },
                        placeholder = {
                            Text(
                                text = "Paste code snippet to analyze or review here...",
                                color = JarvisTextSecondary.copy(alpha = 0.5f),
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                            .testTag("coding_snippet_input"),
                        shape = RoundedCornerShape(12.dp),
                        textStyle = androidx.compose.ui.text.TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = JarvisTextPrimary,
                            unfocusedTextColor = JarvisTextPrimary,
                            focusedContainerColor = JarvisCardBg,
                            unfocusedContainerColor = JarvisCardBg,
                            focusedBorderColor = JarvisCyan,
                            unfocusedBorderColor = Color(0xFF1B365D)
                        )
                    )
                }

                // Execute Button
                item {
                    val isLoading = uiState is CodingUiState.Loading
                    Button(
                        onClick = { codingViewModel.executeCodingTask() },
                        enabled = !isLoading && (queryInput.isNotBlank() || codeSnippetInput.isNotBlank()),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("coding_execute_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = JarvisCyan,
                            contentColor = Color(0xFF001B24),
                            disabledContainerColor = Color(0xFF003845)
                        )
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color(0xFF001B24),
                                strokeWidth = 2.5.dp
                            )
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AutoFixHigh,
                                    contentDescription = "Execute",
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "EXECUTE NEURAL CODE COMPILER",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    letterSpacing = 1.sp
                                )
                            }
                        }
                    }
                }

                // Output / Results Card
                item {
                    when (val state = uiState) {
                        is CodingUiState.Success -> {
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = JarvisCardBg),
                                border = BorderStroke(1.dp, JarvisCyan.copy(alpha = 0.8f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = "Success",
                                                tint = JarvisNeonMint,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "COMPILATION SUCCESS",
                                                color = JarvisNeonMint,
                                                fontSize = 12.sp,
                                                fontFamily = FontFamily.Monospace,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }

                                        IconButton(
                                            onClick = {
                                                clipboardManager.setText(AnnotatedString(state.response))
                                                Toast.makeText(context, "Code copied to clipboard", Toast.LENGTH_SHORT).show()
                                            },
                                            modifier = Modifier.size(34.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.ContentCopy,
                                                contentDescription = "Copy",
                                                tint = JarvisCyan,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color(0xFF040A14),
                                        border = BorderStroke(1.dp, Color(0xFF132A4A)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = state.response,
                                            color = JarvisTextPrimary,
                                            fontSize = 12.sp,
                                            fontFamily = FontFamily.Monospace,
                                            lineHeight = 18.sp,
                                            modifier = Modifier.padding(12.dp)
                                        )
                                    }
                                }
                            }
                        }

                        is CodingUiState.Error -> {
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = JarvisCardBg),
                                border = BorderStroke(1.dp, JarvisWarningRed),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = "COMPILATION ERROR",
                                        color = JarvisWarningRed,
                                        fontSize = 12.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = state.message,
                                        color = JarvisTextPrimary,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }

                        else -> {}
                    }
                }
            }
        }
    }
}

@Composable
private fun CodingActionChip(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = if (isSelected) JarvisCyan.copy(alpha = 0.2f) else JarvisCardBg,
        border = BorderStroke(1.dp, if (isSelected) JarvisCyan else Color(0xFF1B365D)),
        modifier = modifier
            .height(56.dp)
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isSelected) JarvisCyan else JarvisTextSecondary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                color = if (isSelected) JarvisCyan else JarvisTextSecondary,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}
