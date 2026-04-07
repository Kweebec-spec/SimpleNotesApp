package com.example.simplenotesapp.fragments_folder;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.example.simplenotesapp.MyApplication;
import com.example.simplenotesapp.R;
import com.example.simplenotesapp.activity_manager.AuthManager;
import com.example.simplenotesapp.model.Note;
import com.example.simplenotesapp.model.ThemeModel;
import com.example.simplenotesapp.viewModel.NotesViewModel;
import com.example.simplenotesapp.viewModel.NotesViewModelFactory;
import com.example.simplenotesapp.viewModel.ThemeViewModel;
import com.example.simplenotesapp.viewModel.ThemeViewModelFactory;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Фрагмент для просмотра и редактирования заметки.
 */
public class NoteFragment extends Fragment {

    // UI элементы
    private ImageView go_out_of_here;
    private ImageView bottom_sheet_menu;
    private View themeColorCircle;
    private EditText titleEditText;
    private EditText contentEditText;
    private FloatingActionButton btnSpeak;

    // Данные
    private long currentNoteId = -1;
    private Note currentNote;                   // Модель заметки
    private List<ThemeModel> allThemes = new ArrayList<>(); // Список тем (модели)

    // ViewModel
    private NotesViewModel notesViewModel;
    private ThemeViewModel themeViewModel;
    private AuthManager authManager;

    // Флаги для автосохранения
    private boolean isUpdating = false;
    private boolean isTextChanging = false;
    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable titleSaveRunnable;
    private Runnable contentSaveRunnable;

