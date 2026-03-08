package main.backend.lms.dto.request;
import lombok.Data;
import java.util.List;

@Data
public class QuizSubmitRequest {
    private String title;
    private Long courseId;
    private Integer timeLimit;
    private List<QuestionRequest> questions;

    @Data
    public static class QuestionRequest {
        private String content;
        private List<String> options;
        private Integer correctOption;
    }
}
