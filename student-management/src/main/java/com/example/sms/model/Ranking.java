package com.example.sms.model;

/** A student together with their average marks and letter grade. */
public record Ranking(Student student, double average, String letter) {
}
