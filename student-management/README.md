# Student Management System (Java)

A console application for managing students and their grades.

## Features
- Add, list, search, update and delete students
- Record grades per subject (0-100); re-entering a subject replaces it
- Student report with average and letter grade (A-F)
- Class rankings by average
- Validation (email format, unique email, mark range)
- Data saved to `~/.student-management/students.tsv` and `grades.tsv`

## Structure
    model       Student, Grade, Ranking (immutable records)
    repository  interfaces + tab-separated file implementations
    service     StudentService (business rules), GradeCalculator
    cli         ConsoleMenu (interactive menu)

## Requirements
JDK 17+ and Maven 3.8+

## Build, test, run
    mvn test
    mvn package
    java -jar target/student-management-1.0.0.jar
