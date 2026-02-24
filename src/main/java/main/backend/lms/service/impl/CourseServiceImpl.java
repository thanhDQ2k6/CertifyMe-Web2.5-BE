package main.backend.lms.service.impl;
import main.backend.lms.entity.Course;
import main.backend.lms.repository.CourseRepository;
import main.backend.lms.service.CourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
@Service
public class CourseServiceImpl implements CourseService {
        @Autowired private CourseRepository courseRepo;

        @Override
        public List<Course> getAllActiveCourses() {
            return courseRepo.findByIsActiveTrue();
        }

        @Override
        public void softDeleteCourse(String courseId) {
            Course course = courseRepo.findById(courseId).orElseThrow();
            course.setIsActive(false); // Xóa kiểu cập nhật
            courseRepo.save(course);
        }

        @Override
        public Course createCourse(Course course) {
            return courseRepo.save(course);
        }
}
