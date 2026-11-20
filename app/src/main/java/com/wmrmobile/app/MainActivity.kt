@Composable
private fun WmrHome(onChoosePhoto: () -> Unit, onOpenEditor: () -> Unit) {
    val bg = Color(0xFF050506)
    val panel = Color(0xFF151518)
    val red = Color(0xFFFF4655)
    Surface(modifier = Modifier.fillMaxSize(), color = bg) {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 18.dp, vertical = 18.dp), verticalArrangement = Arrangement.spacedBy(16.dp) ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("WMR", color = Color.White, style = MaterialTheme.typography.headlineLarge)
                    Text("MOBILE STUDIO", color = Color(0xFF8D8D96), style = MaterialTheme.typography.labelSmall)
                }
                Surface(color = Color(0xFF28221A), shape = RoundedCornerShape(50)) {
                    Text("♛  PREMIUM", color = Color(0xFFDDBB69), modifier = Modifier.padding(horizontal = 13.dp, vertical = 9.dp), style = MaterialTheme.typography.labelMedium)
                }
            }
            Surface(color = red, shape = RoundedCornerShape(22.dp), modifier = Modifier.fillMaxWidth().clickable { onChoosePhoto() }) {
                Column(Modifier.padding(22.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Text("✦  CREATE", color = Color.White, style = MaterialTheme.typography.labelLarge)
                    Text("Edit a photo", color = Color.White, style = MaterialTheme.typography.headlineSmall)
                    Text("Choose an image to start your edit  →", color = Color.White.copy(alpha = 0.85f))
                }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Surface(color = Color.White, shape = RoundedCornerShape(18.dp), modifier = Modifier.weight(1f).height(100.dp).clickable { onChoosePhoto() }) {
                    Column(Modifier.padding(15.dp), verticalArrangement = Arrangement.SpaceBetween) {
                        Text("▣", color = red, style = MaterialTheme.typography.headlineSmall)
                        Text("PHOTO", color = Color.Black, style = MaterialTheme.typography.titleMedium)
                    }
                }
                Surface(color = panel, shape = RoundedCornerShape(18.dp), modifier = Modifier.weight(1f).height(100.dp).clickable { onOpenEditor() }) {
                    Column(Modifier.padding(15.dp), verticalArrangement = Arrangement.SpaceBetween) {
                        Text("◉", color = red, style = MaterialTheme.typography.headlineSmall)
                        Text("EDITOR", color = Color.White, style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("TOOLS", color = Color.White, style = MaterialTheme.typography.titleMedium)
                    Text("CURRENT FEATURES", color = Color(0xFF777780), style = MaterialTheme.typography.labelSmall)
                }
                val tools = listOf("Adjust", "Filters", "Natural Smile", "Before / After", "Full-size Export", "Reset")
                tools.chunked(3).forEach { row ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                        row.forEach { tool ->
                            Surface(color = panel, shape = RoundedCornerShape(15.dp), modifier = Modifier.weight(1f).height(86.dp).clickable { onOpenEditor() }) {
                                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.SpaceBetween) {
                                    Text("✦", color = red)
                                    Text(tool, color = Color.White, style = MaterialTheme.typography.labelMedium)
                                }
                            }
                        }
                        if (row.size < 3) repeat(3-row.size) { Spacer(Modifier.weight(1f)) }
                    }
                }
            }
            Surface(color = panel, shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("NATURAL RETOUCH", color = Color(0xFFDDBB69), style = MaterialTheme.typography.labelMedium)
                    Text("Subtle edits. Your original stays untouched.", color = Color.White)
                    Text("Preview edits and export a full-resolution copy.", color = Color(0xFF8D8D96), style = MaterialTheme.typography.bodySmall)
                }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                Text("HOME     •     PHOTO STUDIO", color = Color(0xFF777780), style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}
@Composable
private fun WmrHome(onChoosePhoto: () -> Unit, onOpenEditor: () -> Unit) {
    val bg = Color(0xFF050506)
    val panel = Color(0xFF141416)
    val red = Color(0xFFFF4655)
    val gold = Color(0xFFD9B66A)
    Surface(modifier = Modifier.fillMaxSize(), color = bg) {
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 18.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                    Text("WMR", color = Color.White, style = MaterialTheme.typography.headlineLarge)
                    Text("MOBILE STUDIO", color = Color(0xFF92929A), style = MaterialTheme.typography.labelSmall)
                }
                Surface(color = Color(0xFF28221A), shape = RoundedCornerShape(50)) {
                    Text("♛ PREMIUM", color = gold, modifier = Modifier.padding(horizontal = 13.dp, vertical = 8.dp), style = MaterialTheme.typography.labelMedium)
                }
            }
            Surface(color = Color(0xFF18181B), shape = RoundedCornerShape(24.dp), modifier = Modifier.fillMaxWidth().clickable { onChoosePhoto() }) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                    Text("YOUR CREATIVE SPACE", color = red, style = MaterialTheme.typography.labelMedium)
                    Text("Make every photo\nfeel like you.", color = Color.White, style = MaterialTheme.typography.headlineMedium)
                    Text("Natural edits. Clean details. Your original identity stays yours.", color = Color(0xFFB2B2BA), style = MaterialTheme.typography.bodyMedium)
                    Surface(color = red, shape = RoundedCornerShape(12.dp)) {
                        Text("＋  START EDITING", color = Color.White, modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp), style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Surface(color = Color.White, shape = RoundedCornerShape(18.dp), modifier = Modifier.weight(1f).height(108.dp).clickable { onChoosePhoto() }) {
                    Column(Modifier.padding(15.dp), verticalArrangement = Arrangement.SpaceBetween) {
                        Text("▧", color = red, style = MaterialTheme.typography.headlineSmall)
                        Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                            Text("PHOTO", color = Color.Black, style = MaterialTheme.typography.titleMedium)
                            Text("Choose from device", color = Color(0xFF66666B), style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
                Surface(color = panel, shape = RoundedCornerShape(18.dp), modifier = Modifier.weight(1f).height(108.dp).clickable { onOpenEditor() }) {
                    Column(Modifier.padding(15.dp), verticalArrangement = Arrangement.SpaceBetween) {
                        Text("◉", color = red, style = MaterialTheme.typography.headlineSmall)
                        Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                            Text("EDITOR", color = Color.White, style = MaterialTheme.typography.titleMedium)
                            Text("Open your workspace", color = Color(0xFF888890), style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("QUICK TOOLS", color = Color.White, style = MaterialTheme.typography.titleMedium)
                    Text("8 TOOLS", color = Color(0xFF7D7D85), style = MaterialTheme.typography.labelSmall)
                }
                val tools = listOf("Adjust", "Filters", "Natural Smile", "Retouch", "Before / After", "Export", "Portrait", "Reset")
                val symbols = listOf("☼", "◐", "☺", "✦", "⇄", "↗", "◇", "↺")
                tools.chunked(4).forEachIndexed { rowIndex, row ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        row.forEachIndexed { colIndex, tool ->
                            val index = rowIndex * 4 + colIndex
                            Surface(color = panel, shape = RoundedCornerShape(15.dp), modifier = Modifier.weight(1f).height(86.dp).clickable { onOpenEditor() }) {
                                Column(Modifier.padding(11.dp), verticalArrangement = Arrangement.SpaceBetween) {
                                    Text(symbols[index], color = red, style = MaterialTheme.typography.titleMedium)
                                    Text(tool, color = Color.White, style = MaterialTheme.typography.labelMedium)
                                }
                            }
                        }
                    }
                }
            }
            Surface(color = Color(0xFF1A1713), shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth().clickable { onChoosePhoto() }) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Text("✦  RETOUCH & BEAUTIFY", color = gold, style = MaterialTheme.typography.labelMedium)
                    Text("Small changes. Natural results.", color = Color.White, style = MaterialTheme.typography.titleMedium)
                    Text("Try subtle adjustments and keep the photo looking like you.", color = Color(0xFFB5B0A6), style = MaterialTheme.typography.bodySmall)
                    Text("EXPLORE TOOLS  →", color = gold, style = MaterialTheme.typography.labelMedium)
                }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                Text("⌂  HOME", color = Color.White, style = MaterialTheme.typography.labelSmall)
                Text("▧  PHOTOS", color = Color(0xFF777780), style = MaterialTheme.typography.labelSmall)
                Text("✦  EDITOR", color = Color(0xFF777780), style = MaterialTheme.typography.labelSmall, modifier = Modifier.clickable { onOpenEditor() })
                Text("◉  PROFILE", color = Color(0xFF777780), style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}