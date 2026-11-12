package pl.kupimyto.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Dolny pasek: aktywna zakładka ma ikonę na zaokrąglonym jasnozielonym tle (jak w makiecie). */
@Composable
fun AppBottomBar(selected: Tab, onSelect: (Tab) -> Unit) {
    val cs = MaterialTheme.colorScheme
    Surface(color = cs.surfaceContainer) {
        Column {
            HorizontalDivider(color = cs.outlineVariant)
            Row(Modifier.fillMaxWidth().navigationBarsPadding().height(68.dp)) {
                Tab.entries.forEach { tab ->
                    val isSelected = tab == selected
                    Column(
                        Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .selectable(selected = isSelected, onClick = { onSelect(tab) }, role = Role.Tab),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Box(
                            Modifier
                                .size(width = 44.dp, height = 30.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) cs.primaryContainer else Color.Transparent),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                tab.icon,
                                contentDescription = tab.label,
                                modifier = Modifier.size(22.dp),
                                tint = if (isSelected) cs.onPrimaryContainer else cs.onSurfaceVariant,
                            )
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(
                            tab.label,
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                            color = if (isSelected) cs.onPrimaryContainer else cs.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}