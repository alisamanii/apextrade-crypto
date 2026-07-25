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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.data.model.Position
import com.example.ui.theme.CrimsonRed
import com.example.ui.theme.DarkObsidian
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun TpSlDialog(
    position: Position,
    onDismiss: () -> Unit,
    onConfirm: (Double?, Double?) -> Unit
) {
    var tpVal by remember { mutableStateOf(position.takeProfit?.toString() ?: "") }
    var slVal by remember { mutableStateOf(position.stopLoss?.toString() ?: "") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("tpsl_dialog"),
            colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder)
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    text = "Position TP/SL Setup (${position.pairId})",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Entry Price:", style = MaterialTheme.typography.bodyMedium)
                    Text(
                        text = "$${position.entryPrice}",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Mark Price:", style = MaterialTheme.typography.bodyMedium)
                    Text(
                        text = "$${position.markPrice}",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = NeonGreen
                        )
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Take Profit Field
                OutlinedTextField(
                    value = tpVal,
                    onValueChange = { tpVal = it },
                    label = { Text("Take Profit Price (USDT)", color = NeonGreen) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonGreen,
                        unfocusedBorderColor = SlateBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("tp_price_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Stop Loss Field
                OutlinedTextField(
                    value = slVal,
                    onValueChange = { slVal = it },
                    label = { Text("Stop Loss Price (USDT)", color = CrimsonRed) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CrimsonRed,
                        unfocusedBorderColor = SlateBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("sl_price_input")
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder)
                    ) {
                        Text("Cancel", color = TextSecondary)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        onClick = {
                            val tp = tpVal.toDoubleOrNull()
                            val sl = slVal.toDoubleOrNull()
                            onConfirm(tp, sl)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonGreen, contentColor = DarkObsidian)
                    ) {
                        Text("Save TP/SL", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
