package com.example.simplenotesapp.fragments_folder;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.example.simplenotesapp.MyApplication;
import com.example.simplenotesapp.R;
import com.example.simplenotesapp.repository.UserRepository;
import com.example.simplenotesapp.viewModel.AuthViewModel;
import com.example.simplenotesapp.activity_manager.AuthManager;
import com.google.android.material.button.MaterialButton;

import org.jetbrains.annotations.Nullable;

public class SignUpFragment extends Fragment {

    // UI элементы
    EditText etUsername, etEmail, etPassword;
    MaterialButton btnSignUp;

    // Репозиторий, ViewModel, менеджер сессии
    UserRepository userRepository;
    AuthManager authManager;
    AuthViewModel authViewModel;

    // ViewPager для переключения между фрагментами (логин/регистрация)
    ViewPager2 viewPager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup parent, @Nullable Bundle savedInstanceState) {
        // Инфлейтим макет фрагмента
        return inflater.inflate(R.layout.fragment_sign_up, parent, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);

        // Инициализация View
        etUsername = v.findViewById(R.id.etUsername);
        etEmail = v.findViewById(R.id.etEmail);
        etPassword = v.findViewById(R.id.etPassword);
        btnSignUp = v.findViewById(R.id.btnSignUp);

        // Получаем ViewPager из активности (чтобы переключаться между фрагментами)
        viewPager = getActivity().findViewById(R.id.viewPager);

        // Создаём репозиторий и ViewModel
        userRepository = ((MyApplication) requireActivity().getApplication()).getUserRepository();
        authViewModel = new AuthViewModel(userRepository);
        authManager = new AuthManager(requireContext()); // может пригодиться, если после регистрации сразу входить

        // TextWatcher для автоматического преобразования вводимого email в нижний регистр
        etEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // ничего не делаем
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // ничего не делаем
            }

            @Override
            public void afterTextChanged(Editable s) {
                String result = s.toString();
                // Если введён не в нижнем регистре — заменяем на нижний
                if (!result.equals(result.toLowerCase())) {
                    String lower = result.toLowerCase();
                    s.replace(0, s.length(), lower);
                }
            }
        });

        // Настраиваем наблюдателей
        setupObservers();

        // Обработчик нажатия кнопки "Зарегистрироваться"
        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Считываем данные
                String password = etPassword.getText().toString().trim();
                String email = etEmail.getText().toString().toLowerCase(); // уже в нижнем, но на всякий случай
                String username = etUsername.getText().toString().trim();

                // Вызываем метод регистрации во ViewModel
                authViewModel.register(username, email, password);
            }
        });
    }

    // Настройка наблюдателей за LiveData из AuthViewModel
    private void setupObservers() {
        // Ошибка имени
        authViewModel.getUsernameError().observe(getViewLifecycleOwner(), error -> {
            etUsername.setError(error);
        });

        // Ошибка пароля
        authViewModel.getPasswordError().observe(getViewLifecycleOwner(), error -> {
            etPassword.setError(error);
        });

        // Ошибка email
        authViewModel.getEmailError().observe(getViewLifecycleOwner(), error -> {
            etEmail.setError(error);
        });

        // Общая ошибка (показываем Toast)
        authViewModel.getGeneralError().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_LONG).show();
            }
        });

        // Наблюдаем за флагом успешной регистрации
        authViewModel.getSuccess().observe(getViewLifecycleOwner(), isSuccess -> {
            if (isSuccess) {
                // Сбрасываем флаг (чтобы повторные срабатывания не вызывали лишних действий)
                authViewModel.resetSuccess();
                // Переключаем ViewPager на фрагмент входа (индекс 0)
                successfullRegistration_Nav_ToLogin();
            }
        });
    }

    // Переключение на фрагмент входа после успешной регистрации
    public void successfullRegistration_Nav_ToLogin() {
        if (viewPager != null) {
            // Переключаем на первый фрагмент (логин) с анимацией
            viewPager.setCurrentItem(0, true);
        } else {
            // Если viewPager по какой-то причине null, пробуем найти его заново
            ViewPager2 vp = getActivity().findViewById(R.id.viewPager);
            if (vp != null) vp.setCurrentItem(0, true);
        }
    }
}