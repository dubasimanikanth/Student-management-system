package com.example.sms.model;

/** A single subject result for a student. Marks are between 0 and 100. */
public record Grade(int studentId, String subject, double marks) {

    public Grade {
        if (subject == null || subject.isBlank()) {
            throw new IllegalArgumentException("Subject must not be empty.");
        }
        if (Double.isNaN(marks) || marks < 0 || marks > 100) {
            throw new IllegalArgumentException("Marks must be between 0 and 100.");
        }
        subject = subject.strip();
    }
}
