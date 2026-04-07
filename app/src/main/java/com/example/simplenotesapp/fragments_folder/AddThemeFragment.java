package com.example.simplenotesapp.fragments_folder;

import static com.example.simplenotesapp.utils.ColorUtils.isValidHex;
import static com.example.simplenotesapp.utils.ColorUtils.setColor;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.simplenotesapp.MyApplication;
import com.example.simplenotesapp.R;
import com.example.simplenotesapp.activity_manager.AuthManager;
import com.example.simplenotesapp.adapters.ThemeAdapter;
import com.example.simplenotesapp.model.ThemeModel;
import com.example.simplenotesapp.viewModel.NotesViewModel;
import com.example.simplenotesapp.viewModel.NotesViewModelFactory;
import com.example.simplenotesapp.viewModel.ThemeViewModel;
import com.example.simplenotesapp.viewModel.ThemeViewModelFactory;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textfield.TextInputEditText;

import java.util.List;

/**
 * Фрагмент для создания и управления темами.
 */
public class AddThemeFragment extends Fragment {

    private ImageView go_out_of_here, bottom_sheet_menu;
    private TextInputEditText editThemeName, editColorHex;
    private MaterialCardView notePreview;
    private View previewColorCircle;
    private MaterialButton btnSave, btnDeleteTheme;
    private ThemeViewModel themeViewModel;
    private NotesViewModel notesViewModel;
    private List<ThemeModel> existingThemes; // теперь список моделей
    private RecyclerView recyclerViewThemes;
    private ThemeAdapter themeAdapter;
    private TabLayout tabLayout;
    private LinearLayout createThemeSection, manageThemesSection;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.add_theme_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupViewModel();
        setupTabs();
        setupRecyclerView();
        setupListeners();

