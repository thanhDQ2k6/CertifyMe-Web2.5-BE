package main.backend.service;

import main.backend.entity.Course;
import java.util.List;

public interface CourseService {
    List<Course> getAllActiveCourses();
    Course createCourse(Course course);
    void softDeleteCourse(String courseId); // Cập nhật isActive = false
}