package com.example.sms;

import com.example.sms.cli.ConsoleMenu;
import com.example.sms.repository.FileGradeRepository;
import com.example.sms.repository.FileStudentRepository;
import com.example.sms.service.StudentService;

import java.io.IOException;
import java.nio.file.Path;

public class Main {
    public static void main(String[] args) throws IOException {
        Path dir = Path.of(System.getProperty("user.home"), ".student-management");
        StudentService service = new StudentService(
                new FileStudentRepository(dir.resolve("students.tsv")),
                new FileGradeRepository(dir.resolve("grades.tsv")));
        new ConsoleMenu(service, System.in, System.out).run();
    }
}
