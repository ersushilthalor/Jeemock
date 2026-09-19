package com.example.ui.components

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.ApiKeyPreferences
import com.example.ui.common.AppLanguage
import com.example.ui.theme.BrownBorder
import com.example.ui.theme.BrownPrimary
import com.example.ui.theme.BrownSecondary
import com.example.ui.theme.BrownTextBody
import com.example.ui.theme.BrownTextMuted
import com.example.ui.theme.BrownTextTitle
import com.example.ui.theme.PureWhite
import com.example.ui.theme.StatusCrimson
import com.example.ui.theme.StatusEmerald

@Composable
fun ApiKeyDialog(
    isOpen: Boolean,
    appLanguage: AppLanguage,
    onDismiss: () -> Unit,
    onSaveKey: (String) -> Unit,
    onClearKey: () -> Unit,
    onUseOfflineMode: (() -> Unit)? = null
) {
    if (!isOpen) return

    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val uriHandler = LocalUriHandler.current

    val currentKey = remember(isOpen) { ApiKeyPreferences.getApiKey(context) }
    var inputKey by remember(isOpen) { mutableStateOf(currentKey) }
    var isPasswordVisible by remember { mutableStateOf(false) }
    val isConfigured = ApiKeyPreferences.hasApiKey(context)

    val isEn = appLanguage == AppLanguage.ENGLISH

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = PureWhite,
        shape = RoundedCornerShape(20.dp),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFFF8E1)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Key,
                        contentDescription = "API Key",
                        tint = Color(0xFFD97706),
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = if (isEn) "Gemini API Key Setup" else "जेमिनी एपीआई कुंजी दर्ज करें",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = BrownTextTitle
                    )
                    Text(
                        text = if (isConfigured) {
                            if (isEn) "Status: Configured ✓" else "स्थिति: सक्रिय ✓"
                        } else {
                            if (isEn) "Status: Not Configured" else "स्थिति: कॉन्फ़िगर नहीं है"
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (isConfigured) StatusEmerald else StatusCrimson
                    )
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = if (isEn)
                        "To generate unlimited real-time AI mock tests on your device, provide your Google Gemini API key. It's completely free!"
                    else
                        "अपने डिवाइस पर अनलिमिटेड रीयल-टाइम एआई टेस्ट जनरेट करने के लिए अपनी गूगल जेमिनी एपीआई की दर्ज करें। यह पूरी तरह मुफ्त है!",
                    fontSize = 12.sp,
                    color = BrownTextBody,
                    lineHeight = 17.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Input Field with show/hide, paste, and clear
                OutlinedTextField(
                    value = inputKey,
                    onValueChange = { inputKey = it.trim() },
                    label = {
                        Text(
                            text = if (isEn) "Gemini API Key" else "जेमिनी एपीआई कुंजी",
                            fontSize = 12.sp
                        )
                    },
                    placeholder = {
                        Text(
                            text = "AIzaSy...",
                            fontSize = 12.sp,
                            color = BrownTextMuted
                        )
                    },
                    visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (inputKey.isNotEmpty()) {
                                IconButton(onClick = { inputKey = "" }) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Clear",
                                        tint = BrownTextMuted,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                Icon(
                                    imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = if (isPasswordVisible) "Hide Key" else "Show Key",
                                    tint = BrownTextMuted,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BrownPrimary,
                        unfocusedBorderColor = BrownBorder,
                        focusedLabelColor = BrownPrimary,
                        cursorColor = BrownPrimary
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("api_key_text_field")
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Helper Quick Buttons: Paste & Get Free Key
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Paste button
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFF5F5F5),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE0E0E0)),
                        modifier = Modifier
                            .clickable {
                                val clipText = clipboardManager.getText()?.text
                                if (!clipText.isNullOrBlank()) {
                                    inputKey = clipText.trim()
                                    Toast.makeText(context, if (isEn) "Pasted from clipboard" else "क्लिपबोर्ड से पेस्ट किया गया", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, if (isEn) "Clipboard is empty" else "क्लिपबोर्ड खाली है", Toast.LENGTH_SHORT).show()
                                }
                            }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentPaste,
                                contentDescription = "Paste",
                                tint = BrownPrimary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isEn) "Paste" else "पेस्ट करें",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = BrownPrimary
                            )
                        }
                    }

                    // Get Free Key link button
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFFFF8E1),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFD54F)),
                        modifier = Modifier
                            .clickable {
                                try {
                                    uriHandler.openUri("https://aistudio.google.com/app/apikey")
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Visit: https://aistudio.google.com/app/apikey", Toast.LENGTH_LONG).show()
                                }
                            }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.OpenInNew,
                                contentDescription = "Open Link",
                                tint = Color(0xFFB45309),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isEn) "Get Free Key" else "फ्री कुंजी प्राप्त करें",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFB45309)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Steps explanation card
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFFBF9F6),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BrownBorder)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = if (isEn) "How to get a key in 30 seconds:" else "30 सेकंड में फ्री की कैसे लें:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = BrownTextTitle
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isEn)
                                "1. Click 'Get Free Key' above to open Google AI Studio\n2. Click 'Create API Key' (free of charge)\n3. Copy and paste it here"
                            else
                                "1. ऊपर 'फ्री कुंजी प्राप्त करें' दबाकर Google AI Studio खोलें\n2. 'Create API Key' पर क्लिक करें (बिलकुल मुफ्त)\n3. कॉपी करके यहाँ पेस्ट कर दें",
                            fontSize = 10.sp,
                            color = BrownTextMuted,
                            lineHeight = 14.sp
                        )
                    }
                }

                // If user wants offline PYQ alternative
                if (onUseOfflineMode != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = {
                            onDismiss()
                            onUseOfflineMode()
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = BrownSecondary),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BrownSecondary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(38.dp)
                    ) {
                        Text(
                            text = if (isEn) "⚡ Use Offline Genuine PYQs (No API Key)" else "⚡ ऑफलाइन वास्तविक पीवाईक्यू टेस्ट (बिना की)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (inputKey.isNotBlank()) {
                        onSaveKey(inputKey)
                        Toast.makeText(
                            context,
                            if (isEn) "API Key saved successfully!" else "एपीआई कुंजी सफलतापूर्वक सुरक्षित कर दी गई!",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(
                            context,
                            if (isEn) "Please enter an API key" else "कृपया एक एपीआई कुंजी दर्ज करें",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = BrownPrimary),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("save_api_key_btn")
            ) {
                Text(
                    text = if (isEn) "Save Key" else "सुरक्षित करें",
                    fontWeight = FontWeight.Bold,
                    color = PureWhite
                )
            }
        },
        dismissButton = {
            Row {
                if (isConfigured) {
                    TextButton(
                        onClick = {
                            onClearKey()
                            inputKey = ""
                            Toast.makeText(
                                context,
                                if (isEn) "API Key removed" else "एपीआई कुंजी हटा दी गई",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = null,
                            tint = StatusCrimson,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isEn) "Remove" else "हटाएं",
                            color = StatusCrimson,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                TextButton(onClick = onDismiss) {
                    Text(
                        text = if (isEn) "Cancel" else "रद्द करें",
                        color = BrownTextMuted,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    )
}
