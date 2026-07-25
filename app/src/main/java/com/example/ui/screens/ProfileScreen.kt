package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.theme.AmberGold
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.DarkObsidian
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun ProfileScreen() {
    val context = LocalContext.current
    var is2faEnabled by remember { mutableStateOf(true) }
    var isBiometricEnabled by remember { mutableStateOf(true) }
    var isOrderConfirmationEnabled by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkObsidian)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
            .testTag("profile_screen")
    ) {
        // User Trader Header
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = AmberGold.copy(alpha = 0.2f),
                    modifier = Modifier.size(52.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.VerifiedUser,
                            contentDescription = "VIP User",
                            tint = AmberGold,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Apex Institutional Trader", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(AmberGold)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                "VIP 3",
                                style = MaterialTheme.typography.labelSmall.copy(color = DarkObsidian, fontWeight = FontWeight.ExtraBold)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text("UID: 829104829 • Verified (KYC L2)", style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary, fontFamily = FontFamily.Monospace))
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Security & API Settings
        Text("Security & API Management", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))

        Spacer(modifier = Modifier.height(8.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                SettingsSwitchRow(
                    icon = Icons.Default.Shield,
                    title = "Two-Factor Authentication (2FA)",
                    subtitle = "Google Authenticator / YubiKey",
                    checked = is2faEnabled,
                    onCheckedChange = {
                        is2faEnabled = it
                        Toast.makeText(context, "2FA Setting Updated", Toast.LENGTH_SHORT).show()
                    }
                )

                SettingsSwitchRow(
                    icon = Icons.Default.Fingerprint,
                    title = "Biometric Passkey Login",
                    subtitle = "Face ID / Fingerprint prompt",
                    checked = isBiometricEnabled,
                    onCheckedChange = {
                        isBiometricEnabled = it
                        Toast.makeText(context, "Biometric Setting Updated", Toast.LENGTH_SHORT).show()
                    }
                )

                SettingsSwitchRow(
                    icon = Icons.Default.Security,
                    title = "Order Confirmation Popups",
                    subtitle = "Require explicit double-tap for orders > $10,000",
                    checked = isOrderConfirmationEnabled,
                    onCheckedChange = {
                        isOrderConfirmationEnabled = it
                        Toast.makeText(context, "Order Confirmation Updated", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // API Keys & Terminal Config
        Text("API Keys & MT5 Connectivity", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))

        Spacer(modifier = Modifier.height(8.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                SettingsActionRow(
                    icon = Icons.Default.Key,
                    title = "API Key Manager",
                    subtitle = "Create Read/Trade API keys for bots & MT5 EA",
                    onClick = {
                        Toast.makeText(context, "API Key Manager Opened", Toast.LENGTH_SHORT).show()
                    }
                )

                SettingsActionRow(
                    icon = Icons.Default.Lock,
                    title = "Anti-Phishing Code",
                    subtitle = "Configured: 'APEX-SAFE-882'",
                    onClick = {
                        Toast.makeText(context, "Anti-Phishing Code Configured", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }
    }
}

@Composable
private fun SettingsSwitchRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = icon, contentDescription = title, tint = NeonGreen)
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(title, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                Text(subtitle, style = MaterialTheme.typography.labelSmall.copy(color = TextMuted))
            }
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = DarkObsidian,
                checkedTrackColor = NeonGreen,
                uncheckedThumbColor = TextMuted,
                uncheckedTrackColor = DarkSurfaceElevated
            )
        )
    }
}

@Composable
private fun SettingsActionRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = icon, contentDescription = title, tint = AmberGold)
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(title, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                Text(subtitle, style = MaterialTheme.typography.labelSmall.copy(color = TextMuted))
            }
        }

        Icon(imageVector = Icons.Default.ChevronRight, contentDescription = "Open", tint = TextMuted)
    }
}
