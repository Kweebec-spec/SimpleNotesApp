package com.example.simplenotesapp.activitys;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.example.simplenotesapp.MyApplication;
import com.example.simplenotesapp.R;
import com.example.simplenotesapp.activity_manager.AuthManager;
import com.example.simplenotesapp.adapters.NotesAdapter;
import com.example.simplenotesapp.dataBase.entity.NoteEntity;
import com.example.simplenotesapp.dataBase.entity.ThemeEntity;
import com.example.simplenotesapp.fragments_folder.BottomSheetMenu;
import com.example.simplenotesapp.model.User;
import com.example.simplenotesapp.viewModel.AuthViewModel;
import com.example.simplenotesapp.viewModel.AuthViewModelFactory;
import com.example.simplenotesapp.viewModel.NotesViewModel;
import com.example.simplenotesapp.viewModel.NotesViewModelFactory;
import com.example.simplenotesapp.viewModel.ThemeViewModel;
import com.example.simplenotesapp.viewModel.ThemeViewModelFactory;

import java.util.ArrayList;
import java.util.List;

public class MainNotesActivity extends AppCompatActivity {

    // UI Components
    private RecyclerView recyclerView;
    private ImageView addNoteButton;
    private ImageView openBottomSheetMenu;
    private ImageView userPhoto;
    private TextView helloUserTextView;

    // Search components
    private EditText searchInput;
    private ImageButton searchButton;
    private ImageButton filterButton;
    private AutoCompleteTextView searchTheme;
    private ImageButton addThemeBtn;
    private ImageButton dropDownBtn;

    // Adapter
    private NotesAdapter notesAdapter;

    // Managers and ViewModel
    private AuthManager authManager;
    private NotesViewModel notesViewModel;
    private AuthViewModel authViewModel;
    private ThemeViewModel themeViewModel;

