class SettingsViewModel @Inject constructor(
    private val context: Context,
    private val databaseBackup: DatabaseBackup
) : ViewModel() {
    
    fun createBackup(onStatusUpdate: (String) -> Unit) {
        viewModelScope.launch {
            try {
                onStatusUpdate("Creating backup...")
                val backupFiles = databaseBackup.createBackup()
                onStatusUpdate("Backup created successfully!\nFiles: ${backupFiles.joinToString("\n")}")
            } catch (e: Exception) {
                onStatusUpdate("Backup failed: ${e.message}")
            }
        }
    }
} 