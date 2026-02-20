package com.example.simplenotesapp.activitys;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.example.simplenotesapp.R;
import com.example.simplenotesapp.adapters.NotesAdapter;
import com.example.simplenotesapp.dataBases.notes.NotesDatabase;
import com.example.simplenotesapp.model.Mapper;
import com.example.simplenotesapp.repository.NotesRepository;
import com.example.simplenotesapp.viewModel.NotesViewModel;
import com.example.simplenotesapp.viewModel.NotesViewModelFactory;

public class MainNotesActivity extends AppCompatActivity {

    private NotesAdapter adapter;
    private RecyclerView rv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        rv = findViewById(R.id.recyclerView);

        adapter = new NotesAdapter();

        rv.setAdapter(adapter);
        // 2 столбца, вертикальная ориентация
        rv.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));

        NotesRepository repository =
                new NotesRepository(NotesDatabase.getInstance(getApplicationContext()));

        NotesViewModelFactory factory =
                new NotesViewModelFactory(repository);

        NotesViewModel viewModel = new ViewModelProvider(this, factory)
                .get(NotesViewModel.class);

        // Вот здесь магия LiveData
        viewModel.getAllNotes().observe(this, notes -> {
            adapter.updateList(Mapper.listToModel(notes));
        });
    }
}
