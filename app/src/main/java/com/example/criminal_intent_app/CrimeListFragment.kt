
package com.example.criminal_intent_app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch
import java.text.DateFormat
import java.util.Date

class CrimeListFragment : Fragment() {

    private lateinit var adapter: CrimeAdapter
    private lateinit var repository: CrimeRepository

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        repository = CrimeRepository(
            CrimeDatabase.getDatabase(requireContext()).crimeDao()
        )

        val layout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
        }

        val addButton = Button(requireContext()).apply {
            text = "Add Crime"

            setOnClickListener {
                parentFragmentManager.beginTransaction()
                    .replace(
                        (view?.parent as? ViewGroup)?.id
                            ?: android.R.id.content,
                        CrimeDetailFragment.newInstance()
                    )
                    .addToBackStack(null)
                    .commit()
            }
        }

        val recyclerView = RecyclerView(requireContext()).apply {
            layoutManager = LinearLayoutManager(requireContext())
        }

        adapter = CrimeAdapter()
        recyclerView.adapter = adapter

        layout.addView(addButton)

        layout.addView(
            recyclerView,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        )

        return layout
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                repository.crimes.collect { crimes ->
                    adapter.updateCrimes(crimes)
                }
            }
        }
    }

    private fun openCrime(crimeId: Long) {
        parentFragmentManager.beginTransaction()
            .replace(
                (view?.parent as? ViewGroup)?.id
                    ?: android.R.id.content,
                CrimeDetailFragment.newInstance(crimeId)
            )
            .addToBackStack(null)
            .commit()
    }

    private inner class CrimeAdapter :
        RecyclerView.Adapter<CrimeAdapter.CrimeViewHolder>() {

        private val crimes = mutableListOf<Crime>()

        fun updateCrimes(newCrimes: List<Crime>) {
            crimes.clear()
            crimes.addAll(newCrimes)
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): CrimeViewHolder {

            val layout = LinearLayout(parent.context).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(32, 24, 32, 24)
                isClickable = true
                isFocusable = true
            }

            val titleView = TextView(parent.context).apply {
                textSize = 18f
            }

            val offenderView = TextView(parent.context).apply {
                textSize = 16f
            }

            val dateView = TextView(parent.context).apply {
                textSize = 14f
            }

            val solvedView = TextView(parent.context).apply {
                textSize = 14f
            }

            layout.addView(titleView)
            layout.addView(offenderView)
            layout.addView(dateView)
            layout.addView(solvedView)

            return CrimeViewHolder(
                layout,
                titleView,
                offenderView,
                dateView,
                solvedView
            )
        }

        override fun onBindViewHolder(
            holder: CrimeViewHolder,
            position: Int
        ) {
            val crime = crimes[position]

            holder.titleView.text =
                crime.title.ifBlank { "New Crime" }

            holder.offenderView.text =
                "Offender: " + crime.suspect.ifBlank { "Unknown" }

            holder.dateView.text = DateFormat.getDateInstance().format(
                Date(crime.dateMillis)
            )

            holder.solvedView.text =
                if (crime.isSolved) "Solved" else "Unsolved"

            holder.itemView.setOnClickListener {
                openCrime(crime.id)
            }
        }

        override fun getItemCount(): Int = crimes.size

        inner class CrimeViewHolder(
            view: View,
            val titleView: TextView,
            val offenderView: TextView,
            val dateView: TextView,
            val solvedView: TextView
        ) : RecyclerView.ViewHolder(view)
    }
}
