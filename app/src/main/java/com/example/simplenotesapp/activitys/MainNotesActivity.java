package com.example.simplenotesapp.activitys;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.example.simplenotesapp.MyApplication;
import com.example.simplenotesapp.R;
import com.example.simplenotesapp.activity_manager.AuthManager;
import com.example.simplenotesapp.adapters.previewNotesAdapter;
import com.example.simplenotesapp.dataBase.entity.NoteEntity;
import com.example.simplenotesapp.fragments_folder.BottomSheetMenu;
import com.example.simplenotesapp.repository.NotesRepository;
import com.example.simplenotesapp.viewModel.NotesViewModel;
import com.example.simplenotesapp.viewModel.NotesViewModelFactory;

public class MainNotesActivity extends AppCompatActivity {

    private previewNotesAdapter adapter;
    private RecyclerView rv;
    private ImageView addNoteBtn;
    private ImageView openBottomSheetMenu;
    private AuthManager authManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 1. Инициализируем ВСЕ View компоненты
        addNoteBtn = findViewById(R.id.addNoteBtn);
        rv = findViewById(R.id.recyclerView);
        openBottomSheetMenu = findViewById(R.id.openBottomSheetMenu);

        adapter = new previewNotesAdapter();

        rv.setAdapter(adapter);
        // 2 столбца, вертикальная ориентация
        rv.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));


        // В MainNotesActivity.java
        authManager = new AuthManager(getApplicationContext());
        Long currentId = authManager.getId(); // Предположим, вы храните ID при логине

        NotesRepository notesRepository = ((MyApplication) getApplication()).getNotesRepository();
        NotesViewModelFactory factory = new NotesViewModelFactory(notesRepository, currentId);

        NotesViewModel notesViewModel= new ViewModelProvider(this, factory).get(NotesViewModel.class);

        // Button sheet menu image view button and the activation of bottom sheet
        openBottomSheetMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BottomSheetMenu bottomSheetDialogFragment = new BottomSheetMenu();
                bottomSheetDialogFragment.show(getSupportFragmentManager(), "BottomSheetMenu");

            }
        });


// Исправленный observer
        notesViewModel.getAllNotes().observe(this, relationNotes -> {
            // Используем новый метод маппера
            adapter.updateList(relationNotes);
        });


        addNoteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Long currentId = authManager.getId();

                // Лог поможет вам увидеть проблему в Logcat
                android.util.Log.d("DEBUG_ID", "Current User ID: " + currentId);

                if (currentId == null || currentId == 0) {
                    Toast.makeText(MainNotesActivity.this, "Ошибка: ID пользователя не найден!", Toast.LENGTH_LONG).show();
                    return;
                }

                NoteEntity noteEntity = new NoteEntity();
                noteEntity.userId = currentId;

                // 2. Устанавливаем базовые значения (необязательно, но полезно)
                noteEntity.previewTitle = "Новая заметка";
                noteEntity.createdAt = System.currentTimeMillis();

                notesViewModel.upsertNote(noteEntity);
            }
        });
    }
}
