@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    var showBackupDialog by remember { mutableStateOf(false) }
    var backupStatus by remember { mutableStateOf<String?>(null) }
    
    LaunchedEffect(Unit) {
        viewModel.loadSettings()
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // ... existing settings ...
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Backup Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Database Backup",
                    style = MaterialTheme.typography.titleMedium
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Button(
                    onClick = { showBackupDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Create Backup")
                }
                
                backupStatus?.let { status ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = status,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
    
    if (showBackupDialog) {
        AlertDialog(
            onDismissRequest = { showBackupDialog = false },
            title = { Text("Create Backup") },
            text = { Text("This will create a backup of all your data. Continue?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showBackupDialog = false
                        viewModel.createBackup { status ->
                            backupStatus = status
                        }
                    }
                ) {
                    Text("Backup")
                }
            },
            dismissButton = {
                TextButton(onClick = { showBackupDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
} 