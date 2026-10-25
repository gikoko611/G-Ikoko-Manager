package com.ikoko.manager

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.security.MessageDigest
import java.util.zip.ZipInputStream
import java.io.File
import java.io.ByteArrayOutputStream
import android.content.pm.PackageManager
import android.util.Base64

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
        ToolItem("File Manager", "Browse and select files", "▤"),
        ToolItem("APK Analyzer", "Select an APK for inspection", "◈"),
        ToolItem("APKS Converter", "Select split APK packages", "⇄"),
        ToolItem("XAPK / APKM", "Select an app bundle", "▣"),
        ToolItem("Text Editor", "Edit and save text files", "⌘"),
        ToolItem("Hex Editor", "Select a binary file", "01"),
        ToolItem("ZIP Studio", "Select an archive", "▧"),
        ToolItem("APK Signer", "Choose an APK to work with", "✓"),
        ToolItem("DEX / Smali", "Choose bytecode-related files", "⌬"),
        ToolItem("Resource Studio", "Choose Android resource files", "⚙"),
        ToolItem("Compare", "Select files to compare", "⇆"),
        ToolItem("Utilities", "Base64 and SHA-256 tools", "⌁")
    )
    var selected by remember { mutableStateOf<ToolItem?>(null) }
    val transition = rememberInfiniteTransition(label = "neon")
    val glow by transition.animateFloat(
        initialValue = 0.72f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1800), RepeatMode.Reverse),
        label = "glow"
    )
    MaterialTheme(colorScheme = darkColorScheme(
        primary = Cyan, secondary = Blue, background = Navy,
        surface = Color(0xFF081E3C), onPrimary = Navy,
        onBackground = Silver, onSurface = Silver
    )) {
        Box(Modifier.fillMaxSize().background(
            Brush.verticalGradient(listOf(Color(0xFF020711), Navy, Color(0xFF061C43)))
        )) {
            if (selected == null) {
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
                    Text("WORKSPACE", color = Cyan, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(10.dp))
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(tools) { item ->
                            Card(
                                onClick = { selected = item },
                                colors = CardDefaults.cardColors(containerColor = Color(0xE60A2347)),
                                border = BorderStroke(1.dp, Blue.copy(alpha = 0.7f)),
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
                    Text("Powered by I KO KO • Local-first tools", color = Color(0xFF85A8D4), fontSize = 10.sp, modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 10.dp))
                }
            } else {
                FeatureScreen(tool = selected!!, onBack = { selected = null })
            }
        }
    }
}

