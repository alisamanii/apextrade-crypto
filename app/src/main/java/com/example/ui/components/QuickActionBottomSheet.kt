package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.theme.DarkObsidian
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickActionBottomSheet(
    actionTitle: String,
    onDismiss: () -> Unit,
    onSubmit: (String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    var inputAddressOrAmount by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = DarkSurfaceElevated,
        scrimColor = DarkObsidian.copy(alpha = 0.7f),
        modifier = Modifier.testTag("quick_action_bottom_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Text(
                text = "$actionTitle Terminal",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = inputAddressOrAmount,
                onValueChange = { inputAddressOrAmount = it },
                label = { Text("Enter Amount or Wallet Address", color = TextSecondary) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonGreen,
                    unfocusedBorderColor = SlateBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                modifier = Modifier.fillMaxWidth().testTag("quick_action_input")
            )

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Button(
                    onClick = {
                        onSubmit(inputAddressOrAmount)
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonGreen, contentColor = DarkObsidian),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Text("Confirm $actionTitle", fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
