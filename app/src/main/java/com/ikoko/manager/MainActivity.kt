package com.ikoko.manager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Navy = Color(0xFF04142D)
private val Blue = Color(0xFF087CFF)
private val Cyan = Color(0xFF18E6E0)
private val Silver = Color(0xFFE8F1FF)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { ManagerApp() }
    }
}

private data class ToolItem(val title: String, val subtitle: String, val symbol: String)

@Composable
private fun ManagerApp() {
    val tools = listOf(
        ToolItem("File Manager", "Browse and manage files", "▤"),
        ToolItem("APK Analyzer", "Inspect APK metadata", "◈"),
        ToolItem("APKS Converter", "Extract split packages", "⇄"),
        ToolItem("XAPK / APKM", "Inspect and extract bundles", "▣"),
        ToolItem("Text Editor", "Edit text, JSON and XML", "⌘"),
        ToolItem("Hex Editor", "Inspect binary data", "01"),
        ToolItem("ZIP Studio", "Create and extract ZIP", "▧"),
        ToolItem("APK Signer", "Sign and verify packages", "✓"),
        ToolItem("DEX / Smali", "Bytecode tools roadmap", "⌬"),
        ToolItem("Resource Studio", "AXML / ARSC roadmap", "⚙"),
        ToolItem("Compare", "Compare files and hashes", "⇆"),
        ToolItem("Utilities", "Base64, hashes and encoders", "⌁")
    )
    var selected by remember { mutableStateOf<String?>(null) }
    val transition = rememberInfiniteTransition(label = "neon")
    val glow by transition.animateFloat(
        initialValue = 0.65f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1800), RepeatMode.Reverse),
        label = "glow"
    )
    MaterialTheme(colorScheme = darkColorScheme(
        primary = Cyan, secondary = Blue, background = Navy, surface = Color(0xFF081E3C),
        onPrimary = Navy, onBackground = Silver, onSurface = Silver
    )) {
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFF020711), Navy, Color(0xFF061C43))))) {
            Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().padding(16.dp)) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF071A37)),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier.fillMaxWidth().alpha(glow)
                ) {
                    Column(Modifier.padding(20.dp)) {
                        Text("GI", color = Silver, fontSize = 54.sp, fontWeight = FontWeight.Black)
                        Text("I KO KO MANAGER", color = Cyan, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                        Text("OFFLINE • ROOTLESS • ANDROID TOOLKIT", color = Silver, fontSize = 10.sp, letterSpacing = 1.4.sp)
                    }
                }
                Spacer(Modifier.height(18.dp))
                Text(selected ?: "WORKSPACE", color = Cyan, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(10.dp))
                LazyVerticalGrid(columns = GridCells.Fixed(2), modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(tools) { item ->
                        Card(
                            onClick = { selected = item.title },
                            colors = CardDefaults.cardColors(containerColor = Color(0xE60A2347)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Blue.copy(alpha = 0.7f)),
                            shape = RoundedCornerShape(18.dp),
                            modifier = Modifier.height(118.dp)
                        ) {
                            Column(Modifier.fillMaxSize().padding(13.dp), verticalArrangement = Arrangement.SpaceBetween) {
                                Text(item.symbol, color = Cyan, fontSize = 27.sp, fontWeight = FontWeight.Bold)
                                Column {
                                    Text(item.title, color = Silver, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text(item.subtitle, color = Color(0xFF9FB9D9), fontSize = 10.sp, lineHeight = 13.sp)
                                }
                            }
                        }
                    }
                }
                Text("Powered by I KO KO  •  Local-first tools", color = Color(0xFF85A8D4), fontSize = 10.sp, modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 10.dp))
            }
        }
    }
}
