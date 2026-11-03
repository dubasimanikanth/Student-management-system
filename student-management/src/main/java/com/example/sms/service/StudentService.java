package com.example.sms.service;

import com.example.sms.model.Grade;
import com.example.sms.model.Ranking;
import com.example.sms.model.Student;
import com.example.sms.repository.GradeRepository;
import com.example.sms.repository.StudentRepository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.OptionalDouble;

public class StudentService {

    private final StudentRepository studentRepository;
    private final GradeRepository gradeRepository;
    private final List<Student> students;
    private final List<Grade> grades;

    public StudentService(StudentRepository studentRepository, GradeRepository gradeRepository) {
        this.studentRepository = studentRepository;
        this.gradeRepository = gradeRepository;
        this.students = new ArrayList<>(studentRepository.findAll());
        this.grades = new ArrayList<>(gradeRepository.findAll());
    }

    // ---------- students ----------

    public Student addStudent(String name, String email, String department) {
        int nextId = students.stream().mapToInt(Student::id).max().orElse(0) + 1;
        Student student = new Student(nextId, name, email, department);
        ensureEmailAvailable(student.email(), -1);
        students.add(student);
        saveStudents();
        return student;
    }

    public Student updateStudent(int id, String name, String email, String department) {
        int index = indexOf(id);
        Student updated = new Student(id, name, email, department);
        ensureEmailAvailable(updated.email(), id);
        students.set(index, updated);
        saveStudents();
        return updated;
    }

    public void deleteStudent(int id) {
        students.remove(indexOf(id));
        grades.removeIf(g -> g.studentId() == id);
        saveStudents();
        saveGrades();
    }

    public Student findById(int id) {
        return students.get(indexOf(id));
    }

    public List<Student> listAll() {
        return students.stream()
                .sorted(Comparator.comparing(Student::name, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    public List<Student> search(String keyword) {
        String needle = keyword.toLowerCase(Locale.ROOT);
        return students.stream()
                .filter(s -> s.name().toLowerCase(Locale.ROOT).contains(needle)
                        || s.email().toLowerCase(Locale.ROOT).contains(needle)
                        || s.department().toLowerCase(Locale.ROOT).contains(needle))
                .sorted(Comparator.comparing(Student::name, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    // ---------- grades ----------

    /** Adds a grade, replacing any existing grade for the same subject. */
    public Grade addGrade(int studentId, String subject, double marks) {
        indexOf(studentId); // throws if the student does not exist
        Grade grade = new Grade(studentId, subject, marks);
        grades.removeIf(g -> g.studentId() == studentId && g.subject().equalsIgnoreCase(grade.subject()));
        grades.add(grade);
        saveGrades();
        return grade;
    }

    public List<Grade> gradesOf(int studentId) {
        return grades.stream()
                .filter(g -> g.studentId() == studentId)
                .sorted(Comparator.comparing(Grade::subject, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    public OptionalDouble average(int studentId) {
        return grades.stream()
                .filter(g -> g.studentId() == studentId)
                .mapToDouble(Grade::marks)
                .average();
    }

    /** Students that have at least one grade, best average first. */
    public List<Ranking> rankings() {
        List<Ranking> result = new ArrayList<>();
        for (Student s : students) {
            OptionalDouble avg = average(s.id());
            if (avg.isPresent()) {
                result.add(new Ranking(s, avg.getAsDouble(), GradeCalculator.letter(avg.getAsDouble())));
            }
        }
        result.sort(Comparator.comparingDouble(Ranking::average).reversed()
                .thenComparing(r -> r.student().name(), String.CASE_INSENSITIVE_ORDER));
        return result;
    }

    // ---------- helpers ----------

    private int indexOf(int id) {
        for (int i = 0; i < students.size(); i++) {
            if (students.get(i).id() == id) return i;
        }
        throw new NoSuchElementException("No student with id " + id);
    }

    private void ensureEmailAvailable(String email, int ignoreId) {
        boolean taken = students.stream()
                .anyMatch(s -> s.id() != ignoreId && s.email().equalsIgnoreCase(email));
        if (taken) {
            throw new IllegalArgumentException("Email already in use: " + email);
        }
    }

    private void saveStudents() {
        studentRepository.saveAll(students);
    }

    private void saveGrades() {
        gradeRepository.saveAll(grades);
    }
}
