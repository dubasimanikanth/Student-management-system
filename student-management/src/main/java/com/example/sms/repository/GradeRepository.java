package com.example.sms.repository;

import com.example.sms.model.Grade;

import java.util.List;

public interface GradeRepository {
    List<Grade> findAll();

    void saveAll(List<Grade> grades);
}
