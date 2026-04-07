package com.example.simplenotesapp.fragments_folder;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.example.simplenotesapp.MyApplication;
import com.example.simplenotesapp.R;
import com.example.simplenotesapp.activity_manager.AuthManager;
import com.example.simplenotesapp.adapters.NotesAdapter;
import com.example.simplenotesapp.model.Note;
import com.example.simplenotesapp.model.ThemeModel;
import com.example.simplenotesapp.model.User;
import com.example.simplenotesapp.utils.KeyboardUtils;
import com.example.simplenotesapp.viewModel.AuthViewModel;
import com.example.simplenotesapp.viewModel.AuthViewModelFactory;
import com.example.simplenotesapp.viewModel.NotesViewModel;
import com.example.simplenotesapp.viewModel.NotesViewModelFactory;
import com.example.simplenotesapp.viewModel.ThemeViewModel;
import com.example.simplenotesapp.viewModel.ThemeViewModelFactory;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

/**
 * Главный экран со списком заметок.
 * Фильтрация происходит только по нажатию на кнопку поиска (лупа).
 * При выборе темы из выпадающего списка или диалога запрос не применяется.
 * Если пользователь очищает поле темы, выбор сбрасывается на "All themes".
 */
public class MainNotesFragment extends Fragment {

    private static final String TAG = "MainNotesFragment";
    private static final int SPAN_COUNT = 2;

    // UI components
    private ImageView rv_emptyStateImage;
    private RecyclerView recyclerView;
    private MaterialButton addNoteButton;
    private TextView helloUserTextView;
    private ShapeableImageView userPhoto;
    private TextInputEditText searchInput;          // поле поиска по заголовку/тексту
    private ImageButton searchButton;               // кнопка поиска – по нажатию применяются фильтры
    private ImageButton filterButton;               // кнопка для выпадающего списка тем
    private MaterialAutoCompleteTextView searchTheme; // поле выбора темы (только сохранение выбора)
    private MaterialButton addThemeBtn;
    private ImageView openBottomSheetMenu;

    // Adapter & ViewModels
    private NotesAdapter notesAdapter;
    private AuthManager authManager;
    private NotesViewModel notesViewModel;
    private AuthViewModel authViewModel;
    private ThemeViewModel themeViewModel;

