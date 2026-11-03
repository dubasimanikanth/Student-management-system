package com.example.sms.repository;

import com.example.sms.model.Grade;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class FileGradeRepository implements GradeRepository {

    private final Path file;

    public FileGradeRepository(Path file) {
        this.file = file;
    }

    @Override
    public List<Grade> findAll() {
        List<Grade> grades = new ArrayList<>();
        for (String[] r : TsvFile.read(file)) {
            grades.add(new Grade(Integer.parseInt(r[0]), r[1], Double.parseDouble(r[2])));
        }
        return grades;
    }

    @Override
    public void saveAll(List<Grade> grades) {
        List<String[]> rows = new ArrayList<>();
        for (Grade g : grades) {
            rows.add(new String[]{String.valueOf(g.studentId()), g.subject(), String.valueOf(g.marks())});
        }
        TsvFile.write(file, rows);
    }
}
