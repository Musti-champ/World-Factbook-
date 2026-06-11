package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LookupSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    matchCount: Int,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current

    Column(modifier = modifier.fillMaxWidth()) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = IntelSurface),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, IntelOnSurface.copy(alpha = 0.12f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search icon",
                    tint = IntelGold,
                    modifier = Modifier.size(24.dp)
                )

                Spacer(modifier = Modifier.width(4.dp))

                TextField(
                    value = query,
                    onValueChange = onQueryChange,
                    placeholder = {
                        Text(
                            text = "Probe archive (by name, capital, region)...",
                            color = IntelMuted,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("search_input"),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedTextColor = IntelOnSurface,
                        unfocusedTextColor = IntelOnSurface,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent,
                        cursorColor = IntelOnSurface
                    )
                )

                AnimatedVisibility(
                    visible = query.isNotEmpty(),
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    IconButton(
                        onClick = {
                            onQueryChange("")
                            focusManager.clearFocus()
                        },
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("clear_search_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear search",
                            tint = IntelAlert,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Real-time status update tag line
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "STATUS: ${if (query.isEmpty()) "ARCHIVE BROADCAST ACTIVE" else "FILTERING REAL-TIME"}",
                fontSize = 9.sp,
                fontWeight = FontWeight.Black,
                color = if (query.isEmpty()) IntelMuted else IntelSecondary,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 0.5.sp
            )

            // Results badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (matchCount > 0) IntelGoldLight else IntelAlert.copy(alpha = 0.15f))
                    .border(
                        BorderStroke(
                            1.dp,
                            if (matchCount > 0) IntelOnSurface.copy(alpha = 0.3f) else IntelAlert
                        ),
                        RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "$matchCount MATCHING RECORD${if (matchCount != 1) "S" else ""}",
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Black,
                    color = if (matchCount > 0) IntelOnSurface else IntelAlert,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}
