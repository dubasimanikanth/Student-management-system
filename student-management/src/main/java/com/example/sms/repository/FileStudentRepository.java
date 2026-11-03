package com.example.sms.repository;

import com.example.sms.model.Student;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class FileStudentRepository implements StudentRepository {

    private final Path file;

    public FileStudentRepository(Path file) {
        this.file = file;
    }

    @Override
    public List<Student> findAll() {
        List<Student> students = new ArrayList<>();
        for (String[] r : TsvFile.read(file)) {
            students.add(new Student(Integer.parseInt(r[0]), r[1], r[2], r[3]));
        }
        return students;
    }

    @Override
    public void saveAll(List<Student> students) {
        List<String[]> rows = new ArrayList<>();
        for (Student s : students) {
            rows.add(new String[]{String.valueOf(s.id()), s.name(), s.email(), s.department()});
        }
        TsvFile.write(file, rows);
    }
}
