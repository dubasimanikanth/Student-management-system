package com.example.sms.service;

import com.example.sms.model.Grade;
import com.example.sms.model.Ranking;
import com.example.sms.model.Student;
import com.example.sms.repository.GradeRepository;
import com.example.sms.repository.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

class StudentServiceTest {

    static class MemoryStudents implements StudentRepository {
        List<Student> stored = new ArrayList<>();
        @Override public List<Student> findAll() { return new ArrayList<>(stored); }
        @Override public void saveAll(List<Student> students) { stored = new ArrayList<>(students); }
    }

    static class MemoryGrades implements GradeRepository {
        List<Grade> stored = new ArrayList<>();
        @Override public List<Grade> findAll() { return new ArrayList<>(stored); }
        @Override public void saveAll(List<Grade> grades) { stored = new ArrayList<>(grades); }
    }

    private MemoryStudents studentRepo;
    private MemoryGrades gradeRepo;
    private StudentService service;

    @BeforeEach
    void setUp() {
        studentRepo = new MemoryStudents();
        gradeRepo = new MemoryGrades();
        service = new StudentService(studentRepo, gradeRepo);
    }

    @Test
    void addStudentAssignsIdsAndPersists() {
        Student a = service.addStudent("Asha Rao", "asha@school.edu", "Science");
        Student b = service.addStudent("Ravi Kumar", "ravi@school.edu", "Arts");
        assertEquals(1, a.id());
        assertEquals(2, b.id());
        assertEquals(2, studentRepo.stored.size());
    }

    @Test
    void invalidEmailIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> service.addStudent("Asha", "not-an-email", "Science"));
    }

    @Test
    void duplicateEmailIsRejectedIgnoringCase() {
        service.addStudent("Asha", "asha@school.edu", "Science");
        assertThrows(IllegalArgumentException.class,
                () -> service.addStudent("Other", "ASHA@school.edu", "Arts"));
    }

    @Test
    void updateKeepsOwnEmailButBlocksSomeoneElsesEmail() {
        Student a = service.addStudent("Asha", "asha@school.edu", "Science");
        service.addStudent("Ravi", "ravi@school.edu", "Arts");
        Student updated = service.updateStudent(a.id(), "Asha R", "asha@school.edu", "Maths");
        assertEquals("Maths", updated.department());
        assertThrows(IllegalArgumentException.class,
                () -> service.updateStudent(a.id(), "Asha R", "ravi@school.edu", "Maths"));
    }

    @Test
    void deleteRemovesStudentAndTheirGrades() {
        Student a = service.addStudent("Asha", "asha@school.edu", "Science");
        service.addGrade(a.id(), "Math", 90);
        service.deleteStudent(a.id());
        assertTrue(service.listAll().isEmpty());
        assertTrue(gradeRepo.stored.isEmpty());
        assertThrows(NoSuchElementException.class, () -> service.findById(a.id()));
    }

    @Test
    void gradeMarksMustBeInRange() {
        Student a = service.addStudent("Asha", "asha@school.edu", "Science");
        assertThrows(IllegalArgumentException.class, () -> service.addGrade(a.id(), "Math", 101));
        assertThrows(IllegalArgumentException.class, () -> service.addGrade(a.id(), "Math", -1));
    }

    @Test
    void gradeForUnknownStudentFails() {
        assertThrows(NoSuchElementException.class, () -> service.addGrade(42, "Math", 80));
    }

    @Test
    void addingSameSubjectReplacesOldGrade() {
        Student a = service.addStudent("Asha", "asha@school.edu", "Science");
        service.addGrade(a.id(), "Math", 60);
        service.addGrade(a.id(), "math", 85);
        assertEquals(1, service.gradesOf(a.id()).size());
        assertEquals(85.0, service.gradesOf(a.id()).get(0).marks());
    }

    @Test
    void averageIsComputedAcrossSubjects() {
        Student a = service.addStudent("Asha", "asha@school.edu", "Science");
        service.addGrade(a.id(), "Math", 80);
        service.addGrade(a.id(), "Physics", 90);
        assertEquals(85.0, service.average(a.id()).getAsDouble(), 0.0001);
        assertTrue(service.average(999).isEmpty());
    }

    @Test
    void rankingsAreSortedByAverageAndSkipUngradedStudents() {
        Student a = service.addStudent("Asha", "asha@school.edu", "Science");
        Student b = service.addStudent("Ravi", "ravi@school.edu", "Arts");
        service.addStudent("Nobody", "nobody@school.edu", "Arts");
        service.addGrade(a.id(), "Math", 70);
        service.addGrade(b.id(), "Math", 95);
        List<Ranking> rankings = service.rankings();
        assertEquals(2, rankings.size());
        assertEquals("Ravi", rankings.get(0).student().name());
        assertEquals("A", rankings.get(0).letter());
        assertEquals("C", rankings.get(1).letter());
    }

    @Test
    void searchMatchesNameEmailAndDepartment() {
        service.addStudent("Asha Rao", "asha@school.edu", "Science");
        service.addStudent("Ravi Kumar", "ravi@school.edu", "Arts");
        assertEquals(1, service.search("rao").size());
        assertEquals(1, service.search("RAVI@").size());
        assertEquals(1, service.search("arts").size());
    }

    @Test
    void letterGradeBoundaries() {
        assertEquals("A", GradeCalculator.letter(90));
        assertEquals("B", GradeCalculator.letter(89.9));
        assertEquals("D", GradeCalculator.letter(60));
        assertEquals("F", GradeCalculator.letter(59.9));
    }

    @Test
    void newServiceReloadsSavedData() {
        Student a = service.addStudent("Asha", "asha@school.edu", "Science");
        service.addGrade(a.id(), "Math", 88);
        StudentService reloaded = new StudentService(studentRepo, gradeRepo);
        assertEquals(1, reloaded.listAll().size());
        assertEquals(88.0, reloaded.average(a.id()).getAsDouble(), 0.0001);
    }
}