@Composable
private fun FeatureScreen(tool: ToolItem, onBack: () -> Unit) {
    val context = LocalContext.current
    var editorText by remember(tool.title) { mutableStateOf("") }
    var utilityInput by remember(tool.title) { mutableStateOf("") }
    var result by remember(tool.title) { mutableStateOf("") }
    var pickedUri by remember(tool.title) { mutableStateOf<Uri?>(null) }

    val openFile = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            pickedUri = uri
            runCatching { context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION) }
            if (tool.title == "Text Editor") {
                editorText = runCatching {
                    context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }.orEmpty()
                }.getOrElse { "Could not read this file as text: ${it.message}" }
            }
            result = when (tool.title) {
                "APK Analyzer" -> runCatching {
                    val temp = File.createTempFile("ikoko-apk-", ".apk", context.cacheDir)
                    context.contentResolver.openInputStream(uri)?.use { input -> temp.outputStream().use { input.copyTo(it) } }
                    val info = context.packageManager.getPackageArchiveInfo(temp.absolutePath, PackageManager.GET_PERMISSIONS)
                        ?: error("Android could not parse this APK")
                    info.applicationInfo?.sourceDir = temp.absolutePath
                    info.applicationInfo?.publicSourceDir = temp.absolutePath
                    val label = info.applicationInfo?.loadLabel(context.packageManager)?.toString() ?: "(unknown)"
                    val permissions = info.requestedPermissions?.joinToString("\\n") ?: "(none declared)"
                    val digest = MessageDigest.getInstance("SHA-256").digest(temp.readBytes()).joinToString("") { "%02x".format(it) }
                    temp.delete()
                    "APK ANALYSIS\\nApp: $label\\nPackage: ${info.packageName}\\nVersion: ${info.versionName ?: "(unknown)"}\\nVersion code: ${info.longVersionCode}\\nRequested permissions:\\n$permissions\\n\\nSHA-256: $digest"
                }.getOrElse { "APK analysis failed: ${it.message}" }
                "Hex Editor" -> runCatching {
                    val bytes = context.contentResolver.openInputStream(uri)?.use { input ->\n                        val output = ByteArrayOutputStream()\n                        val buffer = ByteArray(512)\n                        val count = input.read(buffer)\n                        if (count > 0) output.write(buffer, 0, count)\n                        output.toByteArray()\n                    } ?: byteArrayOf()
                    val hex = bytes.indices.joinToString(" ") { "%02X".format(bytes[it]) }
                    val ascii = bytes.joinToString("") { b -> val n = b.toInt() and 0xff; if (n in 32..126) n.toChar().toString() else "." }
                    "HEX PREVIEW (first 512 bytes max)\\nPreview bytes: ${bytes.size}\\n\\n$hex\\n\\nASCII:\\n$ascii"
                }.getOrElse { "Hex preview failed: ${it.message}" }
                "ZIP Studio", "APKS Converter", "XAPK / APKM" -> runCatching {
                    val entries = mutableListOf<String>()
                    context.contentResolver.openInputStream(uri)?.use { stream ->
                        ZipInputStream(stream).use { zip ->
                            var entry = zip.nextEntry
                            while (entry != null && entries.size < 500) {
                                entries.add((if (entry.isDirectory) "[DIR] " else "") + entry.name + if (entry.size >= 0) "  (${entry.size} bytes)" else "")
                                entry = zip.nextEntry
                            }
                        }
                    } ?: error("Cannot open selected archive")
                    "ARCHIVE CONTENTS (maximum 500 entries)\\nEntries listed: ${entries.size}\\n\\n" + entries.joinToString("\\n")
                }.getOrElse { "Archive listing failed: ${it.message}" }
                else -> "Selected file:\\n$uri"
            }
            Toast.makeText(context, "File selected", Toast.LENGTH_SHORT).show()
        }
    }
    val saveText = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/plain")) { uri ->
        if (uri != null) {
            val saved = runCatching {
                context.contentResolver.openOutputStream(uri)?.bufferedWriter()?.use { it.write(editorText) }
                true
            }.getOrDefault(false)
            Toast.makeText(context, if (saved) "File saved" else "Save failed", Toast.LENGTH_SHORT).show()
            if (saved) result = "Saved to:\n$uri"
        }
    }

    val description = when (tool.title) {
        "File Manager" -> "Use Android's system file picker to browse files you have access to. Root access is not required."
        "APK Analyzer" -> "Inspect APK package name, app label, version, requested permissions and SHA-256 locally. This is metadata inspection, not a full manifest editor."
        "APKS Converter" -> "Inspect the contents of an .apks archive. Converting split APKs into an installable package is not included yet."
        "XAPK / APKM" -> "Choose an XAPK/APKM bundle. Extraction and installation support are not implemented in this initial screen."
        "Text Editor" -> "Open a text file, edit its contents, then save a copy. Very large files may not fit in memory."
        "Hex Editor" -> "View a hexadecimal and ASCII preview of the first 512 bytes of a selected file. Binary editing is not included yet."
        "ZIP Studio" -> "List ZIP archive entries and reported sizes. Extraction and archive creation are not included yet."
        "APK Signer" -> "Choose an APK. Cryptographic APK signing and signature verification are not implemented yet."
        "DEX / Smali" -> "Choose a DEX or Smali file. Disassembly and assembly engines are not included yet."
        "Resource Studio" -> "Choose an Android resource file. AXML/ARSC decoding is not implemented yet."
        "Compare" -> "Choose files to inspect. Full side-by-side comparison is not implemented yet."
        else -> "Encode/decode Base64 text or calculate a SHA-256 digest locally on this device."
    }

    Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(onClick = onBack) { Text("← Back") }
            Column(Modifier.weight(1f)) {
                Text(tool.title, color = Cyan, fontSize = 21.sp, fontWeight = FontWeight.Bold)
                Text("I KO KO MANAGER", color = Color(0xFF9FB9D9), fontSize = 10.sp)
            }
        }
        Spacer(Modifier.height(16.dp))
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xE60A2347)),
            border = BorderStroke(1.dp, Blue.copy(alpha = 0.7f)),
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier.fillMaxWidth().weight(1f)
        ) {
            Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
                Text(description, color = Silver, fontSize = 14.sp, lineHeight = 21.sp)
                Spacer(Modifier.height(18.dp))

                when (tool.title) {
                    "Text Editor" -> {
                        OutlinedButton(onClick = { openFile.launch(arrayOf("text/*", "application/json", "application/xml")) }, modifier = Modifier.fillMaxWidth()) { Text("Open text file") }
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = editorText, onValueChange = { editorText = it },
                            label = { Text("Text content") }, modifier = Modifier.fillMaxWidth().heightIn(min = 180.dp),
                            minLines = 8
                        )
                        Spacer(Modifier.height(8.dp))
                        Button(onClick = { saveText.launch("ikoko-edited.txt") }, modifier = Modifier.fillMaxWidth()) { Text("Save as new file") }
                    }
                    "Utilities" -> {
                        OutlinedTextField(value = utilityInput, onValueChange = { utilityInput = it }, label = { Text("Input text") }, modifier = Modifier.fillMaxWidth(), minLines = 3)
                        Spacer(Modifier.height(8.dp))
                        Button(onClick = { result = Base64.encodeToString(utilityInput.toByteArray(Charsets.UTF_8), Base64.NO_WRAP) }, modifier = Modifier.fillMaxWidth()) { Text("Encode Base64") }
                        OutlinedButton(onClick = {
                            result = runCatching { String(Base64.decode(utilityInput, Base64.DEFAULT), Charsets.UTF_8) }
                                .getOrElse { "Invalid Base64 input: ${it.message}" }
                        }, modifier = Modifier.fillMaxWidth()) { Text("Decode Base64") }
                        OutlinedButton(onClick = {
                            result = MessageDigest.getInstance("SHA-256").digest(utilityInput.toByteArray(Charsets.UTF_8))
                                .joinToString("") { "%02x".format(it) }
                        }, modifier = Modifier.fillMaxWidth()) { Text("Calculate SHA-256") }
                    }
                    else -> {
                        Button(onClick = {
                            val types = when (tool.title) {
                                "APK Analyzer", "APK Signer" -> arrayOf("application/vnd.android.package-archive", "application/octet-stream")
                                "APKS Converter", "XAPK / APKM", "ZIP Studio" -> arrayOf("application/zip", "application/octet-stream", "application/x-zip-compressed")
                                "DEX / Smali" -> arrayOf("application/octet-stream", "text/*")
                                "Resource Studio", "Hex Editor", "Compare" -> arrayOf("*/*")
                                else -> arrayOf("*/*")
                            }
                            openFile.launch(types)
                        }, modifier = Modifier.fillMaxWidth()) { Text("Choose file") }
                        if (tool.title == "Compare") {
                            Spacer(Modifier.height(8.dp))
                            Text("For now, choose a file to confirm file access. Comparing two files will be added with the comparison engine.", color = Color(0xFF9FB9D9), fontSize = 12.sp)
                        }
                    }
                }

                if (result.isNotBlank()) {
                    Spacer(Modifier.height(18.dp))
                    Text("RESULT", color = Cyan, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Spacer(Modifier.height(6.dp))
                    SelectionContainer {
                        Text(result, color = Silver, fontSize = 12.sp, lineHeight = 17.sp)
                    }
                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(onClick = {
                        val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                        clipboard.setPrimaryClip(android.content.ClipData.newPlainText("I KO KO Manager result", result))
                        Toast.makeText(context, "Copied", Toast.LENGTH_SHORT).show()
                    }) { Text("Copy result") }
                }
                pickedUri?.let {
                    Spacer(Modifier.height(8.dp))
                    Text("File access granted for this selection.", color = Color(0xFF9FB9D9), fontSize = 11.sp)
                }
            }
        }
        Text("Powered by I KO KO", color = Color(0xFF85A8D4), fontSize = 10.sp, modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 10.dp))
    }
}
