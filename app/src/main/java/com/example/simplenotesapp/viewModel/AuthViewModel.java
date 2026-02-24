package com.example.simplenotesapp.viewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.simplenotesapp.model.User;
import com.example.simplenotesapp.repository.UserRepository;

public class AuthViewModel extends ViewModel {

    // Репозиторий для работы с пользователями (БД)
    private final UserRepository userRepository;

    // LiveData для оповещения UI о результатах операций
    private final MutableLiveData<Boolean> success = new MutableLiveData<>();        // успешная регистрация
    private final MutableLiveData<User> loggedInUser = new MutableLiveData<>();      // вошедший пользователь
    private final MutableLiveData<String> usernameError = new MutableLiveData<>();   // ошибка имени
    private final MutableLiveData<String> emailError = new MutableLiveData<>();      // ошибка email
    private final MutableLiveData<String> passwordError = new MutableLiveData<>();   // ошибка пароля
    private final MutableLiveData<String> generalError = new MutableLiveData<>();    // общая ошибка (например, пользователь уже существует)

    // Конструктор получает репозиторий (внедрение зависимости)
    public AuthViewModel(UserRepository repository) {
        this.userRepository = repository;
    }

    // Геттеры для LiveData (UI подписывается на них)
    public LiveData<User> getLoggedInUser() { return loggedInUser; }
    public LiveData<Boolean> getSuccess() { return success; }
    public LiveData<String> getUsernameError() { return usernameError; }
    public LiveData<String> getEmailError() { return emailError; }
    public LiveData<String> getPasswordError() { return passwordError; }
    public LiveData<String> getGeneralError() { return generalError; }

    // ===== Валидация пароля =====
    private boolean isPasswordValid(String password) {
        // Проверка на пустой пароль
        if (password.isEmpty()) {
            passwordError.setValue("Password is required.");
            return false;
        }

        // Проверка минимальной длины (не менее 8 символов)
        if (password.length() < 8) {
            passwordError.setValue("Password must be at least 8 characters.");
            return false;
        }

        // Регулярное выражение: хотя бы одна буква и хотя бы одна цифра
        String regex = "^(?=.*[a-zA-Z])(?=.*[0-9]).*$";
        if (!password.matches(regex)) {
            passwordError.setValue("Password must contain at least one letter and one number.");
            return false;
        }

        return true;
    }

    // ===== Login Validator =====
    public void login(String username, String email, String password) {
        // Очистить предыдущие ошибки
        clearErrors();

        // Проверка имени
        if (username.isEmpty()) {
            usernameError.setValue("Name required.");
            return;
        }

        // Проверка заполнения email
        if (email.isEmpty()) {
            emailError.setValue("Email is required.");
            return;
        }

        // Проверка заполнения пароля
        if (password.isEmpty()) {
            passwordError.setValue("Password is required.");
            return;
        }

        // Запрос к репозиторию: найти пользователя по email (асинхронно, с колбэком)
        userRepository.getUserByEmail(email, user -> {
            // Если пользователь не найден
            if (user == null) {
                generalError.postValue("User not found, Incorrect email."); // postValue для потока (колбэк выполняется в фоне)
                return;
            }

            // Проверка имени
            if (!user.getUsername().equals(username)) {
                usernameError.setValue("Incorrect name.");
                return;
            }

            // Проверка соответствия пароля
            if (!user.getPassword().equals(password)) {
                passwordError.postValue("Incorrect password.");
                return;
            }

            // Всё правильно — сохраняем найденного пользователя в LiveData (он будет содержать id из БД)
            loggedInUser.postValue(user);
        });
    }

    // ===== Регистрация =====
    public void register(String username, String email, String password) {
        // Очистить ошибки
        clearErrors();

        // Проверка имени
        if (username.isEmpty()) {
            usernameError.setValue("Name required.");
            return;
        }

        // Полная валидация пароля (длина, буквы+цифры)
        if (!isPasswordValid(password)) return;

        // Проверка формата email (используем встроенный утилитный класс Android)
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailError.setValue("Invalid email.");
            return;
        }

        // Проверяем, не занят ли уже такой email (асинхронно)
        userRepository.isUserExists(email, exists -> {
            if (exists) {
                // Если уже есть пользователь с таким email — сообщаем об ошибке
                generalError.postValue("User already exists.");
                return;
            }

            // Создаём объект User (без id, т.к. он сгенерируется БД)
            User newUser = new User(username, password, email);

            // Добавляем пользователя в БД через репозиторий (метод с колбэком, вернёт пользователя с id)
            userRepository.addUser(newUser, user -> {
                if (user != null) {
                    // Если добавление прошло успешно, сохраняем пользователя с id в loggedInUser
                    loggedInUser.postValue(user);
                    // Сигнализируем об успешной регистрации
                    success.postValue(true);
                } else {
                    // Если произошла ошибка при вставке (например, проблемы с БД)
                    generalError.postValue("Registration failed.");
                }
            });
        });
    }

    // Вспомогательный метод: очищает все поля ошибок
    private void clearErrors() {
        usernameError.setValue(null);
        emailError.setValue(null);
        passwordError.setValue(null);
        generalError.setValue(null);
    }

    // Сброс флага success (чтобы при повторном нажатии кнопки регистрации не срабатывал старый успех)
    public void resetSuccess() {
        success.postValue(false);
    }
}