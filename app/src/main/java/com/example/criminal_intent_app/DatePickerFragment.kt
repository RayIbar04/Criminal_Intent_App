package com.example.criminal_intent_app

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import java.util.Calendar

class DatePickerFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val initialDate = arguments?.getLong(ARG_DATE)
            ?: System.currentTimeMillis()

        val calendar = Calendar.getInstance().apply {
            timeInMillis = initialDate
        }

        return DatePickerDialog(
            requireContext(),
            { _, year, month, day ->
                parentFragmentManager.setFragmentResult(
                    REQUEST_KEY,
                    Bundle().apply {
                        putInt(KEY_YEAR, year)
                        putInt(KEY_MONTH, month)
                        putInt(KEY_DAY, day)
                    }
                )
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
    }

    companion object {
        const val REQUEST_KEY = "date_picker_result"
        const val KEY_YEAR = "year"
        const val KEY_MONTH = "month"
        const val KEY_DAY = "day"

        private const val ARG_DATE = "initial_date"

        fun newInstance(dateMillis: Long): DatePickerFragment {
            return DatePickerFragment().apply {
                arguments = Bundle().apply {
                    putLong(ARG_DATE, dateMillis)
                }
            }
        }
    }
}
