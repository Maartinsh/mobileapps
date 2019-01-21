package com.experiment.scope

import android.app.ProgressDialog
import android.content.Context

class LoadingProgressDialog(
    context: Context
) : ProgressDialog(context, R.style.SpecialAlertDialogStyle) {
    init {
        isIndeterminate = true
        setCancelable(false)
        setMessage(context.getString(R.string.message_loading_dialog))
    }
}