package com.example.expireddatetracker;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

public class LoginActivityTest {

    //should not throw exception when pass valid email to isEmailValid function
    @Test
    public void shouldNotThrowExceptionWhenSetValidEmail() {
        String email= "abc@gmail.com";
        assertThat(LoginActivity.isEmailValid(email),is(true));
    }

    //should throw exception when pass invalid email to isEmailValid function
    @Test
    public void shouldThrowExceptionWhenSetInvalidEmail() {
        String email= "abc@.com";
        assertThat(LoginActivity.isEmailValid(email),is(false));
    }

    //should throw exception when pass empty email to isEmailValid function
    @Test
    public void shouldThrowExceptionWhenSetEmptyEmail() {
        String email= "    ";
        assertThat(LoginActivity.isEmailValid(email),is(false));
    }

    //should throw exception when pass null email to isEmailValid function
    @Test
    public void shouldThrowExceptionWhenSetNullEmail() {
        String email = null;
        assertThat(LoginActivity.isEmailValid(email),is(false));
    }

    //should not throw exception when pass valid password to isPasswordValid function
    @Test
    public void shouldNotThrowExceptionWhenSetValidPassword() {
        String password= "xinyi123";
        assertThat(LoginActivity.isPasswordValid(password),is(true));
    }

    //should throw exception when pass invalid password to isPasswordValid function
    @Test
    public void shouldThrowExceptionWhenSetPassword() {
        String password= "abc";
        assertThat(LoginActivity.isPasswordValid(password),is(false));
    }

    //should throw exception when pass empty password to isPasswordValid function
    @Test
    public void shouldThrowExceptionWhenSetEmptyPassword() {
        String password= "      ";
        assertThat(LoginActivity.isPasswordValid(password),is(false));
    }

    //should throw exception when pass null password to isPasswordValid function
    @Test
    public void shouldThrowExceptionWhenSetNullPassword() {
        String password = null;
        assertThat(LoginActivity.isEmailValid(password),is(false));
    }
}