    // Data
    private List<ThemeModel> allThemes = new ArrayList<>(); // все темы, полученные из БД
    private long selectedThemeId = -1;                     // -1 означает "все темы"
    private String selectedThemeName = "All themes";       // для отображения в AutoCompleteTextView
    private boolean isUpdatingThemeText = false;           // флаг, чтобы не реагировать на программные изменения текста

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main_notes, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupAuthManager();
        setupViewModels();
        observeThemes();
        setupRecyclerView();
        setupSearchAndFilter();      // устанавливаем слушатель на кнопку поиска (и только на неё)
        setupClickListeners();
        observeNotes();
        observeUserData();
        loadUserData();
    }

    public void deleteNoteObserver(){
        notesAdapter.setOnNoteLongClickListener((note, position) -> {
            AlertDialog dialog = new AlertDialog.Builder(requireContext())
                    .setTitle("Delete Note")
                    .setIcon(R.drawable.ic_alert_delete_check)
                    .setMessage("Are you sure you want to delete \"" + note.getTitle() + "\"?")
                    .setPositiveButton("Delete", (d, which) -> {
                        notesViewModel.deleteNote(note);
                    })
                    .setNegativeButton("Cancel", null)
                    .create();  // ← create() instead of show()

            // Set background before showing
            dialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_delete_bg);
            dialog.show();  // ← show() here instead
        });
    }

    private void initViews(View view) {
        rv_emptyStateImage = view.findViewById(R.id.rv_emptyStateImage);
        helloUserTextView = view.findViewById(R.id.helloUserTxt);
        userPhoto = view.findViewById(R.id.userPhoto);
        addNoteButton = view.findViewById(R.id.addNoteBtn);
        recyclerView = view.findViewById(R.id.recyclerView);
        openBottomSheetMenu = view.findViewById(R.id.openBottomSheetMenu);
        searchInput = view.findViewById(R.id.searchInput);
        searchButton = view.findViewById(R.id.searchButton);
        filterButton = view.findViewById(R.id.filterButton);
        searchTheme = view.findViewById(R.id.searchTheme);
        addThemeBtn = view.findViewById(R.id.addThemeBtn);
    }

    private void setupAuthManager() {
        authManager = new AuthManager(requireContext());
    }

    private void setupViewModels() {
        MyApplication app = (MyApplication) requireActivity().getApplication();

        Long currentUserId = authManager.getId();
        if (currentUserId == null) currentUserId = -1L;

        // NotesViewModel
        NotesViewModelFactory notesFactory = new NotesViewModelFactory(app.getNotesRepository(), currentUserId);
        notesViewModel = new ViewModelProvider(this, notesFactory).get(NotesViewModel.class);

        // ThemeViewModel
        ThemeViewModelFactory themeFactory = new ThemeViewModelFactory(app.getThemeRepository(), currentUserId);
        themeViewModel = new ViewModelProvider(this, themeFactory).get(ThemeViewModel.class);

        // AuthViewModel
        AuthViewModelFactory authFactory = new AuthViewModelFactory(app.getUserRepository());
        authViewModel = new ViewModelProvider(requireActivity(), authFactory).get(AuthViewModel.class);
    }

    /**
     * Наблюдаем за списком тем из ViewModel.
     * При получении обновления сохраняем список и обновляем AutoCompleteTextView.
     */
    private void observeThemes() {
        themeViewModel.getThemes().observe(getViewLifecycleOwner(), themes -> {
            allThemes.clear();
            if (themes != null) {
                allThemes.addAll(themes);
                // Логируем все полученные темы для отладки
                for (ThemeModel t : allThemes) {
                    Log.d(TAG, "Loaded theme: " + t.getName() + " ID=" + t.getId());
                }
            }
            setupThemeAutoComplete();  // обновляем выпадающий список тем
        });
    }

    private void setupRecyclerView() {
        notesAdapter = new NotesAdapter();
        recyclerView.setAdapter(notesAdapter);

        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(
                SPAN_COUNT, StaggeredGridLayoutManager.VERTICAL);
        layoutManager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(false);

        notesAdapter.setOnNoteClickListener(note -> {
            if (note != null) openNoteScreen(note.getId());
        });
        notesAdapter.setOnThemeClickListener(this::showThemeSelectionDialog);


        // ✅ ADD THIS — dismiss keyboard when tapping empty space in RV
        recyclerView.addOnItemTouchListener(new RecyclerView.SimpleOnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
                if (e.getAction() == MotionEvent.ACTION_DOWN) {
                    View child = rv.findChildViewUnder(e.getX(), e.getY());
                    if (child == null) { // ← tapped on empty space, not on a note item
                        KeyboardUtils.handleDispatchTouchEvent(e, requireActivity());
                    }
                }
                return false; // never consume the event
            }
        });
        deleteNoteObserver();
    }

    private void setupClickListeners() {
        addNoteButton.setOnClickListener(v -> createNewNote());

        openBottomSheetMenu.setOnClickListener(v -> {
            BottomSheetMenu bottomSheet = new BottomSheetMenu();
            bottomSheet.show(getParentFragmentManager(), "BottomSheetMenu");
        });

        addThemeBtn.setOnClickListener(v -> {
            Navigation.findNavController(requireView())
                    .navigate(R.id.action_mainNotes_to_addTheme);
        });

        // Кнопка фильтра – по нажатию открывает выпадающий список тем
        filterButton.setOnClickListener(v -> searchTheme.showDropDown());
        // Долгое нажатие – открывает диалог выбора темы
        filterButton.setOnLongClickListener(v -> {
            showThemeFilterDialog();
            return true;
        });

    }

    // ------------------------------------------------------------------------
    // Поиск и фильтрация только по кнопке
    // ------------------------------------------------------------------------

    private void setupSearchAndFilter() {
        // Убираем любые TextWatchers – фильтрация только по клику на кнопку
        searchButton.setOnClickListener(v -> applyFilters());
    }

    private void applyFilters() {
        String query = searchInput.getText().toString().trim();
        String themeDisplay = (selectedThemeId == -1) ? "All themes" : selectedThemeName;

        // Логируем и показываем тост для отладки
        Log.d(TAG, "Apply filters: themeId=" + selectedThemeId + ", query=\"" + query + "\"");
        Toast.makeText(requireContext(),
                "Filtering: " + themeDisplay + (query.isEmpty() ? "" : " | search: \"" + query + "\""),
                Toast.LENGTH_SHORT).show();

        // Передаём выбранную тему и поисковый запрос в ViewModel
        notesViewModel.filterByTheme(selectedThemeId);
        notesViewModel.searchNotes(query);
    }

    // ------------------------------------------------------------------------
    // Выбор темы – только сохранение выбора, фильтрация не применяется
    // ------------------------------------------------------------------------

    private void setupThemeAutoComplete() {
        List<String> themeNames = new ArrayList<>();
        themeNames.add("All themes");
        for (ThemeModel theme : allThemes) {
            themeNames.add(theme.getName());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_dropdown_item_1line, themeNames);
        searchTheme.setAdapter(adapter);
        searchTheme.setThreshold(1);

        // Слушатель на выбор из выпадающего списка
        searchTheme.setOnItemClickListener((parent, view, position, id) -> {
            isUpdatingThemeText = true;
            String selected = (String) parent.getItemAtPosition(position);
            if (selected.equals("All themes")) {
                selectedThemeId = -1;
                selectedThemeName = "All themes";
                searchTheme.setText("All themes");
                Log.d(TAG, "Theme selected: All themes (ID=-1)");
            } else {
                // Ищем тему с таким именем в allThemes
                ThemeModel found = null;
                for (ThemeModel theme : allThemes) {
                    if (theme.getName().equals(selected)) {
                        found = theme;
                        break;
                    }
                }
                if (found != null) {
                    selectedThemeId = found.getId();
                    selectedThemeName = found.getName();
                    searchTheme.setText(selectedThemeName); // уже выставлено, но для уверенности
                    Log.d(TAG, "Theme selected: " + selectedThemeName + " ID=" + selectedThemeId);
                } else {
                    Log.e(TAG, "Selected theme not found in allThemes: " + selected);
                    selectedThemeId = -1;
                    selectedThemeName = "All themes";
                    searchTheme.setText("");
                }
            }
            isUpdatingThemeText = false;
            // Фильтр НЕ применяется – пользователь должен нажать на кнопку поиска
        });

        // TextWatcher для отслеживания ручного очищения поля темы
        searchTheme.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (isUpdatingThemeText) return; // игнорируем программные изменения
                String text = s.toString().trim();
                if (text.isEmpty()) {
                    // Пользователь очистил поле темы – сбрасываем выбор на "All themes"
                    selectedThemeId = -1;
                    selectedThemeName = "All themes";
                    Log.d(TAG, "Theme field cleared, reset to All themes");
                    // Фильтр не применяем
                }
            }
        });
    }

    private void showThemeFilterDialog() {
        if (allThemes.isEmpty()) {
            Toast.makeText(requireContext(), "No themes available", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] themeNames = new String[allThemes.size() + 1];
        themeNames[0] = "All themes";
        for (int i = 0; i < allThemes.size(); i++) {
            themeNames[i + 1] = allThemes.get(i).getName();
        }

        new AlertDialog.Builder(requireContext())
                .setTitle("Select theme")
                .setItems(themeNames, (dialog, which) -> {
                    isUpdatingThemeText = true;
                    if (which == 0) {
                        selectedThemeId = -1;
                        selectedThemeName = "All themes";
                        searchTheme.setText("");
                        Log.d(TAG, "Theme selected via dialog: All themes");
                    } else {
                        ThemeModel selected = allThemes.get(which - 1);
                        selectedThemeId = selected.getId();
                        selectedThemeName = selected.getName();
                        searchTheme.setText(selectedThemeName);
                        Log.d(TAG, "Theme selected via dialog: " + selectedThemeName + " ID=" + selectedThemeId);
                    }
                    isUpdatingThemeText = false;
                    // Фильтр не применяется
                })
                .show();
    }

    // ------------------------------------------------------------------------
    // Операции с заметками (без изменений)
    // ------------------------------------------------------------------------

    private void createNewNote() {
        Long currentUserId = authManager.getId();
        if (currentUserId == null || currentUserId <= 0) {
            Toast.makeText(requireContext(), "User ID is invalid", Toast.LENGTH_SHORT).show();
            return;
        }

        Note newNote = new Note();
        newNote.setUserid(currentUserId);
        newNote.setTitle("Unnamed");
        newNote.setContent("");
        newNote.setThemeId(-1);
        newNote.setColor(null);

        addNoteButton.setEnabled(false);

        notesViewModel.createNote(newNote, new NotesViewModel.OnNoteCreatedCallback() {
            @Override
            public void onSuccess(long noteId) {
                addNoteButton.setEnabled(true);
                Bundle args = new Bundle();
                args.putLong("noteId", noteId);
                Navigation.findNavController(requireView())
                        .navigate(R.id.action_mainNotes_to_note, args);
            }

            @Override
            public void onError(Exception e) {
                addNoteButton.setEnabled(true);
                Toast.makeText(requireContext(),
                        "Error creating note: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showThemeSelectionDialog(Note note, View anchor) {
        if (allThemes.isEmpty()) {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Choose theme")
                    .setItems(new String[]{"No theme"}, (dialog, which) -> {
                        note.setThemeId(-1);
                        note.setColor(null);
                        notesViewModel.updateNote(note);
                        int pos = notesAdapter.getCurrentList().indexOf(note);
                        if (pos >= 0) notesAdapter.notifyItemChanged(pos);
                    })
                    .show();
            return;
        }

        String[] names = new String[allThemes.size() + 1];
        names[0] = "No theme";
        for (int i = 0; i < allThemes.size(); i++) {
            names[i + 1] = allThemes.get(i).getName();
        }

        new AlertDialog.Builder(requireContext())
                .setTitle("Choose theme")
                .setItems(names, (dialog, which) -> {
                    if (which == 0) {
                        note.setThemeId(-1);
                        note.setColor(null);
                    } else {
                        ThemeModel selected = allThemes.get(which - 1);
                        note.setThemeId(selected.getId());
                        note.setColor(selected.getColor());
                    }
                    notesViewModel.updateNote(note);
                    int pos = notesAdapter.getCurrentList().indexOf(note);
                    if (pos >= 0) notesAdapter.notifyItemChanged(pos);
                })
                .show();
    }

    private void openNoteScreen(long noteId) {
        Bundle args = new Bundle();
        args.putLong("noteId", noteId);
        Navigation.findNavController(requireView())
                .navigate(R.id.action_mainNotes_to_note, args);
    }

    private void observeNotes() {
        notesViewModel.getFilteredNotes().observe(getViewLifecycleOwner(), notes -> {
            notesAdapter.submitList(notes);
            updateEmptyState(notes == null || notes.isEmpty());
        });
    }

    private void updateEmptyState(boolean isEmpty) {
        if (isEmpty) {
            rv_emptyStateImage.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            rv_emptyStateImage.setOnClickListener(v -> createNewNote());
        } else {
            rv_emptyStateImage.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    private void observeUserData() {
        authViewModel.getLoggedInUser().observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                updateUserGreeting(user.getUsername());
                authManager.updateUserName(user.getUsername());
            }
        });
    }

    private void loadUserData() {
        String userName = authManager.getUserName();
        if (userName != null && !userName.isEmpty()) {
            updateUserGreeting(userName);
            return;
        }
        User user = authViewModel.getLoggedInUser().getValue();
        if (user != null) {
            updateUserGreeting(user.getUsername());
            authManager.updateUserName(user.getUsername());
        } else {
            helloUserTextView.setText("Hello, User!");
        }
    }

    private void updateUserGreeting(String userName) {
        String baseGreeting = getString(R.string.hello_user);
        helloUserTextView.setText(baseGreeting + " " + userName + " !");
    }

    @Override
    public void onResume() {
        super.onResume();
        if (notesViewModel != null) notesViewModel.refreshNotes();
        loadUserData();
    }
}