package com.example.mynotes.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import androidx.room.util.convertByteToUUID
import com.example.mynotes.MainActivity
import com.example.mynotes.R
import com.example.mynotes.adapter.NoteAdapter
import com.example.mynotes.databinding.FragmentHomeBinding
import com.example.mynotes.databinding.FragmentUpdateBinding
import com.example.mynotes.model.Note
import com.example.mynotes.viewmodel.NoteViewModel
import kotlin.random.Random

class UpdateFragment : Fragment(R.layout.fragment_update) {

    private var _binding: FragmentUpdateBinding? = null
    private val binding get() = _binding!!

    private lateinit var noteViewModel: NoteViewModel
    private lateinit var currentNote: Note
    private val args: UpdateFragmentArgs by navArgs()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentUpdateBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        noteViewModel = (activity as MainActivity).noteViewModel
        currentNote = args.note!!

        binding.titeleEt.setText(currentNote.notetitle)
        binding.noteBody.setText(currentNote.notebody)

        binding.done.setOnClickListener {
            val title = binding.titeleEt.text.toString().trim()
            val body = binding.noteBody.text.toString().trim()

            if(title.isNotEmpty()){
                val note = Note(currentNote.id,title,body)
                noteViewModel.updateNote(note)
                view.findNavController().navigate(R.id.action_updateFragment_to_homeFragment)
            }else{
                Toast.makeText(context, "Please enter Title", Toast.LENGTH_LONG).show()
            }
        }
    }
    private fun deleteNote(){
        AlertDialog.Builder(requireContext()).apply {
            setTitle("Delete Note")
            setMessage("Are you sure you want to delete note ? ")
            setPositiveButton("Delete"){_,_ ->
                noteViewModel.deleteNote(currentNote)

                view?.findNavController()?.navigate(R.id.action_updateFragment_to_homeFragment)
            }
            setNegativeButton("Cancel", null)
        }.create().show()
    }
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        inflater.inflate(R.menu.delete_menu,menu)
        super.onCreateOptionsMenu(menu, inflater)
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.del -> {
                deleteNote()
            }
        }
        return super.onOptionsItemSelected(item)
    }
    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}