package main.backend.course.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class CourseDetailResponse {
    private String courseIcon;
    private String courseName;
    private String courseCode;
    private String teacherName;
    private String startDate;
    private String endDate;
    private Integer progress;
    private Integer totalQuizzes;
    private Integer completedQuizzes;
    private boolean isCompleted;
    private String studentName;
    private Double averageScore;
    private List<QuizDTO> quizzes;
    private CertificateDTO certificate; // Chỉ hiện khi isCompleted = true

    @Data @Builder
    public static class QuizDTO {
        private String id;
        private String name;
        private Double score;
        private Double maxScore;
        private String status; // "completed", "pending", "locked"
    }

    @Data @Builder
    public static class CertificateDTO {
        private String verificationHash;
        private Map<String, String> blockchainInfo;
    }
}