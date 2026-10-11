package com.example.criminal_intent_app

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.FragmentManager

class MainActivity : AppCompatActivity() {

    private val fragmentContainerId = 1001
    private lateinit var toolbar: Toolbar

    private val fragmentListener =
        FragmentManager.OnBackStackChangedListener {
            updateToolbar()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val rootLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        toolbar = Toolbar(this).apply {
            title = "Criminal Intent"
            setTitleTextColor(android.graphics.Color.BLACK)
            setBackgroundColor(android.graphics.Color.LTGRAY)
        }

        rootLayout.addView(
            toolbar,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                resources.getDimensionPixelSize(
                    androidx.appcompat.R.dimen.abc_action_bar_default_height_material
                )
            )
        )

        val container = FrameLayout(this).apply {
            id = fragmentContainerId
        }

        rootLayout.addView(
            container,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        )

        setContentView(rootLayout)
        setSupportActionBar(toolbar)

        toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        supportFragmentManager.addOnBackStackChangedListener(
            fragmentListener
        )

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .add(
                    fragmentContainerId,
                    CrimeListFragment(),
                    "crime_list"
                )
                .commit()
        }

        updateToolbar()
    }

    private fun updateToolbar() {
        val showingDetails =
            supportFragmentManager.backStackEntryCount > 0

        supportActionBar?.setDisplayHomeAsUpEnabled(showingDetails)
        supportActionBar?.setHomeButtonEnabled(showingDetails)

        supportActionBar?.title = "Criminal Intent"

        // Show the back arrow only on the detail screen.
        supportActionBar?.setDisplayShowHomeEnabled(showingDetails)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressedDispatcher.onBackPressed()
                true
            }

            R.id.menu_share -> {
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(
                        Intent.EXTRA_TEXT,
                        "I'm using Criminal Intent to manage and track crime records!"
                    )
                }

                startActivity(
                    Intent.createChooser(
                        shareIntent,
                        "Share Criminal Intent"
                    )
                )
                true
            }

            R.id.menu_about -> {
                Toast.makeText(
                    this,
                    "Criminal Intent App - Manage and track crime records.",
                    Toast.LENGTH_LONG
                ).show()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroy() {
        supportFragmentManager.removeOnBackStackChangedListener(
            fragmentListener
        )
        super.onDestroy()
    }
}