package com.example.sms.service;

public final class GradeCalculator {

    private GradeCalculator() {
    }

    public static String letter(double average) {
        if (average >= 90) return "A";
        if (average >= 80) return "B";
        if (average >= 70) return "C";
        if (average >= 60) return "D";
        return "F";
    }
}
