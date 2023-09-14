package com.stevesoltys.seedvault.e2e

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.stevesoltys.seedvault.restore.RestoreViewModel
import com.stevesoltys.seedvault.ui.notification.BackupNotificationManager
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.rules.TestName
import org.junit.runner.RunWith
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean

@RunWith(AndroidJUnit4::class)
internal abstract class SeedvaultLargeTest :
    LargeBackupTestBase, LargeRestoreTestBase, KoinComponent {

    @JvmField
    @Rule
    var name = TestName()

    companion object {
        private const val BASELINE_BACKUP_FOLDER = "seedvault_baseline"
        private const val RECOVERY_CODE_FILE = "recovery-code.txt"
    }

    override val spyBackupNotificationManager: BackupNotificationManager by inject()

    override val spyRestoreViewModel: RestoreViewModel by inject()

    private val baselineBackupFolderPath = "${this.externalStorageDir()}/$BASELINE_BACKUP_FOLDER"

    private val baselineRecoveryCodePath = "$baselineBackupFolderPath/$RECOVERY_CODE_FILE"

    private val keepRecordingScreen = AtomicBoolean(true)

    @Before
    open fun setUp() = runBlocking {
        clearDocumentPickerAppData()
        clearTestBackups()

        startScreenRecord(keepRecordingScreen, name.methodName)
        restoreBaselineBackup()
    }

    @After
    open fun tearDown() {
        stopScreenRecord(keepRecordingScreen)
    }

    /**
     * Restore the baseline backup, if it exists.
     *
     * This is a hand-crafted backup containing various apps and app data that we use for
     * provisioning tests: https://github.com/seedvault-app/seedvault-test-data
     */
    private fun restoreBaselineBackup() {
        if (File(baselineBackupFolderPath).exists()) {
            launchRestoreActivity()
            chooseStorageLocation(folderName = BASELINE_BACKUP_FOLDER, exists = true)
            typeInRestoreCode(baselineBackupRecoveryCode())
            performRestore()

            // remove baseline backup after restore
            runCommand("rm -Rf $baselineBackupFolderPath/*")
        }
    }

    private fun baselineBackupRecoveryCode(): List<String> {
        val recoveryCodeFile = File(baselineRecoveryCodePath)

        return recoveryCodeFile.readLines()
            .filter { it.isNotBlank() }
            .joinToString(separator = " ") { it.trim() }
            .split(" ")
    }
}
