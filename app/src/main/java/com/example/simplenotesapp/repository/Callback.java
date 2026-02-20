package com.example.simplenotesapp.repository;

public interface Callback<T> {
    void onComplete(T result);
}
