package org.hyperskill.pomodoro

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.DialogFragment

class SettingsDialogFragment : DialogFragment() {

    internal lateinit var listener: SettingsDialogListener

    interface SettingsDialogListener {
        fun onDialogPositiveClick(dialog: DialogFragment, workTime: Int?, restTime: Int?)
        fun onDialogNegativeClick(dialog: DialogFragment)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        try {
            listener = context as SettingsDialogListener
        } catch (e: ClassCastException) {
            throw ClassCastException((context.toString() +
                    " must implement SettingsDialogListener"))
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            val inflater = requireActivity().layoutInflater
            val view = inflater.inflate(R.layout.fragment_settings_dialog_view, null)

            builder
                .setView(view)
                .setPositiveButton(R.string.button_dialog_ok,
                        DialogInterface.OnClickListener { dialog, id ->
                            val workTime = view.findViewById<EditText>(R.id.workTime).text.toString().toIntOrNull()
                            val restTime = view.findViewById<EditText>(R.id.restTime).text.toString().toIntOrNull()

                            listener.onDialogPositiveClick(
                                dialog = this,
                                workTime = workTime,
                                restTime = restTime
                            )
                        })
                .setNegativeButton(R.string.button_dialog_cancel,
                        DialogInterface.OnClickListener { dialog, id ->
                            listener.onDialogNegativeClick(this)
                        }
                )

            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}