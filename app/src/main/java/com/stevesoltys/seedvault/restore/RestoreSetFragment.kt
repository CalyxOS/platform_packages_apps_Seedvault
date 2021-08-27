package com.stevesoltys.seedvault.restore

import android.app.Activity.RESULT_FIRST_USER
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.stevesoltys.seedvault.R
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class RestoreSetFragment : Fragment() {

    private val viewModel: RestoreViewModel by sharedViewModel()

    private lateinit var listView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var errorView: TextView
    private lateinit var skipView: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val v: View = inflater.inflate(R.layout.fragment_restore_set, container, false)

        listView = v.findViewById(R.id.listView)
        progressBar = v.findViewById(R.id.progressBar)
        errorView = v.findViewById(R.id.errorView)
        skipView = v.findViewById(R.id.skipView)

        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // decryption will fail when the device is locked, so keep the screen on to prevent locking
        requireActivity().window.addFlags(FLAG_KEEP_SCREEN_ON)

        viewModel.restoreSetResults.observe(viewLifecycleOwner, { result ->
            onRestoreResultsLoaded(result)
        })

        skipView.setOnClickListener {
            viewModel.onFinishClickedAfterRestoringAppData()
            // Equivalent to com.google.android.setupcompat.util.ResultCodes.RESULT_SKIP
            // SetupWizard handles this
            requireActivity().setResult(RESULT_FIRST_USER)
            requireActivity().finishAfterTransition()
        }
    }

    override fun onStart() {
        super.onStart()
        if (viewModel.recoveryCodeIsSet() && viewModel.validLocationIsSet()) {
            viewModel.loadRestoreSets()
        }
    }

    private fun onRestoreResultsLoaded(results: RestoreSetResult) {
        if (results.hasError()) {
            errorView.visibility = VISIBLE
            listView.visibility = INVISIBLE
            progressBar.visibility = INVISIBLE

            errorView.text = results.errorMsg
        } else {
            errorView.visibility = INVISIBLE
            listView.visibility = VISIBLE
            progressBar.visibility = INVISIBLE

            listView.adapter = RestoreSetAdapter(viewModel, results.restorableBackups)
        }
    }

}

internal interface RestorableBackupClickListener {
    fun onRestorableBackupClicked(restorableBackup: RestorableBackup)
}
