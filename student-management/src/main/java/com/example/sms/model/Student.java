package com.example.sms.model;

import java.util.regex.Pattern;

/** Immutable student record with basic validation. */
public record Student(int id, String name, String email, String department) {

    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    public Student {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name must not be empty.");
        }
        if (email == null || !EMAIL.matcher(email.strip()).matches()) {
            throw new IllegalArgumentException("Invalid email address.");
        }
        if (department == null || department.isBlank()) {
            throw new IllegalArgumentException("Department must not be empty.");
        }
        name = name.strip();
        email = email.strip();
        department = department.strip();
    }

    @Override
    public String toString() {
        return String.format("#%-4d %-22s %-28s %s", id, name, email, department);
    }
}
