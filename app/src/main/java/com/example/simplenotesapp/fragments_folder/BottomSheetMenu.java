package com.example.simplenotesapp.fragments_folder;

import android.Manifest;
import android.app.AlarmManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.navigation.Navigation;

import com.example.simplenotesapp.MyApplication;
import com.example.simplenotesapp.R;
import com.example.simplenotesapp.activity_manager.AuthManager;
import com.example.simplenotesapp.activity_manager.NotificationPrefsManager;
import com.example.simplenotesapp.activity_manager.ThemeManager;
import com.example.simplenotesapp.activitys.AuthScreen;
import com.example.simplenotesapp.utils.NotificationHelper;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.switchmaterial.SwitchMaterial;

public class BottomSheetMenu extends BottomSheetDialogFragment {

    private static final int PERMISSION_REQUEST_NOTIFICATIONS = 1001;

    // поля класса — используются в нескольких методах
    private boolean isWaitingForPermission = false;
    private SwitchMaterial switchNotifications;
    private AuthManager authManager;
    private ThemeManager themeManager;
    private NotificationPrefsManager notificationPrefsManager;

    @Override
    public int getTheme() {
        return R.style.AppBottomSheetDialogTheme;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_settings_bottom_sheet, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        MyApplication app = (MyApplication) requireActivity().getApplication();
        authManager = app.getAuthManager();
        themeManager = app.getThemeManager();
        notificationPrefsManager = app.getNotificationPrefsManager();

        // Views
        SwitchMaterial switchTheme = view.findViewById(R.id.switch_theme);
        LinearLayout switchThemeItem = view.findViewById(R.id.toggleTheme_item);
        LinearLayout notificationsItem = view.findViewById(R.id.notifications_item);
        switchNotifications = view.findViewById(R.id.switch_notifications);
        LinearLayout logoutItem = view.findViewById(R.id.logout_item);
        LinearLayout aboutItem = view.findViewById(R.id.about_item);
        View triggerLine = view.findViewById(R.id.triggerLine);

        // Начальное состояние — берём из менеджеров
        switchTheme.setChecked(themeManager.isDarkModeActive());
        switchNotifications.setChecked(notificationPrefsManager.isEnabled());

        // Тема
        switchTheme.setOnCheckedChangeListener((button, isChecked) -> {
            themeManager.saveDarkModeThemePreference(isChecked);
            AppCompatDelegate.setDefaultNightMode(isChecked ?
                    AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
        });
        switchThemeItem.setOnClickListener(v -> switchTheme.setChecked(!switchTheme.isChecked()));

        // Уведомления — свитч
        switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isWaitingForPermission) return;
            if (isChecked) handleEnableNotifications();
            else disableNotifications();
        });

        // Уведомления — клик на весь item открывает настройки
        notificationsItem.setOnClickListener(v -> {
            dismiss();
            if (getActivity() != null) {
                Navigation.findNavController(getActivity(), R.id.nav_host_fragment)
                        .navigate(R.id.notificationSettingsFragment);
            }
        });

        // Expand/collapse
        triggerLine.setOnClickListener(v -> {
            View parent = (View) v.getParent();
            com.google.android.material.bottomsheet.BottomSheetBehavior<View> behavior =
                    com.google.android.material.bottomsheet.BottomSheetBehavior.from(parent);
            if (behavior.getState() != com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_HALF_EXPANDED) {
                behavior.setState(com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_HALF_EXPANDED);
            } else {
                dismiss();
            }
        });

        // Logout
        logoutItem.setOnClickListener(v -> {
            authManager.logout();
            Intent intent = new Intent(getActivity(), AuthScreen.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            if (getActivity() != null) getActivity().finish();
        });

        // About
        aboutItem.setOnClickListener(v -> showAboutDialog());
    }

    // ─── Notifications logic ──────────────────────────────────────────────────

    private void handleEnableNotifications() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                requestNotificationPermission();
                return;
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager alarmManager = requireContext().getSystemService(AlarmManager.class);
            if (alarmManager != null && !alarmManager.canScheduleExactAlarms()) {
                requestExactAlarmPermission();
                return;
            }
        }
        enableNotifications();
    }

    private void enableNotifications() {
        // ✅ данные из NotificationManager, логика в saveSettingsAndSchedule
        notificationPrefsManager.saveSettingsAndSchedule(
                requireContext(),
                true,
                notificationPrefsManager.getFrequency(),
                notificationPrefsManager.getHour(),
                notificationPrefsManager.getMinute(),
                notificationPrefsManager.getThemeId(),
                notificationPrefsManager.getThemeName()
        );
        Toast.makeText(requireContext(), "Notifications on", Toast.LENGTH_SHORT).show();
    }

    private void disableNotifications() {
        // ✅ только меняем enabled, остальные настройки сохраняем
        notificationPrefsManager.saveSettingsAndSchedule(
                requireContext(),
                false,
                notificationPrefsManager.getFrequency(),
                notificationPrefsManager.getHour(),
                notificationPrefsManager.getMinute(),
                notificationPrefsManager.getThemeId(),
                notificationPrefsManager.getThemeName()
        );
        Toast.makeText(requireContext(), "Notifications off", Toast.LENGTH_SHORT).show();
    }

    private void requestNotificationPermission() {
        isWaitingForPermission = true;
        switchNotifications.setOnCheckedChangeListener(null);
        switchNotifications.setChecked(false);
        requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS},
                PERMISSION_REQUEST_NOTIFICATIONS);
    }

    private void requestExactAlarmPermission() {
        isWaitingForPermission = true;
        switchNotifications.setOnCheckedChangeListener(null);
        switchNotifications.setChecked(false);

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Exact alarms permission")
                .setMessage("This app needs permission to schedule exact alarms.\n\nPlease grant it in the system settings.")
                .setPositiveButton("Open settings", (dialog, which) -> {
                    startActivity(new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM));
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    isWaitingForPermission = false;
                    restoreNotificationListener();
                })
                .show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode != PERMISSION_REQUEST_NOTIFICATIONS) return;

        isWaitingForPermission = false;

        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            handleEnableNotifications();
        } else {
            Toast.makeText(requireContext(), "Permission needed for notifications", Toast.LENGTH_LONG).show();
            switchNotifications.setOnCheckedChangeListener(null);
            switchNotifications.setChecked(false);
            restoreNotificationListener();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        isWaitingForPermission = false;
        if (switchNotifications == null) return;

        boolean isEnabled = notificationPrefsManager.isEnabled();

        // Android 13+ — проверяем разрешение POST_NOTIFICATIONS
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && isEnabled) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                notificationPrefsManager.setEnabled(false);
                NotificationHelper.cancelNotification(requireContext());
                isEnabled = false;
            }
        }

        // Android 12+ — проверяем точные будильники
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && isEnabled) {
            AlarmManager alarmManager = requireContext().getSystemService(AlarmManager.class);
            if (alarmManager != null && !alarmManager.canScheduleExactAlarms()) {
                notificationPrefsManager.setEnabled(false);
                NotificationHelper.cancelNotification(requireContext());
                isEnabled = false;
                Toast.makeText(requireContext(), "Exact alarm permission revoked. Notifications disabled.", Toast.LENGTH_SHORT).show();
            }
        }

        boolean finalIsEnabled = isEnabled;
        switchNotifications.post(() -> {
            switchNotifications.setOnCheckedChangeListener(null);
            switchNotifications.setChecked(finalIsEnabled);
            restoreNotificationListener();
        });
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private void restoreNotificationListener() {
        switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isWaitingForPermission) return;
            if (isChecked) handleEnableNotifications();
            else disableNotifications();
        });
    }

    private void showAboutDialog() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Natatki")
                .setMessage("Ver 1.0.0\n\nNatatki - notes app here for you to save your thoughts and goals!\n\nMade with great effort and help of internet!")
                .setPositiveButton("Understood", (dialog, which) -> dialog.dismiss())
                .setIcon(R.drawable.ic_info_outline)
                .show();
    }
}