        go_out_of_here.setOnClickListener(v ->
                Navigation.findNavController(requireView()).navigate(R.id.action_addTheme_to_mainNotes)
        );
    }

    private void initViews(View view) {
        bottom_sheet_menu = view.findViewById(R.id.bottom_sheet_menu);
        go_out_of_here = view.findViewById(R.id.go_out_of_here);
        editThemeName = view.findViewById(R.id.editThemeName);
        editColorHex = view.findViewById(R.id.editColorHex);
        notePreview = view.findViewById(R.id.notePreview);
        previewColorCircle = notePreview.findViewById(R.id.themeColorView);
        btnSave = view.findViewById(R.id.btnSave);
        btnDeleteTheme = view.findViewById(R.id.btnDeleteTheme);
        recyclerViewThemes = view.findViewById(R.id.recyclerViewThemes);
        tabLayout = view.findViewById(R.id.tabLayout);
        createThemeSection = view.findViewById(R.id.createThemeSection);
        manageThemesSection = view.findViewById(R.id.manageThemesSection);
    }

    private void setupViewModel() {
        MyApplication app = (MyApplication) requireActivity().getApplication();

        // Получаем ID текущего пользователя
        AuthManager authManager = app.getAuthManager();
        Long currentUserId = authManager.getId();
        long userId = (currentUserId != null) ? currentUserId : -1L;

        ThemeViewModelFactory themeFactory = new ThemeViewModelFactory(app.getThemeRepository(), userId);
        themeViewModel = new ViewModelProvider(this, themeFactory).get(ThemeViewModel.class);

        // NotesViewModel for updating notes (userId не нужен для сброса тем)
        NotesViewModelFactory notesFactory = new NotesViewModelFactory(app.getNotesRepository(), 0);
        notesViewModel = new ViewModelProvider(this, notesFactory).get(NotesViewModel.class);

        // Подписываемся на список тем (LiveData<List<ThemeModel>>)
        themeViewModel.getThemes().observe(getViewLifecycleOwner(), themes -> {
            existingThemes = themes;
            if (themeAdapter != null) {
                themeAdapter.submitList(themes);
            }
        });
    }

    private void setupTabs() {
        tabLayout.addTab(tabLayout.newTab().setText("Create Theme"));
        tabLayout.addTab(tabLayout.newTab().setText("Manage Themes"));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    createThemeSection.setVisibility(View.VISIBLE);
                    manageThemesSection.setVisibility(View.GONE);
                } else {
                    createThemeSection.setVisibility(View.GONE);
                    manageThemesSection.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void setupRecyclerView() {
        themeAdapter = new ThemeAdapter();
        recyclerViewThemes.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerViewThemes.setAdapter(themeAdapter);

        themeAdapter.setOnThemeDeleteListener(this::showDeleteConfirmationDialog);
        themeAdapter.setOnThemeEditListener(this::editTheme);
    }

    private void setupListeners() {
        editColorHex.setText("#00000000");
        updatePreviewColor("#00000000");

        editColorHex.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                updatePreviewColor(s.toString().trim());
            }
        });

        bottom_sheet_menu.setOnClickListener(v ->{
            BottomSheetMenu bottomSheetDialogFragment = new BottomSheetMenu();
            bottomSheetDialogFragment.show(getParentFragmentManager(), "BottomSheetMenu");
        });

        btnSave.setOnClickListener(v -> saveTheme());
        btnDeleteTheme.setOnClickListener(v -> showBulkDeleteDialog());
    }

    private void saveTheme() {
        String name = editThemeName.getText().toString().trim();
        String hex = editColorHex.getText().toString().trim();

        if (name.isEmpty()) {
            editThemeName.setError("Type name");
            return;
        }
        if (!isValidHex(hex)) {
            editColorHex.setError("Incorrect HEX color (use #AARRGGBB format)");
            return;
        }

        if (existingThemes != null) {
            for (ThemeModel theme : existingThemes) {
                if (theme.getName().equalsIgnoreCase(name)) {
                    showThemeExistsDialog(name);
                    return;
                }
            }
            for (ThemeModel theme : existingThemes) {
                if (theme.getColor().equalsIgnoreCase(hex)) {
                    showColorExistsDialog(hex, theme.getName());
                    return;
                }
            }
        }

        ThemeModel theme = new ThemeModel(name, hex, 0); // id=0 для новой записи
        themeViewModel.upsert(theme);

        Toast.makeText(requireContext(), "Theme [" + name + "] created!", Toast.LENGTH_SHORT).show();
        clearInputs();
    }

    private void showThemeExistsDialog(String name) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Theme already exists")
                .setMessage("Theme [" + name + "] already exists. Overwrite?")
                .setPositiveButton("Overwrite", (dialog, which) -> {
                    for (ThemeModel theme : existingThemes) {
                        if (theme.getName().equalsIgnoreCase(name)) {
                            theme.setColor(editColorHex.getText().toString().trim());
                            themeViewModel.upsert(theme);
                            Toast.makeText(requireContext(), "Theme updated!", Toast.LENGTH_SHORT).show();
                            clearInputs();
                            break;
                        }
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showColorExistsDialog(String hex, String themeName) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Color already used")
                .setMessage("Color " + hex + " is used by theme '" + themeName + "'.")
                .setPositiveButton("OK", null)
                .show();
    }

    private void showDeleteConfirmationDialog(ThemeModel theme) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Theme")
                .setMessage("Delete theme '" + theme.getName() + "'?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    // First reset all notes using this theme
                    if (notesViewModel != null) {
                        notesViewModel.resetThemeForNotes(theme.getId());
                    }
                    // Then delete the theme
                    themeViewModel.delete(theme);
                    Toast.makeText(requireContext(), "Theme deleted", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showBulkDeleteDialog() {
        if (existingThemes == null || existingThemes.isEmpty()) {
            Toast.makeText(requireContext(), "No themes to delete", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] themeNames = new String[existingThemes.size()];
        boolean[] selected = new boolean[existingThemes.size()];

        for (int i = 0; i < existingThemes.size(); i++) {
            themeNames[i] = existingThemes.get(i).getName();
        }

        new AlertDialog.Builder(requireContext())
                .setTitle("Select themes to delete")
                .setMultiChoiceItems(themeNames, selected, (dialog, which, isChecked) -> selected[which] = isChecked)
                .setPositiveButton("Delete", (dialog, which) -> {
                    int count = 0;
                    for (int i = 0; i < selected.length; i++) {
                        if (selected[i]) {
                            ThemeModel theme = existingThemes.get(i);
                            // Reset notes first
                            notesViewModel.resetThemeForNotes(theme.getId());
                            // Then delete theme
                            themeViewModel.delete(theme);
                            count++;
                        }
                    }
                    if (count > 0) {
                        Toast.makeText(requireContext(), count + " theme(s) deleted", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void editTheme(ThemeModel theme) {
        tabLayout.getTabAt(0).select();
        editThemeName.setText(theme.getName());
        editColorHex.setText(theme.getColor());
        updatePreviewColor(theme.getColor());

        btnSave.setText("Update Theme");
        btnSave.setOnClickListener(v -> updateTheme(theme));
    }

    private void updateTheme(ThemeModel oldTheme) {
        String name = editThemeName.getText().toString().trim();
        String hex = editColorHex.getText().toString().trim();

        if (name.isEmpty()) {
            editThemeName.setError("Type name");
            return;
        }
        if (!isValidHex(hex)) {
            editColorHex.setError("Incorrect HEX color");
            return;
        }

        oldTheme.setName(name);
        oldTheme.setColor(hex);
        themeViewModel.upsert(oldTheme);

        Toast.makeText(requireContext(), "Theme updated!", Toast.LENGTH_SHORT).show();

        btnSave.setText("Save Theme");
        btnSave.setOnClickListener(v -> saveTheme());
        clearInputs();
        tabLayout.getTabAt(1).select();
    }

    private void clearInputs() {
        editThemeName.setText("");
        editColorHex.setText("#00000000");
        updatePreviewColor("#00000000");
    }

    private void updatePreviewColor(String hex) {
        setColor(previewColorCircle, hex);
    }
}