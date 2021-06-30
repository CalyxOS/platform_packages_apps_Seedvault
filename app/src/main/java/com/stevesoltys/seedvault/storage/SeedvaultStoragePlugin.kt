package com.stevesoltys.seedvault.storage

import android.content.Context
import android.os.UserHandle
import androidx.documentfile.provider.DocumentFile
import com.stevesoltys.seedvault.crypto.KeyManager
import com.stevesoltys.seedvault.plugins.saf.DocumentsStorage
import org.calyxos.backup.storage.plugin.saf.SafStoragePlugin
import javax.crypto.SecretKey

internal class SeedvaultStoragePlugin(
    context: Context,
    private val storage: DocumentsStorage,
    private val keyManager: KeyManager,
) : SafStoragePlugin(context) {
    override val mContext: Context
        get() = if (storage.storage?.isUsb == true)
            context.createContextAsUser(UserHandle.SYSTEM, 0) else context
    override val root: DocumentFile
        get() = storage.rootBackupDir ?: error("No storage set")

    override fun getMasterKey(): SecretKey = keyManager.getMainKey()
    override fun hasMasterKey(): Boolean = keyManager.hasMainKey()
}
