package main.backend.lms.repository;

import main.backend.lms.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CourseRepository extends JpaRepository<Course, String> {
    // Lấy các khóa học chưa bị "xóa mềm"
    List<Course> findByIsActiveTrue();
}
