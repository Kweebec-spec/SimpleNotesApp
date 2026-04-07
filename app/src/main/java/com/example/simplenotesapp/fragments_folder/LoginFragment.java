package com.example.simplenotesapp.fragments_folder;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;

import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.simplenotesapp.MyApplication;
import com.example.simplenotesapp.activitys.MainActivity;
import com.example.simplenotesapp.R;
import com.example.simplenotesapp.utils.KeyboardUtils;
import com.example.simplenotesapp.viewModel.AuthViewModel;
import com.example.simplenotesapp.activity_manager.AuthManager;
import com.example.simplenotesapp.model.User;
import com.example.simplenotesapp.repository.UserRepository;
import com.example.simplenotesapp.viewModel.AuthViewModelFactory;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class LoginFragment extends Fragment {

    private TextInputEditText etUsername, etPassword, etEmail;
    private TextInputLayout etUsernameL, etEmailL, etPasswordL;
    private CheckBox cbRemember;
    private Button btnLogin;

    // Репозиторий, ViewModel и менеджер для сохранения состояния входа
    private UserRepository userRepository;
    private AuthViewModel authViewModel;
    private AuthManager authManager;
    private ScrollView scrollView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup parent, @Nullable Bundle savedInstanceState) {
        // Инфлейтим (создаём) макет фрагмента
        View v = inflater.inflate(R.layout.fragment_login, parent, false);
        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        // Инициализация AuthManager (для сохранения данных сессии)
        MyApplication app = (MyApplication) requireActivity().getApplication();

        authManager = app.getAuthManager();
        scrollView = view.findViewById(R.id.scrollView2);
        KeyboardUtils.setupScrollViewKeyboardDismiss(scrollView, requireActivity());



        // Находим View по id
        etUsernameL = view.findViewById(R.id.etUsernameL);
        etPasswordL = view.findViewById(R.id.etPasswordL);
        etEmailL = view.findViewById(R.id.etEmailL);
        etUsername = view.findViewById(R.id.etUsername);
        etPassword = view.findViewById(R.id.etPassword);
        etEmail = view.findViewById(R.id.etEmail);
        cbRemember = view.findViewById(R.id.checkbox_remember_me);
        btnLogin = view.findViewById(R.id.btnLogin);

        // Получаем репозиторий из MyApplication
        userRepository = app.getUserRepository();

// Инициализируем ViewModel
// В будущем лучше делать так (потребуется ViewModelFactory, так как есть конструктор)
        authViewModel = new ViewModelProvider(this, new AuthViewModelFactory(userRepository)).get(AuthViewModel.class);


        // Настраиваем наблюдателей (observers) за изменениями в LiveData
        setupObservers();

        // Обработчик нажатия кнопки "Войти"
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Считываем введённые данные
                String username = etUsername.getText().toString().trim(); // имя не обязательно для входа, но можно использовать
                String password = etPassword.getText().toString().trim();
                String email = etEmail.getText().toString().toLowerCase(); // email приводим к нижнему регистру

                // Вызываем метод login у ViewModel
                authViewModel.login(username, email, password);
            }
        });
    }

    // Настройка наблюдателей LiveData
    private void setupObservers() {
        // Наблюдаем за ошибками имени (хотя в логине имя не используется, но LiveData может быть обновлена)
        authViewModel.getUsernameError().observe(getViewLifecycleOwner(), error -> {
            etUsernameL.setError(error); // показываем ошибку под полем
            etUsernameL.setErrorEnabled(error != null);
        });

        // Ошибка пароля
        authViewModel.getPasswordError().observe(getViewLifecycleOwner(), error -> {
            etPasswordL.setError(error);
            etPasswordL.setErrorEnabled(error != null);
        });

        // Ошибка email
        authViewModel.getEmailError().observe(getViewLifecycleOwner(), error -> {
            etEmailL.setError(error);
            etEmailL.setErrorEnabled(error != null);

        });

        // Общая ошибка (показываем Toast)
        authViewModel.getGeneralError().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_LONG).show();
            }
        });

        // Наблюдаем за успешным входом (когда loggedInUser получает значение)
        authViewModel.getLoggedInUser().observe(getViewLifecycleOwner(), user -> {
            // Проверяем, что пользователь не null и у него есть id (корректный)
            if (user != null && user.getId() != 0) {
                // Сбрасываем флаг success (на всякий случай)
                authViewModel.resetSuccess();

                // Сохраняем данные входа через AuthManager (учитываем состояние чекбокса)
                authManager.login(user.getId(), user.getEmail(), user.getUsername(), cbRemember.isChecked());

                // Переходим к главной активности со списком заметок
                navigateToMain();
            }
        });
    }

    // В методе navigateToMain():
    private void navigateToMain() {
        Intent intent = new Intent(getActivity(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        if (getActivity() != null) {
            getActivity().finish();
        }
    }
}