    // Голосовой ввод
    private SpeechRecognizer speechRecognizer;
    private Intent speechRecognizerIntent;
    private boolean isListening = false;
    private static final int PERMISSION_REQUEST_RECORD_AUDIO = 1002;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.note_fragment_screen, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            currentNoteId = getArguments().getLong("noteId", -1);
        }

        initViews(view);
        setupViewModels();
        observeThemes();
        setupListeners();
        loadNoteData();

    }

    private void initViews(View view) {
        bottom_sheet_menu = view.findViewById(R.id.bottom_sheet_menu);
        go_out_of_here = view.findViewById(R.id.go_out_of_here);
        titleEditText = view.findViewById(R.id.title);
        contentEditText = view.findViewById(R.id.content);
        themeColorCircle = view.findViewById(R.id.theme_color);
        btnSpeak = view.findViewById(R.id.btnSpeak);


    }

    private void setupViewModels() {
        authManager = new AuthManager(requireContext());
        MyApplication app = (MyApplication) requireActivity().getApplication();

        Long userIdLong = authManager.getId();
        long userId = (userIdLong != null) ? userIdLong : -1L;

        NotesViewModelFactory notesFactory = new NotesViewModelFactory(app.getNotesRepository(), userId);
        notesViewModel = new ViewModelProvider(this, notesFactory).get(NotesViewModel.class);

        ThemeViewModelFactory themeFactory = new ThemeViewModelFactory(app.getThemeRepository(), userId);
        themeViewModel = new ViewModelProvider(this, themeFactory).get(ThemeViewModel.class);
    }

    private void observeThemes() {
        // Подписываемся на список тем (LiveData<List<ThemeModel>>)
        themeViewModel.getThemes().observe(getViewLifecycleOwner(), themes -> {
            allThemes.clear();
            if (themes != null) allThemes.addAll(themes);
            updateThemeColor(); // Обновляем цвет кружка
        });
    }

    private void loadNoteData() {
        if (currentNoteId != -1) {
            notesViewModel.setCurrentNoteId(currentNoteId);
        }

        // Наблюдаем за текущей заметкой (LiveData<Note>)
        // Примечание: нужно, чтобы NotesViewModel предоставлял LiveData<Note>, а не LiveData<NoteEntity>
        // Для этого в NotesViewModel добавим преобразование.
        notesViewModel.getCurrentNote().observe(getViewLifecycleOwner(), note -> {
            if (note != null) {
                // Проверяем, изменился ли текст, чтобы избежать циклического обновления
                boolean titleChanged = !note.getTitle().equals(titleEditText.getText().toString());
                boolean contentChanged = !note.getContent().equals(contentEditText.getText().toString());

                if (titleChanged || contentChanged) {
                    isUpdating = true;
                    isTextChanging = true;

                    if (titleChanged) {
                        titleEditText.setText(note.getTitle() != null ? note.getTitle() : "");
                    }
                    if (contentChanged) {
                        contentEditText.setText(note.getContent() != null ? note.getContent() : "");
                    }

                    isTextChanging = false;
                    isUpdating = false;
                }

                currentNote = note;
                updateThemeColor();
            }
        });
    }

    private void updateThemeColor() {
        if (currentNote != null && themeColorCircle != null) {
            // Цвет заметки хранится в currentNote.getColor()
            String color = currentNote.getColor();
            if (color != null && !color.isEmpty()) {
                setCircleColor(color);
            } else {
                setCircleColor("#00000000");
            }
        }
    }

    private void setCircleColor(String colorHex) {
        Drawable backgroundDrawable = themeColorCircle.getBackground();
        if (backgroundDrawable instanceof GradientDrawable) {
            GradientDrawable shape = (GradientDrawable) backgroundDrawable.mutate();
            try {
                int color = android.graphics.Color.parseColor(colorHex);
                shape.setColor(color);
            } catch (Exception e) {
                shape.setColor(android.graphics.Color.parseColor("#00000000"));
            }
        }
    }

    private void showThemeSelectionDialog() {
        if (allThemes.isEmpty()) {
            Toast.makeText(requireContext(), "Firstly create new theme!", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] themeNames = new String[allThemes.size() + 1];
        themeNames[0] = "No Theme";
        for (int i = 0; i < allThemes.size(); i++) {
            themeNames[i + 1] = allThemes.get(i).getName();
        }

        new AlertDialog.Builder(requireContext())
                .setTitle("Choose theme")
                .setItems(themeNames, (dialog, which) -> {
                    if (which == 0) {
                        currentNote.setThemeId(-1);
                        currentNote.setColor(null);
                        setCircleColor("#D3D3D3");
                    } else {
                        ThemeModel selected = allThemes.get(which - 1);
                        currentNote.setThemeId(selected.getId());
                        currentNote.setColor(selected.getColor());
                        setCircleColor(selected.getColor());
                    }
                    notesViewModel.updateNote(currentNote);
                })
                .show();
    }

    // ========== ГОЛОСОВОЙ ВВОД ==========
    private void initSpeech() {
        if (!SpeechRecognizer.isRecognitionAvailable(requireContext())) {
            btnSpeak.setEnabled(false);
            btnSpeak.setImageResource(R.drawable.ic_mic_off);
            btnSpeak.setOnClickListener(v ->
                    Toast.makeText(requireContext(), "Speech recognition not available", Toast.LENGTH_SHORT).show());
            return;
        }

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(requireContext());
        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) {
                isListening = true;
                btnSpeak.setImageResource(R.drawable.ic_mic_on);
                Toast.makeText(requireContext(), "Speak now...", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onBeginningOfSpeech() {}

            @Override
            public void onRmsChanged(float rmsdB) {}

            @Override
            public void onBufferReceived(byte[] buffer) {}

            @Override
            public void onEndOfSpeech() {}

            @Override
            public void onError(int error) {
                stopListening();
                String errorMsg = getSpeechErrorString(error);
                Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResults(Bundle results) {
                List<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null && !matches.isEmpty()) {
                    String spoken = matches.get(0);
                    int cursorPos = contentEditText.getSelectionStart();
                    String currentText = contentEditText.getText().toString();
                    String newText = currentText.substring(0, cursorPos) + spoken + currentText.substring(cursorPos);
                    contentEditText.setText(newText);
                    contentEditText.setSelection(cursorPos + spoken.length());

                    if (currentNote != null) {
                        currentNote.setContent(contentEditText.getText().toString());
                        notesViewModel.updateNote(currentNote);
                    }
                }
                stopListening();
            }

            @Override
            public void onPartialResults(Bundle partialResults) {}

            @Override
            public void onEvent(int eventType, Bundle params) {}
        });

        speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);

        btnSpeak.setOnClickListener(v -> {
            if (isListening) {
                speechRecognizer.stopListening();
                stopListening();
            } else {
                startListeningWithPermissionCheck();
            }
        });
    }

    private String getSpeechErrorString(int error) {
        switch (error) {
            case SpeechRecognizer.ERROR_AUDIO: return "Audio recording error";
            case SpeechRecognizer.ERROR_CLIENT: return "Client error";
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS: return "Microphone permission required";
            case SpeechRecognizer.ERROR_NETWORK: return "Network error";
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT: return "Network timeout";
            case SpeechRecognizer.ERROR_NO_MATCH: return "No speech recognized";
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY: return "Recognition service busy";
            case SpeechRecognizer.ERROR_SERVER: return "Server error";
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT: return "No speech input";
            default: return "Error: " + error;
        }
    }

    private void startListeningWithPermissionCheck() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO)
                    != PackageManager.PERMISSION_GRANTED) {
                if (shouldShowRequestPermissionRationale(Manifest.permission.RECORD_AUDIO)) {
                    new MaterialAlertDialogBuilder(requireContext())
                            .setTitle("Microphone permission")
                            .setMessage("This app needs microphone access to convert your speech to text.")
                            .setPositiveButton("Grant", (dialog, which) ->
                                    requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO},
                                            PERMISSION_REQUEST_RECORD_AUDIO))
                            .setNegativeButton("Cancel", (dialog, which) ->
                                    Toast.makeText(requireContext(),
                                            "Microphone permission required for speech input",
                                            Toast.LENGTH_LONG).show())
                            .show();
                } else {
                    requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO},
                            PERMISSION_REQUEST_RECORD_AUDIO);
                }
                return;
            }
        }
        startListening();
    }

    private void startListening() {
        if (speechRecognizer != null) {
            speechRecognizer.startListening(speechRecognizerIntent);
        }
    }

    private void stopListening() {
        if (speechRecognizer != null && isListening) {
            speechRecognizer.stopListening();
        }
        isListening = false;
        btnSpeak.setImageResource(R.drawable.ic_mic_off);
    }
    // ========== КОНЕЦ ГОЛОСОВОГО ВВОДА ==========

    private void setupListeners() {
        setupTitleTextWatcher();
        setupContentTextWatcherWithReplacement();
        setupFocusChangeListeners();
        setupClickListeners();
        initSpeech();
        themeColorCircle.setOnClickListener(v -> showThemeSelectionDialog());
    }

    private void setupTitleTextWatcher() {
        titleEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (currentNote != null && !isUpdating && !isTextChanging) {
                    if (titleSaveRunnable != null) handler.removeCallbacks(titleSaveRunnable);
                    titleSaveRunnable = () -> {
                        currentNote.setTitle(s.toString());
                        notesViewModel.updateNote(currentNote);
                    };
                    handler.postDelayed(titleSaveRunnable, 1000);
                }
            }
        });
    }

    private void setupContentTextWatcherWithReplacement() {
        contentEditText.addTextChangedListener(new TextWatcher() {
            private boolean isReplacing = false;
            private int cursorPos = 0;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if (!isReplacing && !isTextChanging) {
                    cursorPos = contentEditText.getSelectionStart();
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (isReplacing || currentNote == null || isUpdating || isTextChanging) return;

                String text = s.toString();

                // Замена [] на ☐ (пустой чекбокс)
                if (text.contains("[]")) {
                    isReplacing = true;
                    String newText = text.replace("[]", "☐");
                    int newPosition = cursorPos + ("☐".length() - "[]".length());
                    newPosition = Math.max(0, Math.min(newPosition, newText.length()));
                    contentEditText.setText(newText);
                    contentEditText.setSelection(newPosition);
                    isReplacing = false;
                    return;
                }

                // Замена [V] или [v] на ☑ (заполненный чекбокс)
                if (text.contains("[V]") || text.contains("[v]")) {
                    isReplacing = true;
                    String newText = text.replace("[V]", "☑").replace("[v]", "☑");
                    int newPosition = cursorPos + ("☑".length() - 3);
                    newPosition = Math.max(0, Math.min(newPosition, newText.length()));
                    contentEditText.setText(newText);
                    contentEditText.setSelection(newPosition);
                    isReplacing = false;
                    return;
                }

                // Обычное автосохранение
                if (!isReplacing && !isUpdating && !isTextChanging) {
                    if (contentSaveRunnable != null) handler.removeCallbacks(contentSaveRunnable);
                    contentSaveRunnable = () -> {
                        int currentPosition = contentEditText.getSelectionStart();
                        currentNote.setContent(contentEditText.getText().toString());
                        notesViewModel.updateNote(currentNote);
                        if (currentPosition > 0) {
                            int finalPosition = currentPosition;
                            contentEditText.post(() -> {
                                if (finalPosition <= contentEditText.getText().length()) {
                                    contentEditText.setSelection(finalPosition);
                                }
                            });
                        }
                    };
                    handler.postDelayed(contentSaveRunnable, 1000);
                }
            }
        });
    }

    private void setupFocusChangeListeners() {
        titleEditText.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus && currentNote != null && !isTextChanging) {
                currentNote.setTitle(titleEditText.getText().toString());
                notesViewModel.updateNote(currentNote);
            }
        });

        contentEditText.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus && currentNote != null && !isTextChanging) {
                currentNote.setContent(contentEditText.getText().toString());
                notesViewModel.updateNote(currentNote);
            }
        });
    }

    private void setupClickListeners() {
        bottom_sheet_menu.setOnClickListener(v -> {
            BottomSheetMenu bottomSheet = new BottomSheetMenu();
            bottomSheet.show(getParentFragmentManager(), "BottomSheetMenu");
        });

        go_out_of_here.setOnClickListener(v -> Navigation.findNavController(requireView()).navigateUp());
    }

    @Override
    public void onPause() {
        super.onPause();
        saveNote();
    }

    private void saveNote() {
        if (currentNote != null) {
            if (titleSaveRunnable != null) handler.removeCallbacks(titleSaveRunnable);
            if (contentSaveRunnable != null) handler.removeCallbacks(contentSaveRunnable);
            currentNote.setTitle(titleEditText.getText().toString());
            currentNote.setContent(contentEditText.getText().toString());
            notesViewModel.updateNote(currentNote);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_RECORD_AUDIO) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startListening();
            } else {
                Toast.makeText(requireContext(),
                        "Microphone permission is required for speech input.",
                        Toast.LENGTH_LONG).show();
            }
        }
    }
}