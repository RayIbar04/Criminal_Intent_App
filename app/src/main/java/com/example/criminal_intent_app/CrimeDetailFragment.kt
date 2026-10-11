package com.example.criminal_intent_app

import android.app.AlertDialog
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.File
import java.text.DateFormat
import java.util.Calendar

class CrimeDetailFragment : Fragment() {

    private lateinit var repository: CrimeRepository

    private var currentCrime: Crime? = null
    private var crimeId: Long = -1L
    private var photoUri: Uri? = null

    private lateinit var titleField: EditText
    private lateinit var suspectField: EditText
    private lateinit var dateButton: Button
    private lateinit var solvedCheckBox: CheckBox
    private lateinit var photoImageView: ImageView

    private val takePicture =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) {
                photoUri?.let { uri ->
                    photoImageView.setImageURI(null)
                    photoImageView.setImageURI(uri)
                    photoImageView.visibility = View.VISIBLE

                    Toast.makeText(
                        requireContext(),
                        "Photo taken!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                photoImageView.visibility =
                    if (currentCrime?.photoUri != null) View.VISIBLE
                    else View.GONE
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        crimeId = arguments?.getLong(ARG_CRIME_ID, -1L) ?: -1L

        val database = CrimeDatabase.getDatabase(requireContext())
        repository = CrimeRepository(database.crimeDao())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val layout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 24, 32, 24)
        }

        titleField = EditText(requireContext()).apply {
            hint = "Crime title"
            setSingleLine(true)
        }

        suspectField = EditText(requireContext()).apply {
            hint = "Suspect name"
            setSingleLine(true)
        }

        dateButton = Button(requireContext()).apply {
            text = "Choose Date"
            setOnClickListener {
                showDatePicker()
            }
        }

        solvedCheckBox = CheckBox(requireContext()).apply {
            text = "Crime solved"
        }

        photoImageView = ImageView(requireContext()).apply {
            contentDescription = "Crime photo"
            adjustViewBounds = true
            scaleType = ImageView.ScaleType.CENTER_CROP
            visibility = View.GONE

            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                500
            )
        }

        val takePhotoButton = Button(requireContext()).apply {
            text = "Take Photo"
            setOnClickListener {
                openCamera()
            }
        }

        val saveButton = Button(requireContext()).apply {
            text = "Save Crime"
            setOnClickListener {
                saveCrime()
            }
        }

        val deleteButton = Button(requireContext()).apply {
            text = "Delete Crime"
            setOnClickListener {
                confirmDelete()
            }
        }

        layout.addView(titleField)
        layout.addView(suspectField)
        layout.addView(dateButton)
        layout.addView(solvedCheckBox)
        layout.addView(takePhotoButton)
        layout.addView(photoImageView)
        layout.addView(saveButton)
        layout.addView(deleteButton)

        val scrollView = ScrollView(requireContext())
        scrollView.addView(layout)

        return scrollView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        parentFragmentManager.setFragmentResultListener(
            DatePickerFragment.REQUEST_KEY,
            viewLifecycleOwner
        ) { _, result ->

            val year = result.getInt(DatePickerFragment.KEY_YEAR)
            val month = result.getInt(DatePickerFragment.KEY_MONTH)
            val day = result.getInt(DatePickerFragment.KEY_DAY)

            val calendar = Calendar.getInstance().apply {
                timeInMillis = currentCrime?.dateMillis
                    ?: System.currentTimeMillis()

                set(Calendar.YEAR, year)
                set(Calendar.MONTH, month)
                set(Calendar.DAY_OF_MONTH, day)
            }

            val updatedDate = calendar.timeInMillis

            currentCrime = (currentCrime ?: Crime()).copy(
                dateMillis = updatedDate
            )

            updateDateButton(updatedDate)
        }

        loadCrime()
    }

    private fun loadCrime() {
        if (crimeId == -1L) {
            currentCrime = Crime()
            updateDateButton(currentCrime!!.dateMillis)
            return
        }

        viewLifecycleOwner.lifecycleScope.launch {
            val crime = repository.getCrime(crimeId).first()

            if (crime == null) {
                Toast.makeText(
                    requireContext(),
                    "Crime not found",
                    Toast.LENGTH_SHORT
                ).show()

                parentFragmentManager.popBackStack()
                return@launch
            }

            currentCrime = crime

            titleField.setText(crime.title)
            suspectField.setText(crime.suspect)
            solvedCheckBox.isChecked = crime.isSolved
            updateDateButton(crime.dateMillis)

            if (!crime.photoUri.isNullOrBlank()) {
                photoUri = Uri.parse(crime.photoUri)

                try {
                    photoImageView.setImageURI(photoUri)
                    photoImageView.visibility = View.VISIBLE
                } catch (_: Exception) {
                    photoImageView.visibility = View.GONE
                }
            }
        }
    }

    private fun showDatePicker() {
        val date = currentCrime?.dateMillis
            ?: System.currentTimeMillis()

        DatePickerFragment.newInstance(date)
            .show(parentFragmentManager, "date_picker")
    }

    private fun openCamera() {
        try {
            val photoDirectory = File(
                requireContext().filesDir,
                "photos"
            )

            if (!photoDirectory.exists()) {
                photoDirectory.mkdirs()
            }

            val photoFile = File(
                photoDirectory,
                "crime_${System.currentTimeMillis()}.jpg"
            )

            val uri = FileProvider.getUriForFile(
                requireContext(),
                "${requireContext().packageName}.fileprovider",
                photoFile
            )

            photoUri = uri
            takePicture.launch(uri)

        } catch (e: Exception) {
            Toast.makeText(
                requireContext(),
                "Could not open camera: ${e.message}",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun saveCrime() {
        val oldCrime = currentCrime ?: Crime()

        val updatedCrime = oldCrime.copy(
            title = titleField.text.toString().trim(),
            suspect = suspectField.text.toString().trim(),
            isSolved = solvedCheckBox.isChecked,
            photoUri = photoUri?.toString() ?: oldCrime.photoUri
        )

        viewLifecycleOwner.lifecycleScope.launch {
            if (updatedCrime.id == 0L) {
                repository.addCrime(updatedCrime)
            } else {
                repository.updateCrime(updatedCrime)
            }

            Toast.makeText(
                requireContext(),
                "Crime saved!",
                Toast.LENGTH_SHORT
            ).show()

            parentFragmentManager.popBackStack()
        }
    }

    private fun confirmDelete() {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Crime")
            .setMessage("Are you sure you want to delete this crime?")
            .setPositiveButton("Delete") { _, _ ->
                deleteCrime()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteCrime() {
        val crime = currentCrime

        if (crime == null || crime.id == 0L) {
            parentFragmentManager.popBackStack()
            return
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repository.deleteCrime(crime)

            Toast.makeText(
                requireContext(),
                "Crime deleted",
                Toast.LENGTH_SHORT
            ).show()

            parentFragmentManager.popBackStack()
        }
    }

    private fun updateDateButton(dateMillis: Long) {
        dateButton.text =
            DateFormat.getDateInstance().format(dateMillis)
    }

    companion object {
        private const val ARG_CRIME_ID = "crime_id"

        fun newInstance(
            crimeId: Long = -1L
        ): CrimeDetailFragment {
            return CrimeDetailFragment().apply {
                arguments = Bundle().apply {
                    putLong(ARG_CRIME_ID, crimeId)
                }
            }
        }
    }
}