    // Constants
    private static final int SPAN_COUNT = 2;
    private List<ThemeEntity> allThemes = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        setupAuthManager();
        setupViewModels();
        observeThemes();
        setupRecyclerView();
        setupSearchAndFilter();
        setupClickListeners();
        observeNotes();
        observeUserData();
        loadUserData();
    }

    /**
     * Initialize all UI views
     */
    private void initViews() {
        helloUserTextView = findViewById(R.id.helloUserTxt);
        userPhoto = findViewById(R.id.userPhoto);
        addNoteButton = findViewById(R.id.addNoteBtn);
        recyclerView = findViewById(R.id.recyclerView);
        openBottomSheetMenu = findViewById(R.id.openBottomSheetMenu);

        // Search views
        searchInput = findViewById(R.id.searchInput);
        searchButton = findViewById(R.id.searchButton);
        filterButton = findViewById(R.id.filterButton);
        searchTheme = findViewById(R.id.searchTheme);
        addThemeBtn = findViewById(R.id.addThemeBtn);
        dropDownBtn = findViewById(R.id.dropDownBtn);
    }

    /**
     * Setup AuthManager
     */
    private void setupAuthManager() {
        authManager = new AuthManager(getApplicationContext());
    }

    /**
     * Setup ViewModels with singleton repositories from Application
     */
    private void setupViewModels() {
        MyApplication app = (MyApplication) getApplication();

        // NotesViewModel
        Long currentUserId = authManager.getId();
        if (currentUserId == null) currentUserId = -1L;
        NotesViewModelFactory notesViewModelFactory = new NotesViewModelFactory(app.getNotesRepository(), currentUserId);
        notesViewModel = new ViewModelProvider(this, notesViewModelFactory).get(NotesViewModel.class);

        // ThemeViewModel
        ThemeViewModelFactory themeFactory = new ThemeViewModelFactory(app.getThemeRepository());
        themeViewModel = new ViewModelProvider(this, themeFactory).get(ThemeViewModel.class);

        // AuthViewModel
        AuthViewModelFactory authViewModelFactory = new AuthViewModelFactory(app.getUserRepository());
        authViewModel = new ViewModelProvider(this, authViewModelFactory).get(AuthViewModel.class);
    }

    /**
     * Observe themes from ViewModel
     */
    private void observeThemes() {
        themeViewModel.getThemes().observe(this, themes -> {
            allThemes.clear();
            if (themes != null) {
                allThemes.addAll(themes);
            }
            setupThemeAutoComplete();
        });
    }

    /**
     * Load user data from AuthViewModel
     */
    private void loadUserData() {
        String userName = authManager.getUserName();
        if (userName != null && !userName.isEmpty()) {
            updateUserGreeting(userName);
            return;
        }

        // Получаем пользователя из AuthViewModel
        User user = authViewModel.getLoggedInUser().getValue();
        if (user != null) {
            updateUserGreeting(user.getUsername());
            authManager.updateUserName(user.getUsername());
        } else {
            helloUserTextView.setText("Hello, User!");
        }
    }

    /**
     * Observe user data from AuthViewModel
     */
    private void observeUserData() {
        authViewModel.getLoggedInUser().observe(this, user -> {
            if (user != null) {
                updateUserGreeting(user.getUsername());
                authManager.updateUserName(user.getUsername());
            }
        });
    }

    /**
     * Setup RecyclerView with adapter and layout manager
     */
    private void setupRecyclerView() {
        notesAdapter = new NotesAdapter();
        recyclerView.setAdapter(notesAdapter);

        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(
                SPAN_COUNT,
                StaggeredGridLayoutManager.VERTICAL
        );

        layoutManager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(false);

        setupNoteClickListener();
        notesAdapter.setOnThemeClickListener((note, anchor) -> showThemeSelectionDialog(note));
    }

    /**
     * Setup click listener for notes in the adapter
     */
    private void setupNoteClickListener() {
        notesAdapter.setOnNoteClickListener(note -> {
            if (note != null) {
                openNoteScreen(note.getId());
            }
        });
    }

    /**
     * Setup all click listeners for buttons
     */
    private void setupClickListeners() {
        addNoteButton.setOnClickListener(v -> createNewNote());

        openBottomSheetMenu.setOnClickListener(view -> {
            BottomSheetMenu bottomSheetDialogFragment = new BottomSheetMenu();
            bottomSheetDialogFragment.show(getSupportFragmentManager(), "BottomSheetMenu");
        });

        addThemeBtn.setOnClickListener(v -> startActivity(new Intent(this, AddThemeActivity.class)));
    }

    /**
     * Setup search and filter functionality
     */
    private void setupSearchAndFilter() {
        // Поиск по тексту при нажатии на кнопку поиска
        searchButton.setOnClickListener(v -> {
            String query = searchInput.getText().toString().trim();
            notesViewModel.searchNotes(query);
        });

        // Поиск по тексту в реальном времени
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String query = s.toString().trim();
                notesViewModel.searchNotes(query);
            }
        });

        // Фильтр по теме при нажатии на filterButton
        filterButton.setOnClickListener(v -> showThemeFilterDialog());

        // Поиск по теме во втором поиске
        searchTheme.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String themeQuery = s.toString().trim();
                filterByThemeName(themeQuery);
            }
        });
    }

    /**
     * Show dialog with all themes for filtering
     */
    private void showThemeFilterDialog() {
        if (allThemes.isEmpty()) {
            Toast.makeText(this, "No themes available", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] themeNames = new String[allThemes.size() + 1];
        themeNames[0] = "All themes";
        for (int i = 0; i < allThemes.size(); i++) {
            themeNames[i + 1] = allThemes.get(i).getName();
        }

        new AlertDialog.Builder(this)
                .setTitle("Filter by theme")
                .setItems(themeNames, (dialog, which) -> {
                    if (which == 0) {
                        // All themes
                        notesViewModel.filterByTheme(-1);
                        searchTheme.setText("");
                    } else {
                        // Selected theme
                        ThemeEntity selected = allThemes.get(which - 1);
                        notesViewModel.filterByTheme(selected.getId());
                        searchTheme.setText(selected.getName());
                    }
                })
                .show();
    }

    /**
     * Setup theme auto-complete
     */
    private void setupThemeAutoComplete() {
        List<String> themeNames = new ArrayList<>();
        themeNames.add("All themes");
        for (ThemeEntity theme : allThemes) {
            themeNames.add(theme.getName());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, themeNames);
        searchTheme.setAdapter(adapter);
        searchTheme.setThreshold(1);

        searchTheme.setOnItemClickListener((parent, view, position, id) -> {
            String selected = (String) parent.getItemAtPosition(position);
            if (selected.equals("All themes")) {
                notesViewModel.filterByTheme(-1);
            } else {
                // Находим ID выбранной темы
                for (ThemeEntity theme : allThemes) {
                    if (theme.getName().equals(selected)) {
                        notesViewModel.filterByTheme(theme.getId());
                        break;
                    }
                }
            }
        });

        dropDownBtn.setOnClickListener(v -> searchTheme.showDropDown());
    }

    /**
     * Filter notes by theme name
     */
    private void filterByThemeName(String themeName) {
        if (themeName.isEmpty() || themeName.equals("All themes")) {
            notesViewModel.filterByTheme(-1);
            return;
        }

        for (ThemeEntity theme : allThemes) {
            if (theme.getName().toLowerCase().contains(themeName.toLowerCase())) {
                notesViewModel.filterByTheme(theme.getId());
                return;
            }
        }
        // If no theme found, show all
        notesViewModel.filterByTheme(-1);
    }

    /**
     * Create a new note
     */
    private void createNewNote() {
        Long currentUserId = authManager.getId();

        if (currentUserId == null || currentUserId <= 0) {
            Toast.makeText(this, "User ID is invalid", Toast.LENGTH_SHORT).show();
            return;
        }

        NoteEntity newNote = new NoteEntity();
        newNote.setUserId(currentUserId);
        newNote.setTitle("");
        newNote.setText("");

        notesViewModel.createNoteAndOpen(newNote, this);
    }

    /**
     * Show theme selection dialog for a note with "No theme" option
     */
    private void showThemeSelectionDialog(NoteEntity note) {
        if (allThemes.isEmpty()) {
            // Если тем нет, показываем только опцию "No theme"
            new AlertDialog.Builder(this)
                    .setTitle("Choose theme")
                    .setItems(new String[]{"No theme"}, (dialog, which) -> {
                        note.setThemeId(null);
                        note.setColor(null);
                        notesViewModel.updateNote(note);
                        int position = notesAdapter.getCurrentList().indexOf(note);
                        if (position >= 0) {
                            notesAdapter.notifyItemChanged(position);
                        }
                    })
                    .show();
            return;
        }

        // Создаем массив с опциями: сначала "No theme", потом все темы
        String[] names = new String[allThemes.size() + 1];
        names[0] = "No theme";
        for (int i = 0; i < allThemes.size(); i++) {
            names[i + 1] = allThemes.get(i).getName();
        }

        new AlertDialog.Builder(this)
                .setTitle("Choose theme")
                .setItems(names, (dialog, which) -> {
                    if (which == 0) {
                        // Выбрана опция "No theme"
                        note.setThemeId(null);
                        note.setColor(null);
                    } else {
                        // Выбрана конкретная тема
                        ThemeEntity selected = allThemes.get(which - 1);
                        note.setThemeId(selected.getId());
                        note.setColor(selected.getColor());
                    }
                    notesViewModel.updateNote(note);
                    int position = notesAdapter.getCurrentList().indexOf(note);
                    if (position >= 0) {
                        notesAdapter.notifyItemChanged(position);
                    }
                })
                .show();
    }

    /**
     * Open existing note screen
     * @param noteId ID of the note to open
     */
    private void openNoteScreen(long noteId) {
        notesViewModel.setCurrentNoteId(noteId);
        Intent intent = new Intent(MainNotesActivity.this, NoteScreen.class);
        intent.putExtra("noteId", noteId);
        startActivity(intent);
    }

    /**
     * Observe notes from ViewModel and update UI
     */
    private void observeNotes() {
        notesViewModel.getFilteredNotes().observe(this, notes -> {
            notesAdapter.submitList(notes);
            updateEmptyState(notes == null || notes.isEmpty());
        });
    }

    /**
     * Update UI based on empty state
     */
    private void updateEmptyState(boolean isEmpty) {
        // добавить отображение пустого состояния
    }

    /**
     * Update user greeting with username
     */
    private void updateUserGreeting(String userName) {
        String baseGreeting = getString(R.string.hello_user);
        helloUserTextView.setText(baseGreeting + " " + userName + " !");
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (notesViewModel != null) {
            notesViewModel.refreshNotes();
        }
        loadUserData();
    }
}