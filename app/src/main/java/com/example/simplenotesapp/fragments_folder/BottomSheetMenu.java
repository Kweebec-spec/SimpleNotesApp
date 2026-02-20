package com.example.simplenotesapp.fragments_folder;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;

import com.example.simplenotesapp.activitys.AuthScreen;
import com.example.simplenotesapp.R;
import com.example.simplenotesapp.activity_manager.AuthManager;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.switchmaterial.SwitchMaterial;

public class BottomSheetMenu extends BottomSheetDialogFragment {

    private void showAboutDialog() {
        new com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
                .setTitle("Natatki")
                .setMessage("Ver 1.0.0\n\n Natatki - notes app here for you to save your thoughts and goal and any text business you have on your mind ! \n\nMade with great effort and help of internet")
                .setPositiveButton("Understood", (dialog, which) -> dialog.dismiss())
                .setIcon(R.drawable.ic_info_outline) // Здесь иконка информации
                .show();
    }

    private void saveDarkModeThemePreference(boolean isDark) {
        SharedPreferences prefs = requireActivity().getSharedPreferences("userPrefs", Context.MODE_PRIVATE);
        prefs.edit().putBoolean("is_dark_mode", isDark).apply();
    }
    private boolean isDarkModeActive() {
        SharedPreferences prefs = requireActivity().getSharedPreferences("userPrefs", Context.MODE_PRIVATE);
        return prefs.getBoolean("is_dark_mode", false);
    }

    @Override
    public int getTheme() {
        return R.style.AppBottomSheetDialogTheme; // Подключаем наш стиль из themes.xml
    }

    // делает замеры всего в приложении и размешает на экране примерно так
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.layout_settings_bottom_sheet, container, false);

        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        AuthManager authManager = new AuthManager(view.getContext());
        SharedPreferences prefs = view.getContext().getSharedPreferences("userPrefs", Context.MODE_PRIVATE);

        // find all needed from our xml file to use it and add functionality to it.
        SwitchMaterial switchTheme = view.findViewById(R.id.switch_theme);
        LinearLayout switchTheme_item = view.findViewById(R.id.toggleTheme_item);
        LinearLayout notifications_item = view.findViewById(R.id.notifications_item);
        SwitchMaterial switchNotifications = view.findViewById(R.id.switch_notifications);
        LinearLayout logout_item = view.findViewById(R.id.logout_item);
        LinearLayout about_item = view.findViewById(R.id.about_item);
        LinearLayout extra_settings_item = view.findViewById(R.id.extra_settings_item);
        View triggerLine = view.findViewById(R.id.triggerLine);

        switchTheme.setChecked(isDarkModeActive());

        // after click on the line we expand our menu so it can be seen and used by user.
        triggerLine.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                View parent = (View) view.getParent();
                com.google.android.material.bottomsheet.BottomSheetBehavior<View> behavior =
                        com.google.android.material.bottomsheet.BottomSheetBehavior.from(parent);

                if (behavior.getState() != com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_HALF_EXPANDED) {
                    behavior.setState(com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_HALF_EXPANDED);
                } else {
                    dismiss();
                }
            }
        });


        switchTheme.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(@NonNull CompoundButton compoundButton, boolean isChecked) {
                // 1. Сохраняем выбор
                saveDarkModeThemePreference(isChecked);

                if (getActivity() != null) {
                    // 2. Устанавливаем режим (это спровоцирует смену ресурсов)
                    AppCompatDelegate.setDefaultNightMode(isChecked ?
                            AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
                }
            }
        });

       switchTheme_item.setOnClickListener(v -> {
            // Просто меняем состояние свитча,
            // а switchTheme.setOnCheckedChangeListener (выше) сделает остальную работу!
            switchTheme.setChecked(!switchTheme.isChecked());
        });

        logout_item.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                authManager.logout();
                Intent intent = new Intent(getActivity(), AuthScreen.class);
                startActivity(intent);
                if (getActivity() != null) {
                    getActivity().finish(); // Вызываем у Activity
                }
            }
        });

        about_item.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAboutDialog();
            }
        });


    }
}