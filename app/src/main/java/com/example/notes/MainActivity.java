package com.example.notes;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.SearchView;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Switch;
import android.widget.Toast;

import com.example.notes.Adapter.NoteAdapter;
import com.example.notes.DataBase.RoomDB;
import com.example.notes.Models.Notes;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements PopupMenu.OnMenuItemClickListener {

    RecyclerView recyclerView;
    NoteAdapter noteAdapter;
    List<Notes> notes = new ArrayList<>();
    RoomDB database;
    FloatingActionButton add;
    SearchView searchView;
    Notes selectedNotes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recyclerView);
        add = findViewById(R.id.add_note);
        searchView = findViewById(R.id.searchView);

        database = RoomDB.getInstance(this);

        notes = database.mainDAO().getAll();

        updateRecycler(notes);

        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, AddnotesActivity.class);
                startActivityForResult(intent, 1);
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filter(newText);
                return true;
            }
        });

    }

    private void filter(String newText) {
        List<Notes> filteredList = new ArrayList<>();
        for (Notes singleNote : notes){
            if (singleNote.getTitle().toLowerCase().contains(newText.toLowerCase())
            || singleNote.getNotes().toLowerCase().contains(newText.toLowerCase())){
                filteredList.add(singleNote);
            }
        }
        noteAdapter.filterList(filteredList);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1) {
            if (resultCode == Activity.RESULT_OK) {
                Notes newNote = (Notes) data.getSerializableExtra("note");
                database.mainDAO().insert(newNote);
                notes.clear();
                notes.addAll(database.mainDAO().getAll());
                noteAdapter.notifyDataSetChanged();
            }
        } else if (requestCode == 2) {
            if (resultCode == Activity.RESULT_OK) {
                Notes newNote = (Notes) data.getSerializableExtra("note");
                database.mainDAO().update(newNote.getID(), newNote.getTitle(), newNote.getNotes());
                notes.clear();
                notes.addAll(database.mainDAO().getAll());
                noteAdapter.notifyDataSetChanged();
            }
        }
    }

    private void updateRecycler(List<Notes> notes) {
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, LinearLayoutManager.VERTICAL));
        noteAdapter = new NoteAdapter(this, notes, notesClickListner);
        recyclerView.setAdapter(noteAdapter);
    }

    private final NotesClickListner notesClickListner = new NotesClickListner() {
        @Override
        public void onClick(Notes notes) {
            Intent intent = new Intent(MainActivity.this, AddnotesActivity.class);
            intent.putExtra("old_notes", notes);
            startActivityForResult(intent, 2);
        }

        @Override
        public void onLongClick(Notes notes, CardView cardview) {
            selectedNotes = new Notes();
            selectedNotes = notes;

            showpopUp(cardview);
        }
    };

    private void showpopUp(CardView cardview) {
        PopupMenu popupMenu = new PopupMenu(this,cardview);
        popupMenu.setOnMenuItemClickListener(this);
        popupMenu.inflate(R.menu.popup);
        popupMenu.show();
    }


    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()){
            case R.id.pinMenu:
                if (selectedNotes.isPinned()){
                        database.mainDAO().pin(selectedNotes.getID(),false);
                        Toast.makeText(this, "Unpinned", Toast.LENGTH_SHORT).show();
                }else {
                    database.mainDAO().pin(selectedNotes.getID(),true);
                    Toast.makeText(this, "Pinned", Toast.LENGTH_SHORT).show();
                }

                notes.clear();
                notes.addAll(database.mainDAO().getAll());
                noteAdapter.notifyDataSetChanged();
                return true;

            case R.id.delete:
                database.mainDAO().delete(selectedNotes);
                notes.remove(selectedNotes);
                noteAdapter.notifyDataSetChanged();
                Toast.makeText(this, "Deleted", Toast.LENGTH_SHORT).show();
                return true;
            default:
                return false;
        }
    }
}