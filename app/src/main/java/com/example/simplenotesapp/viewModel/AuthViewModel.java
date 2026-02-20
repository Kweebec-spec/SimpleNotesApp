package com.example.simplenotesapp.viewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.simplenotesapp.model.User;
import com.example.simplenotesapp.repository.Callback;
import com.example.simplenotesapp.repository.UserRepository;

public class AuthViewModel extends ViewModel {

    private final UserRepository userRepository;

    private final MutableLiveData<Boolean> success = new MutableLiveData<>();

    private final MutableLiveData<String> usernameError = new MutableLiveData<>();
    private final MutableLiveData<String> emailError = new MutableLiveData<>();
    private final MutableLiveData<String> passwordError = new MutableLiveData<>();
    private final MutableLiveData<String> generalError = new MutableLiveData<>();


    public AuthViewModel(UserRepository repository) {
        this.userRepository = repository;
    }

    // ===== LiveData getters =====

    public LiveData<Boolean> getSuccess() { return success; }
    public LiveData<String> getUsernameError() { return usernameError; }
    public LiveData<String> getEmailError() { return emailError; }
    public LiveData<String> getPasswordError() { return passwordError; }
    public LiveData<String> getGeneralError() { return generalError; }

    // ===== Password validation =====

    private boolean isPasswordValid(String password) {

        if (password == null || password.isEmpty()) {
            passwordError.setValue("Password is required");
            return false;
        }

        if (password.length() < 8) {
            passwordError.setValue("Password must be at least 8 characters");
            return false;
        }

        String regex = "^(?=.*[a-zA-Z])(?=.*[0-9]).*$";
        if (!password.matches(regex)) {
            passwordError.setValue("Password must contain at least one letter and one number");
            return false;
        }

        return true;
    }

    // ===== LOGIN =====

    public void login(String username, String email, String password) {

        clearErrors();
        success.setValue(false);

        if (username.isEmpty()) {
            usernameError.setValue("Username is required");
            return;
        }

        if (email.isEmpty()) {
            emailError.setValue("Email is required");
            return;
        }

        if (password.isEmpty()) {
            passwordError.setValue("Password is required");
            return;
        }

        userRepository.getUserByEmail(email, user -> {

            if (user == null) {
                generalError.postValue("User not found");
                return;
            }

            if (!user.getPassword().equals(password)) {
                passwordError.postValue("Incorrect password");
                return;
            }

            success.postValue(true);
        });
    }

    // ===== REGISTER =====

    public void register(String username, String email, String password) {

        clearErrors();
        success.setValue(false);

        if (username.isEmpty()) {
            usernameError.setValue("Name required");
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailError.setValue("Invalid email");
            return;
        }

        if (!isPasswordValid(password)) return;

        userRepository.isUserExists(email, exists -> {

            if (exists) {
                generalError.postValue("User already exists");
                return;
            }

            userRepository.addUser(new User(username, email, password));
            success.postValue(true);
        });
    }

    private void clearErrors() {
        usernameError.setValue(null);
        emailError.setValue(null);
        passwordError.setValue(null);
        generalError.setValue(null);
    }
}
