package com.tidywork.c172checklist

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import java.nio.charset.Charset

data class Checklist(val aircraft: String, val disclaimer: String, val sections: List<Section>)
data class Section(val id: String, val title: String, val items: List<String>)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                ChecklistApp()
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChecklistApp() {
    val context = LocalContext.current
    // Load JSON from assets
    val jsonText = remember {
        context.assets.open("checklist.json").use { it.readBytes().toString(Charset.defaultCharset()) }
    }
    val checklist = remember(jsonText) { parseChecklist(jsonText) }

    val pagerState = rememberPagerState(pageCount = { checklist.sections.size })
    var currentTab by remember { mutableStateOf(0) }
    // Map to hold checked state for current tab only
    var checkedMap by remember { mutableStateOf<Map<Int, MutableList<Boolean>>>(emptyMap()) }

    // When tab changes, reset the previous tab's checks (i.e., do not carry state between tabs)
    LaunchedEffect(currentTab) {
        // Clear all stored states except the active tab to ensure per-tab freshness.
        checkedMap = mapOf()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("C172 Checklist") },
                actions = {
                    IconButton(onClick = {
                        // Reset checks for the current screen
                        checkedMap = mapOf()
                    }) {
                        Icon(Icons.Default.RestartAlt, contentDescription = "Reset")
                    }
                }
            )
        }
    ) { padding ->
        Column(Modifier.padding(padding)) {
            // Tabs
            ScrollableTabRow(selectedTabIndex = currentTab) {
                checklist.sections.forEachIndexed { index, section ->
                    Tab(
                        selected = currentTab == index,
                        onClick = { currentTab = index },
                        text = { Text(section.title) }
                    )
                }
            }
            // Content pager (swipeable)
            HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
                // Keep UI tab selection and pager synced
                LaunchedEffect(page) { if (currentTab != page) currentTab = page }

                val section = checklist.sections[page]
                val checks = remember(section.id) {
                    mutableStateListOf<Boolean>().apply {
                        repeat(section.items.size) { add(false) }
                    }
                }
                // Store in map so reset button clears
                SideEffect {
                    checkedMap = mapOf(page to checks)
                }

                SectionChecklist(section, checks)
            }
        }
    }
}

@Composable
fun SectionChecklist(section: Section, checks: MutableList<Boolean>) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        Text(section.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(6.dp))
        Text(
            "Training/reference only. Always follow your specific aircraft POH/AFM.",
            style = MaterialTheme.typography.bodySmall
        )
        Spacer(Modifier.height(12.dp))
        Column(Modifier.verticalScroll(rememberScrollState())) {
            section.items.forEachIndexed { idx, item ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Checkbox(
                        checked = checks[idx],
                        onCheckedChange = { checks[idx] = it }
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(item, style = MaterialTheme.typography.bodyLarge)
                }
                Divider()
            }
            Spacer(Modifier.height(100.dp))
        }
    }
}

fun parseChecklist(jsonText: String): Checklist {
    // Very small/naive parser using org.json to avoid extra deps
    try {
        val root = org.json.JSONObject(jsonText)
        val aircraft = root.getString("aircraft")
        val disclaimer = root.getString("disclaimer")
        val sectionsJson = root.getJSONArray("sections")
        val sections = buildList {
            for (i in 0 until sectionsJson.length()) {
                val s = sectionsJson.getJSONObject(i)
                val id = s.getString("id")
                val title = s.getString("title")
                val itemsJson = s.getJSONArray("items")
                val items = buildList {
                    for (j in 0 until itemsJson.length()) add(itemsJson.getString(j))
                }
                add(Section(id, title, items))
            }
        }
        return Checklist(aircraft, disclaimer, sections)
    } catch (e: Exception) {
        // Fallback basic list so the app still launches
        val defaultSections = listOf(
            Section("default", "Checklist", listOf("Example item 1", "Example item 2"))
        )
        return Checklist("C172", "Fallback", defaultSections)
    }
}
