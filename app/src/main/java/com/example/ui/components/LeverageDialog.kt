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
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.AmberGold
import com.example.ui.theme.CrimsonRed
import com.example.ui.theme.DarkObsidian
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun LeverageDialog(
    currentLeverage: Int,
    pairPrice: Double,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var selectedLeverage by remember { mutableFloatStateOf(currentLeverage.toFloat()) }
    val presets = listOf(5, 10, 20, 50, 100, 125)

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("leverage_dialog"),
            colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Adjust Leverage",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "${selectedLeverage.toInt()}x",
                    style = MaterialTheme.typography.displayLarge.copy(
                        color = NeonGreen,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.ExtraBold
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                Slider(
                    value = selectedLeverage,
                    onValueChange = { selectedLeverage = it },
                    valueRange = 1f..125f,
                    steps = 124,
                    colors = SliderDefaults.colors(
                        thumbColor = NeonGreen,
                        activeTrackColor = NeonGreen,
                        inactiveTrackColor = SlateBorder
                    ),
                    modifier = Modifier.testTag("leverage_slider")
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Presets
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    presets.forEach { p ->
                        Button(
                            onClick = { selectedLeverage = p.toFloat() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selectedLeverage.toInt() == p) NeonGreen else DarkObsidian,
                                contentColor = if (selectedLeverage.toInt() == p) DarkObsidian else TextPrimary
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 2.dp)
                                .height(32.dp),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "${p}x",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Estimated Liquidation Price preview
                val estLiqLong = pairPrice * (1.0 - (1.0 / selectedLeverage) * 0.9)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(DarkObsidian, RoundedCornerShape(8.dp))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Est. Long Liquidation:", style = MaterialTheme.typography.bodyMedium)
                    Text(
                        text = String.format("$%.2f", estLiqLong),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = CrimsonRed,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

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
                            onConfirm(selectedLeverage.toInt())
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonGreen, contentColor = DarkObsidian)
                    ) {
                        Text("Confirm", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
