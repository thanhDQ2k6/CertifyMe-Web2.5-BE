package main.backend.classroom.repository;

import main.backend.classroom.entity.ClassEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClassRepository extends JpaRepository<ClassEntity, String> {

    List<ClassEntity> findByTeacher_UserId(String teacherId);

    List<ClassEntity> findByCourse_CourseId(String courseId);
}
