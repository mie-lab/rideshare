package ch.ethz.ikg.rideshare.ui.basic

import android.os.Bundle
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import ch.ethz.ikg.rideshare.R

class HelpActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_help)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setTitle("Help")

        val helpText = buildSpannedString {
            append(
                "We appreciate your use of the SCCER Rideshare app a lot! " +
                        "This guide will explain the most important parts to you. "
            )
            append(
                "The SCCER Rideshare app aims to provide an ecologically (and economically) sustainable " +
                        "means of transport by matching you with other people driving similar routes. "
            )
            append(
                "By automatically tracking and predicting your (and other people's) mobility, it can detect " +
                        "potentially fruitful collaborations and suggest them to you in real time. "
            )
            append("\n\n")
            append(
                "For this to work you simply need to activate the tracking from within the app, and it " +
                        "will autonomously suggest you whenever you have the possibility to rideshare. "
            )
            append(
                "Please consider that this application works best if a certain minimal number of people " +
                        "use it. "
            )
            append(
                "As such, you might have to use it for a while (and/or convince your coworkers to participate " +
                        "in the project as well) before it can suggest you potential rideshare opportunities. "
            )
        }

        val textViewHelp: TextView = findViewById(R.id.textViewHelp)
        textViewHelp.text = helpText

        val privacyText = buildSpannedString {
            append(
                "We take privacy seriously. That's why the data we collect about you is solely used " +
                        "within the SCCER Rideshare research project to provide you with real-time " +
                        "rideshare suggestions and analyze them within the research project."
            )
        }

        val textViewPrivacy: TextView = findViewById(R.id.textViewPrivacy)
        textViewPrivacy.text = privacyText
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            // Respond to the action bar's Up/Home button
            android.R.id.home -> {
                super.onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
