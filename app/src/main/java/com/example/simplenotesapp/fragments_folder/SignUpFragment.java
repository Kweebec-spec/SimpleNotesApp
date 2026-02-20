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

import com.example.simplenotesapp.R;
import com.example.simplenotesapp.repository.UserRepository;
import com.example.simplenotesapp.viewModel.AuthViewModel;
import com.example.simplenotesapp.activity_manager.AuthManager;
import com.example.simplenotesapp.model.User;
import com.google.android.material.button.MaterialButton;

import org.jetbrains.annotations.Nullable;

public class SignUpFragment extends Fragment {

    EditText etUsername, etEmail, etPassword;
    MaterialButton btnSignUp;
    UserRepository userRepository;
    AuthManager authManager;
    AuthViewModel authViewModel;
    ViewPager2 viewPager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup parent, @Nullable Bundle savedInstanceState) {
        // ONLY inflate the view here
        return inflater.inflate(R.layout.fragment_sign_up, parent, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);

        etUsername = v.findViewById(R.id.etUsername);
        etEmail = v.findViewById(R.id.etEmail);
        etPassword = v.findViewById(R.id.etPassword);
        btnSignUp = v.findViewById(R.id.btnSignUp);

        viewPager = getActivity().findViewById(R.id.viewPager);

        userRepository = new UserRepository(requireContext());
        authViewModel = new AuthViewModel(userRepository);
        authManager = new AuthManager(requireContext());



        // real time Low case editor of email text box
        etEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String result = s.toString();
                // Force lowercase in real-time as they type
                if (!result.equals(result.toLowerCase())) {
                    String lower = result.toLowerCase();
                    s.replace(0, s.length(), lower);
                }
            }
        });


        setupObservers(); // Initialize observers once here

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String password = etPassword.getText().toString().trim(); // Fixed: was etUsername
                String email = etEmail.getText().toString().toLowerCase();
                String username = etUsername.getText().toString().trim();

                authViewModel.register(username, email, password);
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

        authViewModel.getSuccess().observe(getViewLifecycleOwner(), success -> {
            if (Boolean.TRUE.equals(success)) {
                successfullRegistration_Nav_ToLogin();
            }
        });
    }

    // swipe from sign up fragment to the login fragment in the auth activity
    public void successfullRegistration_Nav_ToLogin() {
        if (viewPager != null) {
            viewPager.setCurrentItem(0, true); // Added 'true' for smooth scroll
        } else {
            // Fallback: Try to find it again if it was null
            ViewPager2 vp = getActivity().findViewById(R.id.viewPager);
            if (vp != null) vp.setCurrentItem(0, true);
        }
    }
}
