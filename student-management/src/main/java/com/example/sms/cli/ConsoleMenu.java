package com.example.sms.cli;

import com.example.sms.model.Grade;
import com.example.sms.model.Ranking;
import com.example.sms.model.Student;
import com.example.sms.service.GradeCalculator;
import com.example.sms.service.StudentService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.List;
import java.util.OptionalDouble;

public class ConsoleMenu {

    private final StudentService service;
    private final BufferedReader in;
    private final PrintStream out;

    public ConsoleMenu(StudentService service, InputStream in, PrintStream out) {
        this.service = service;
        this.in = new BufferedReader(new InputStreamReader(in));
        this.out = out;
    }

    public void run() throws IOException {
        out.println("=== Student Management System ===");
        while (true) {
            printMenu();
            String choice = in.readLine();
            if (choice == null) break;
            choice = choice.trim();
            if (choice.equals("0")) break;
            try {
                handle(choice);
            } catch (RuntimeException e) {
                out.println("Error: " + e.getMessage());
            }
        }
        out.println("Goodbye!");
    }

    private void printMenu() {
        out.println();
        out.println("1) Add student      5) Update student");
        out.println("2) List students    6) Delete student");
        out.println("3) Search           7) Add / update grade");
        out.println("4) Student report   8) Rankings");
        out.println("0) Exit");
        out.print("Choose: ");
        out.flush();
    }

    private void handle(String choice) {
        switch (choice) {
            case "1" -> addStudent();
            case "2" -> printStudents(service.listAll());
            case "3" -> printStudents(service.search(ask("Keyword")));
            case "4" -> report();
            case "5" -> updateStudent();
            case "6" -> deleteStudent();
            case "7" -> addGrade();
            case "8" -> rankings();
            default -> out.println("Unknown option.");
        }
    }

    private void addStudent() {
        String name = ask("Name");
        String email = ask("Email");
        String department = ask("Department");
        out.println("Added: " + service.addStudent(name, email, department));
    }

    private void updateStudent() {
        Student current = service.findById(askInt("Student id"));
        out.println("Leave a field blank to keep its current value.");
        String name = askOptional("Name [" + current.name() + "]", current.name());
        String email = askOptional("Email [" + current.email() + "]", current.email());
        String department = askOptional("Department [" + current.department() + "]", current.department());
        out.println("Updated: " + service.updateStudent(current.id(), name, email, department));
    }

    private void deleteStudent() {
        int id = askInt("Student id");
        Student student = service.findById(id);
        String answer = ask("Delete " + student.name() + " and all grades? (y/n)");
        if (answer.equalsIgnoreCase("y")) {
            service.deleteStudent(id);
            out.println("Deleted.");
        } else {
            out.println("Cancelled.");
        }
    }

    private void addGrade() {
        int id = askInt("Student id");
        Student student = service.findById(id);
        String subject = ask("Subject");
        double marks = askDouble("Marks (0-100)");
        Grade grade = service.addGrade(id, subject, marks);
        out.printf("Saved %s: %.1f for %s%n", grade.subject(), grade.marks(), student.name());
    }

    private void report() {
        Student student = service.findById(askInt("Student id"));
        out.println(student);
        List<Grade> grades = service.gradesOf(student.id());
        if (grades.isEmpty()) {
            out.println("  (no grades yet)");
            return;
        }
        for (Grade g : grades) {
            out.printf("  %-20s %6.1f%n", g.subject(), g.marks());
        }
        OptionalDouble avg = service.average(student.id());
        out.printf("  Average: %.2f  Grade: %s%n", avg.getAsDouble(), GradeCalculator.letter(avg.getAsDouble()));
    }

    private void rankings() {
        List<Ranking> rankings = service.rankings();
        if (rankings.isEmpty()) {
            out.println("(no graded students yet)");
            return;
        }
        int position = 1;
        for (Ranking r : rankings) {
            out.printf("%2d. %-22s %6.2f  %s%n", position++, r.student().name(), r.average(), r.letter());
        }
    }

    private void printStudents(List<Student> students) {
        if (students.isEmpty()) {
            out.println("(no students)");
            return;
        }
        students.forEach(out::println);
    }

    // ---------- input helpers ----------

    private String ask(String label) {
        out.print(label + ": ");
        out.flush();
        try {
            String line = in.readLine();
            if (line == null) throw new IllegalStateException("Input ended.");
            return line.trim();
        } catch (IOException e) {
            throw new IllegalStateException("Could not read input.", e);
        }
    }

    private String askOptional(String label, String fallback) {
        String value = ask(label);
        return value.isEmpty() ? fallback : value;
    }

    private int askInt(String label) {
        try {
            return Integer.parseInt(ask(label));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Please enter a whole number.");
        }
    }

    private double askDouble(String label) {
        try {
            return Double.parseDouble(ask(label));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Please enter a number.");
        }
    }
}
