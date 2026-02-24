package com.example.simplenotesapp.fragments_folder;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.simplenotesapp.MyApplication;
import com.example.simplenotesapp.activitys.MainNotesActivity;
import com.example.simplenotesapp.R;
import com.example.simplenotesapp.viewModel.AuthViewModel;
import com.example.simplenotesapp.activity_manager.AuthManager;
import com.example.simplenotesapp.model.User;
import com.example.simplenotesapp.repository.UserRepository;
import com.example.simplenotesapp.viewModel.AuthViewModelFactory;

public class LoginFragment extends Fragment {

    // UI элементы
    EditText etUsername, etPassword, etEmail;
    CheckBox cbRemember;
    Button btnLogin;

    // Репозиторий, ViewModel и менеджер для сохранения состояния входа
    UserRepository userRepository;
    AuthViewModel authViewModel;
    AuthManager authManager;

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
        authManager = new AuthManager(getActivity());

        // Находим View по id
        etUsername = view.findViewById(R.id.etUsername);
        etPassword = view.findViewById(R.id.etPassword);
        etEmail = view.findViewById(R.id.etEmail);
        cbRemember = view.findViewById(R.id.checkbox_remember_me);
        btnLogin = view.findViewById(R.id.btnLogin);

        // Получаем репозиторий из MyApplication
        userRepository = ((MyApplication) requireActivity().getApplication()).getUserRepository();

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
            etUsername.setError(error); // показываем ошибку под полем
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

        // Наблюдаем за успешным входом (когда loggedInUser получает значение)
        authViewModel.getLoggedInUser().observe(getViewLifecycleOwner(), user -> {
            // Проверяем, что пользователь не null и у него есть id (корректный)
            if (user != null && user.getId() != 0) {
                // Сбрасываем флаг success (на всякий случай)
                authViewModel.resetSuccess();

                // Сохраняем данные входа через AuthManager (учитываем состояние чекбокса)
                authManager.login(user, cbRemember.isChecked());

                // Переходим к главной активности со списком заметок
                navigateToMain();
            }
        });
    }

    // Переход к MainNotesActivity с очисткой стека
    private void navigateToMain() {
        Intent intent = new Intent(getActivity(), MainNotesActivity.class);

        // Флаги гарантируют, что после перехода нельзя вернуться назад к экрану входа
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        startActivity(intent);

        // Закрываем текущую Activity (AuthActivity), в которой находится фрагмент
        if (getActivity() != null) {
            getActivity().finish();
        }
    }
}