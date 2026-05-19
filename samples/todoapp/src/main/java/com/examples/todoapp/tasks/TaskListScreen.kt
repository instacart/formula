package com.examples.todoapp.tasks

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Checkbox
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.instacart.formula.invoke

@Composable
fun TaskListScreen(model: TaskListRenderModel) {
    MaterialTheme {
        Surface {
            Scaffold(
                topBar = { TaskListAppBar(model.filterOptions) },
            ) { padding ->
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                ) {
                    items(items = model.items, key = { it.id }) { item ->
                        TaskItemRow(item)
                    }
                }
            }
        }
    }
}

@Composable
private fun TaskListAppBar(filters: List<TaskFilterRenderModel>) {
    var menuExpanded by remember { mutableStateOf(false) }
    TopAppBar(
        title = { Text("Tasks") },
        actions = {
            IconButton(onClick = { menuExpanded = true }) {
                Text("Filter")
            }
            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false },
            ) {
                filters.forEach { filter ->
                    DropdownMenuItem(onClick = {
                        menuExpanded = false
                        filter.onSelected.invoke()
                    }) {
                        Text(filter.title)
                    }
                }
            }
        },
    )
}

@Composable
private fun TaskItemRow(item: TaskItemRenderModel) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { item.onClick.invoke() }
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(
            checked = item.isSelected,
            onCheckedChange = { item.onToggle.invoke() },
        )
        Text(
            text = item.text,
            modifier = Modifier.padding(start = 8.dp),
        )
    }
}
