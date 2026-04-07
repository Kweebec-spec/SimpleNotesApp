package com.example.simplenotesapp.fragments_folder;

import android.app.TimePickerDialog;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.example.simplenotesapp.MyApplication;
import com.example.simplenotesapp.R;
import com.example.simplenotesapp.activity_manager.AuthManager;
import com.example.simplenotesapp.activity_manager.NotificationPrefsManager;
import com.example.simplenotesapp.model.ThemeModel;
import com.example.simplenotesapp.viewModel.ThemeViewModel;
import com.example.simplenotesapp.viewModel.ThemeViewModelFactory;
import com.google.android.material.button.MaterialButton;

import com.google.android.material.textview.MaterialTextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Фрагмент настроек уведомлений.
 */
public class NotificationSettingsFragment extends Fragment {

    private ImageView go_out_of_here, bottom_sheet_menu;
    private RadioGroup radioGroupFrequency;
    private MaterialTextView textTime;
    private Spinner spinnerThemes;
    private MaterialButton btnSave;
    private ThemeViewModel themeViewModel;
    private List<ThemeModel> themesList = new ArrayList<>();
    private int hour = 9, minute = 0;

    // добавляем менеджеры как поля — используются в нескольких методах
    private NotificationPrefsManager notificationPrefsManager;
    private AuthManager authManager;
    private MyApplication app;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notification_settings, container, false);

        go_out_of_here = view.findViewById(R.id.go_out_of_here);
        bottom_sheet_menu = view.findViewById(R.id.bottom_sheet_menu);
        radioGroupFrequency = view.findViewById(R.id.radioGroupFrequency);
        textTime = view.findViewById(R.id.textTime);
        spinnerThemes = view.findViewById(R.id.spinnerThemes);
        btnSave = view.findViewById(R.id.btnSave);

        // берём из MyApplication
        app = (MyApplication) requireActivity().getApplication();
        notificationPrefsManager = app.getNotificationPrefsManager();
        authManager = app.getAuthManager();

        setupViewModel();
        loadThemes();
        loadCurrentSettings();
        updateTimeText();

        go_out_of_here.setOnClickListener(v ->
                Navigation.findNavController(requireView())
                        .navigate(R.id.action_notificationSettings_to_mainNotes)
        );

        bottom_sheet_menu.setOnClickListener(v -> {
            new BottomSheetMenu().show(getParentFragmentManager(), "BottomSheetMenu");
        });

        textTime.setOnClickListener(v -> showTimePicker());
        btnSave.setOnClickListener(v -> saveSettings());

        return view;
    }

    private void setupViewModel() {

        Long currentUserId = authManager.getId();
        long userId = (currentUserId != null) ? currentUserId : -1L;


        ThemeViewModelFactory factory = new ThemeViewModelFactory(app.getThemeRepository(), userId);
        themeViewModel = new ViewModelProvider(this, factory).get(ThemeViewModel.class);


    }

    private void loadCurrentSettings() {
        // данные через notificationManager — никаких SharedPreferences напрямую
        switch (notificationPrefsManager.getFrequency()) {
            case 0: radioGroupFrequency.check(R.id.radioDay); break;
            case 1: radioGroupFrequency.check(R.id.radioWeek); break;
            case 2: radioGroupFrequency.check(R.id.radioMonth); break;
            case 3: radioGroupFrequency.check(R.id.radioYear); break;
        }
        hour = notificationPrefsManager.getHour();
        minute = notificationPrefsManager.getMinute();
    }

    private void loadThemes() {
        themeViewModel.getThemes().observe(getViewLifecycleOwner(), themes -> {
            themesList.clear();
            themesList.addAll(themes);

            List<String> themeNames = new ArrayList<>();
            themeNames.add("All notes");
            for (ThemeModel theme : themes) {
                themeNames.add(theme.getName());
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                    requireContext(), android.R.layout.simple_spinner_item, themeNames);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerThemes.setAdapter(adapter);

            // savedThemeId через notificationManager
            long savedThemeId = notificationPrefsManager.getThemeId();
            if (savedThemeId != -1) {
                for (int i = 0; i < themesList.size(); i++) {
                    if (themesList.get(i).getId() == savedThemeId) {
                        spinnerThemes.setSelection(i + 1); // +1 из-за "All notes"
                        break;
                    }
                }
            } else {
                spinnerThemes.setSelection(0);
            }
        });
    }

    private void showTimePicker() {
        new TimePickerDialog(requireContext(), (view, hourOfDay, minuteOfHour) -> {
            hour = hourOfDay;
            minute = minuteOfHour;
            updateTimeText();
        }, hour, minute, true).show();
    }

    private void updateTimeText() {
        textTime.setText(String.format("%02d:%02d", hour, minute));
    }

    private void saveSettings() {
        int selectedId = radioGroupFrequency.getCheckedRadioButtonId();
        int frequency = 0;
        if (selectedId == R.id.radioWeek) frequency = 1;
        else if (selectedId == R.id.radioMonth) frequency = 2;
        else if (selectedId == R.id.radioYear) frequency = 3;

        long themeId = -1;
        String themeName = null;
        int pos = spinnerThemes.getSelectedItemPosition();
        if (pos > 0) {
            ThemeModel selectedTheme = themesList.get(pos - 1);
            themeId = selectedTheme.getId();
            themeName = selectedTheme.getName();
        }

        // saveSettingsAndSchedule — сохраняет и планирует AlarmManager
        notificationPrefsManager.saveSettingsAndSchedule(
                requireContext(),
                notificationPrefsManager.isEnabled(), // сохраняем текущий статус enabled
                frequency,
                hour,
                minute,
                themeId,
                themeName
        );

        Toast.makeText(requireContext(), "Settings saved", Toast.LENGTH_SHORT).show();
    }
}