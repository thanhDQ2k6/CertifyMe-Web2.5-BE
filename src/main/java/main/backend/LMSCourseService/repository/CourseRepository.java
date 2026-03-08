package main.backend.LMSCourseService.repository;

import main.backend.LMSCourseService.model.CourseEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CourseRepository extends JpaRepository<CourseEntity, String> {
}