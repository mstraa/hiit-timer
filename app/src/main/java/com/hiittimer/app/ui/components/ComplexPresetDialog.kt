package com.hiittimer.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.hiittimer.app.data.*
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComplexPresetDialog(
    isOpen: Boolean,
    onDismiss: () -> Unit,
    onSave: (ComplexPreset) -> Unit,
    presetToEdit: ComplexPreset? = null
) {
    if (!isOpen) return

    // State for the preset - use key to reset when presetToEdit changes
    var presetName by remember(presetToEdit) { mutableStateOf(presetToEdit?.name ?: "") }
    var presetDescription by remember(presetToEdit) { mutableStateOf(presetToEdit?.description ?: "") }
    var roundGroups by remember(presetToEdit) { 
        mutableStateOf(presetToEdit?.roundGroups ?: listOf(
            RoundGroup(
                name = "Round Group 1",
                rounds = 3,
                workPhases = listOf(
                    WorkPhase(name = "Exercise 1", durationSeconds = 30)
                ),
                restBetweenPhases = 10,
                restBetweenRounds = 20
            )
        ))
    }
    
    var showRoundGroupDialog by remember { mutableStateOf(false) }
    var roundGroupToEdit by remember { mutableStateOf<Pair<Int, RoundGroup>?>(null) }
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.9f),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header
                Text(
                    text = if (presetToEdit != null) "Edit Complex Preset" else "Create Complex Preset",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Preset Name
                OutlinedTextField(
                    value = presetName,
                    onValueChange = { presetName = it },
                    label = { Text("Preset Name*") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Preset Description
                OutlinedTextField(
                    value = presetDescription,
                    onValueChange = { 
                        if (it.length <= 200) presetDescription = it 
                    },
                    label = { Text("Description (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    supportingText = { Text("${presetDescription.length}/200") },
                    minLines = 2,
                    maxLines = 3
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Round Groups Section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Round Groups",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    IconButton(
                        onClick = {
                            roundGroupToEdit = null
                            showRoundGroupDialog = true
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Round Group"
                        )
                    }
                }
                
                // Round Groups List
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(roundGroups) { index, group ->
                        RoundGroupCard(
                            roundGroup = group,
                            groupNumber = index + 1,
                            onEdit = {
                                roundGroupToEdit = Pair(index, group)
                                showRoundGroupDialog = true
                            },
                            onDelete = {
                                if (roundGroups.size > 1) {
                                    roundGroups = roundGroups.toMutableList().also { it.removeAt(index) }
                                }
                            },
                            onMoveUp = if (index > 0) {
                                {
                                    val mutableList = roundGroups.toMutableList()
                                    val temp = mutableList[index]
                                    mutableList[index] = mutableList[index - 1]
                                    mutableList[index - 1] = temp
                                    roundGroups = mutableList
                                }
                            } else null,
                            onMoveDown = if (index < roundGroups.size - 1) {
                                {
                                    val mutableList = roundGroups.toMutableList()
                                    val temp = mutableList[index]
                                    mutableList[index] = mutableList[index + 1]
                                    mutableList[index + 1] = temp
                                    roundGroups = mutableList
                                }
                            } else null
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }
                    
                    Button(
                        onClick = {
                            if (presetName.isNotBlank() && roundGroups.isNotEmpty()) {
                                val preset = ComplexPreset(
                                    id = presetToEdit?.id ?: UUID.randomUUID().toString(),
                                    name = presetName.trim(),
                                    description = presetDescription.trim().takeIf { it.isNotEmpty() },
                                    roundGroups = roundGroups,
                                    createdAt = presetToEdit?.createdAt ?: System.currentTimeMillis()
                                )
                                onSave(preset)
                                onDismiss()
                            }
                        },
                        enabled = presetName.isNotBlank() && roundGroups.isNotEmpty(),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Save")
                    }
                }
            }
        }
        
        // Round Group Dialog
        if (showRoundGroupDialog) {
            RoundGroupDialog(
                isOpen = true,
                onDismiss = { showRoundGroupDialog = false },
                onSave = { newGroup ->
                    roundGroups = if (roundGroupToEdit != null) {
                        roundGroups.mapIndexed { index, group ->
                            if (index == roundGroupToEdit!!.first) newGroup else group
                        }
                    } else {
                        roundGroups + newGroup
                    }
                    showRoundGroupDialog = false
                    roundGroupToEdit = null
                },
                groupToEdit = roundGroupToEdit?.second
            )
        }
    }
}

