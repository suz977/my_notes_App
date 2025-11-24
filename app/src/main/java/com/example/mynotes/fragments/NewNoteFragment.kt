package com.example.mynotes.fragments

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognizerIntent
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import com.example.mynotes.BuildConfig
import com.example.mynotes.MainActivity
import com.example.mynotes.R
import com.example.mynotes.databinding.FragmentNewNoteBinding
import com.example.mynotes.model.Note
import com.example.mynotes.viewmodel.NoteViewModel
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.launch
import java.util.Locale

class NewNoteFragment : Fragment(R.layout.fragment_new_note) {

    private var _binding: FragmentNewNoteBinding? = null
    private val binding get() = _binding!!

    private lateinit var noteViewModel: NoteViewModel
    private lateinit var mView: View

    // 1. Gemini Model Initialization
    private val generativeModel by lazy {
        GenerativeModel(
            modelName = "gemini-2.5-pro",
            apiKey = BuildConfig.GEMINI_API
        )
    }

    // 2. Activity Result Launcher for Speech Recognition
    private val speechRecognizerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            val spokenText: String? =
                data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.get(0)

            if (!spokenText.isNullOrBlank()) {
                summarizeTextWithGemini(spokenText)
            }
        }
    }

    // 3. Activity Result Launcher for Audio Permission
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                // Permission is granted. Continue the action
                startSpeechToText()
            } else {
                // Explain to the user that the feature is unavailable
                Toast.makeText(
                    context,
                    "Permission denied. Voice recording feature is unavailable.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNewNoteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        noteViewModel = (activity as MainActivity).noteViewModel
        mView = view
        setHasOptionsMenu(true) // We manage the menu here

        // 4. Set the OnClickListener for the FAB
        binding.fabRecordAudio.setOnClickListener {
            checkAudioPermissionAndRecord()
        }
    }

    // 5. Check for permission and start recording
    private fun checkAudioPermissionAndRecord() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED -> {
                // Permission is already granted
                startSpeechToText()
            }
            shouldShowRequestPermissionRationale(Manifest.permission.RECORD_AUDIO) -> {
                // Explain why you need the permission
                Toast.makeText(context, "This feature requires audio permission to record your note.", Toast.LENGTH_LONG).show()
                requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
            else -> {
                // Directly ask for the permission
                requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
        }
    }

    // 6. Launch the speech recognizer intent
    private fun startSpeechToText() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak your note...")
        }
        try {
            speechRecognizerLauncher.launch(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Speech recognition not available", Toast.LENGTH_SHORT).show()
        }
    }

    // 7. Send text to Gemini and update UI
    private fun summarizeTextWithGemini(text: String) {
        lifecycleScope.launch {
            try {
                // Show a loading indicator if you have one
                binding.noteBody.setText("Summarizing...")

                val prompt = "Summarize the following text into clear bullet points:\n\n$text"
                val response = generativeModel.generateContent(prompt)

                // Update the EditText with the summarized text
                binding.noteBody.setText(response.text ?: "Could not summarize.")

            } catch (e: Exception) {
                // Handle API errors
                Toast.makeText(context, "Error summarizing: ${e.message}", Toast.LENGTH_LONG).show()
                binding.noteBody.setText(text) // Fallback to the original text
            }
        }
    }

    private fun saveNote(view: View) {
        val noteTitle = binding.titeleEt.text.toString().trim()
        val noteBody = binding.noteBody.text.toString().trim()

        if (noteTitle.isNotEmpty()) {
            val note = Note(0, noteTitle, noteBody)
            noteViewModel.addNote(note)
            Toast.makeText(mView.context, "Note Saved Successfully", Toast.LENGTH_LONG).show()
            view.findNavController().navigate(R.id.action_newNoteFragment_to_homeFragment)
        } else {
            Toast.makeText(mView.context, "Please Enter Note Title", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        inflater.inflate(R.menu.update_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.save) {
            saveNote(mView)
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
