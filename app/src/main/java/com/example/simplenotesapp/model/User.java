package com.example.simplenotesapp.model;

public class User {
    // Поля класса. Они хранят данные пользователя.
    private int id; // Уникальный идентификатор пользователя в базе данных.
    private String username; // Имя пользователя (логин).
    private String password; // Пароль (в реальности храните хэш, но для простоты - строка).
    private String email; // Пароль (в реальности храните хэш, но для простоты - строка).

    // Конструктор без параметров. Нужен для создания пустого объекта.
    public User() {
    }

    // Конструктор с параметрами. Используется для создания объекта с данными.
    public User(String username, String password, String email) {
        this.username = username; // Присваиваем значение полю username.
        this.password = password;
        this.email = email;// Присваиваем значение полю password.
    }

    // Геттеры и сеттеры. Это методы для чтения и записи полей (инкапсуляция).
    public int getId() {
        return id; // Возвращает значение id.
    }

    public void setId(int id) {
        this.id = id; // Устанавливает значение id.
    }

    public String getUsername() {
        return username; // Возвращает имя пользователя.
    }

    public void setUsername(String username) {
        this.username = username; // Устанавливает имя пользователя.
    }

    public String getEmail() {
        return email; // Возвращает email пользователя.
    }

    public void setEmail(String email) {
        this.email = email; // Устанавливает email пользователя.
    }

    public String getPassword() {
        return password; // Возвращает пароль.
    }

    public void setPassword(String password) {
        this.password = password; // Устанавливает пароль.
    }
}