@Composable
private fun RoundGroupCard(
    roundGroup: RoundGroup,
    groupNumber: Int,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onMoveUp: (() -> Unit)? = null,
    onMoveDown: (() -> Unit)? = null
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Group $groupNumber: ${roundGroup.name}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    
                    if (!roundGroup.description.isNullOrEmpty()) {
                        Text(
                            text = roundGroup.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = "${roundGroup.rounds} rounds × ${roundGroup.workPhases.size} exercises",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    
                    // Show work phases
                    roundGroup.workPhases.forEachIndexed { index, phase ->
                        Text(
                            text = "  ${index + 1}. ${phase.name} (${phase.durationSeconds}s)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = "Rest: ${roundGroup.restBetweenPhases}s between exercises, ${roundGroup.restBetweenRounds}s between rounds",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    
                    if (roundGroup.specialRestAfterGroup != null) {
                        Text(
                            text = "Special rest after group: ${roundGroup.specialRestAfterGroup}s",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                
                Row {
                    // Move up button
                    if (onMoveUp != null) {
                        IconButton(
                            onClick = onMoveUp,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowUp,
                                contentDescription = "Move Up",
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    
                    // Move down button
                    if (onMoveDown != null) {
                        IconButton(
                            onClick = onMoveDown,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = "Move Down",
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RoundGroupDialog(
    isOpen: Boolean,
    onDismiss: () -> Unit,
    onSave: (RoundGroup) -> Unit,
    groupToEdit: RoundGroup? = null
) {
    if (!isOpen) return
    
    var groupName by remember { mutableStateOf(groupToEdit?.name ?: "Round Group") }
    var groupDescription by remember { mutableStateOf(groupToEdit?.description ?: "") }
    var rounds by remember { mutableIntStateOf(groupToEdit?.rounds ?: 3) }
    var restBetweenPhases by remember { mutableIntStateOf(groupToEdit?.restBetweenPhases ?: 10) }
    var restBetweenRounds by remember { mutableIntStateOf(groupToEdit?.restBetweenRounds ?: 30) }
    var specialRestAfterGroup by remember { mutableIntStateOf(groupToEdit?.specialRestAfterGroup ?: 0) }
    var hasSpecialRest by remember { mutableStateOf(groupToEdit?.specialRestAfterGroup != null) }
    
    var workPhases by remember { 
        mutableStateOf(groupToEdit?.workPhases ?: listOf(
            WorkPhase(name = "Exercise 1", durationSeconds = 30)
        ))
    }
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .fillMaxHeight(0.85f),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Text(
                    text = if (groupToEdit != null) "Edit Round Group" else "Create Round Group",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Group Name
                    OutlinedTextField(
                        value = groupName,
                        onValueChange = { groupName = it },
                        label = { Text("Group Name*") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Group Description
                    OutlinedTextField(
                        value = groupDescription,
                        onValueChange = { groupDescription = it },
                        label = { Text("Description (optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        maxLines = 3
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Rounds
                    OutlinedTextField(
                        value = rounds.toString(),
                        onValueChange = { 
                            if (it.isEmpty()) {
                                rounds = 0
                            } else {
                                it.toIntOrNull()?.let { value ->
                                    rounds = value
                                }
                            }
                        },
                        label = { Text("Number of Rounds") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        isError = rounds !in 1..99
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Work Phases
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Exercises",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium
                        )
                        
                        TextButton(
                            onClick = {
                                workPhases = workPhases + WorkPhase(
                                    name = "Exercise ${workPhases.size + 1}",
                                    durationSeconds = 30
                                )
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add Exercise")
                        }
                    }
                    
                    // Work Phases List
                    workPhases.forEachIndexed { index, phase ->
                        WorkPhaseRow(
                            phase = phase,
                            index = index,
                            onUpdate = { updatedPhase ->
                                workPhases = workPhases.mapIndexed { i, p ->
                                    if (i == index) updatedPhase else p
                                }
                            },
                            onDelete = {
                                if (workPhases.size > 1) {
                                    workPhases = workPhases.filterIndexed { i, _ -> i != index }
                                }
                            },
                            onMoveUp = if (index > 0) {
                                {
                                    val mutableList = workPhases.toMutableList()
                                    val temp = mutableList[index]
                                    mutableList[index] = mutableList[index - 1]
                                    mutableList[index - 1] = temp
                                    workPhases = mutableList
                                }
                            } else null,
                            onMoveDown = if (index < workPhases.size - 1) {
                                {
                                    val mutableList = workPhases.toMutableList()
                                    val temp = mutableList[index]
                                    mutableList[index] = mutableList[index + 1]
                                    mutableList[index + 1] = temp
                                    workPhases = mutableList
                                }
                            } else null
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Rest Settings
                    Text(
                        text = "Rest Settings",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = restBetweenPhases.toString(),
                            onValueChange = { 
                                if (it.isEmpty()) {
                                    restBetweenPhases = 0
                                } else {
                                    it.toIntOrNull()?.let { value ->
                                        restBetweenPhases = value
                                    }
                                }
                            },
                            label = { Text("Between Exercises (s)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            isError = restBetweenPhases !in 0..300
                        )
                        
                        OutlinedTextField(
                            value = restBetweenRounds.toString(),
                            onValueChange = { 
                                if (it.isEmpty()) {
                                    restBetweenRounds = 0
                                } else {
                                    it.toIntOrNull()?.let { value ->
                                        restBetweenRounds = value
                                    }
                                }
                            },
                            label = { Text("Between Rounds (s)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            isError = restBetweenRounds !in 0..300
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Special Rest
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = hasSpecialRest,
                            onCheckedChange = { hasSpecialRest = it }
                        )
                        Text("Special rest after this group")
                    }
                    
                    if (hasSpecialRest) {
                        OutlinedTextField(
                            value = specialRestAfterGroup.toString(),
                            onValueChange = { 
                                if (it.isEmpty()) {
                                    specialRestAfterGroup = 0
                                } else {
                                    it.toIntOrNull()?.let { value ->
                                        specialRestAfterGroup = value
                                    }
                                }
                            },
                            label = { Text("Special Rest Duration (s)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            isError = specialRestAfterGroup !in 5..600
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }
                    
                    Button(
                        onClick = {
                            if (groupName.isNotBlank() && workPhases.isNotEmpty()) {
                                onSave(
                                    RoundGroup(
                                        id = groupToEdit?.id ?: UUID.randomUUID().toString(),
                                        name = groupName.trim(),
                                        description = groupDescription.trim().takeIf { it.isNotEmpty() },
                                        rounds = rounds,
                                        workPhases = workPhases,
                                        restBetweenPhases = restBetweenPhases,
                                        restBetweenRounds = restBetweenRounds,
                                        specialRestAfterGroup = if (hasSpecialRest) specialRestAfterGroup else null
                                    )
                                )
                            }
                        },
                        enabled = groupName.isNotBlank() && 
                            workPhases.isNotEmpty() && 
                            rounds in 1..99 &&
                            restBetweenPhases in 0..300 &&
                            restBetweenRounds in 0..300 &&
                            (!hasSpecialRest || specialRestAfterGroup in 5..600) &&
                            workPhases.all { it.durationSeconds in 5..900 },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Save")
                    }
                }
            }
        }
    }
}

@Composable
private fun WorkPhaseRow(
    phase: WorkPhase,
    index: Int,
    onUpdate: (WorkPhase) -> Unit,
    onDelete: () -> Unit,
    onMoveUp: (() -> Unit)? = null,
    onMoveDown: (() -> Unit)? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "${index + 1}.",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            
            OutlinedTextField(
                value = phase.name,
                onValueChange = { onUpdate(phase.copy(name = it)) },
                label = { Text("Exercise Name") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            
            OutlinedTextField(
                value = phase.durationSeconds.toString(),
                onValueChange = { 
                    if (it.isEmpty()) {
                        onUpdate(phase.copy(durationSeconds = 0))
                    } else {
                        it.toIntOrNull()?.let { value ->
                            onUpdate(phase.copy(durationSeconds = value))
                        }
                    }
                },
                label = { Text("Duration (s)") },
                modifier = Modifier.width(100.dp),
                singleLine = true,
                isError = phase.durationSeconds !in 5..900
            )
            
            // Move up button
            if (onMoveUp != null) {
                IconButton(
                    onClick = onMoveUp,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowUp,
                        contentDescription = "Move Up",
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            
            // Move down button
            if (onMoveDown != null) {
                IconButton(
                    onClick = onMoveDown,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Move Down",
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}