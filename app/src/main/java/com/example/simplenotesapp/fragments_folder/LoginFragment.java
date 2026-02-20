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

import com.example.simplenotesapp.activitys.MainNotesActivity;
import com.example.simplenotesapp.R;
import com.example.simplenotesapp.viewModel.AuthViewModel;
import com.example.simplenotesapp.activity_manager.AuthManager;
import com.example.simplenotesapp.model.User;
import com.example.simplenotesapp.repository.UserRepository;
import com.google.android.material.snackbar.Snackbar;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;

public class LoginFragment extends Fragment {

    EditText etUsername, etPassword, etEmail;
    CheckBox cbRemember;
    Button btnLogin;
    UserRepository userRepository;
    AuthViewModel authViewModel;
    AuthManager authManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup parent, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_login, parent, false);






        return v;

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        authManager = new AuthManager(getActivity());
        etUsername = view.findViewById(R.id.etUsername);
        etPassword = view.findViewById(R.id.etPassword);
        etEmail = view.findViewById(R.id.etEmail);
        cbRemember = view.findViewById(R.id.checkbox_remember_me);
        btnLogin = view.findViewById(R.id.btnLogin);
        userRepository = new UserRepository(requireContext());
        authViewModel = new AuthViewModel(userRepository);

        setupObservers();

        if (authManager.Y_remember_me()) navigateToMain();


        userRepository = new UserRepository(getContext());
        cbRemember.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(@NonNull CompoundButton compoundButton, boolean isChecked) {
                cbRemember.setChecked(isChecked);
            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String username = etUsername.getText().toString().trim();
                String password = etPassword.getText().toString().trim();
                String email = etEmail.getText().toString().toLowerCase();
                authViewModel.login(username, password, email);


            }
        });
    }

        private void setupObservers() {
            // Use viewLifecycleOwner to prevent memory leaks and UID context issues
            authViewModel.getUsernameError().observe(getViewLifecycleOwner(), error -> {
                etUsername.setError(error);
            });

            authViewModel.getPasswordError().observe(getViewLifecycleOwner(), error -> {
                etPassword.setError(error);
            });

            authViewModel.getEmailError().observe(getViewLifecycleOwner(), error -> {
                etEmail.setError(error);
            });

            authViewModel.getGeneralError().observe(getViewLifecycleOwner(), error -> {
                if (error != null) {
                    Toast.makeText(requireContext(), error, Toast.LENGTH_LONG).show();
                }
            });

            authViewModel.getSuccess().observe(requireActivity(), success -> {
                if (Boolean.TRUE.equals(success)) {
                    navigateToMain();
                }
            });
        }



    private void navigateToMain() {
        Intent intent = new Intent(getActivity(), MainNotesActivity.class);

        // Важно: флаги очистки стека, чтобы пользователь не мог
        // вернуться назад на экран регистрации кнопкой "Назад"
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        startActivity(intent);

        // Закрываем текущую AuthActivity, в которой лежит фрагмент
        if (getActivity() != null) {
            getActivity().finish();
        }
    }


}
