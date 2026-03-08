package main.backend.lms.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

@Data
public class QuizRequestDTO {
    private String classId;
    private String quizName;
    private Integer duration;
    private Double passingScore;
    private Integer maxScore;
    private List<QuestionRequestDTO> questions; // Hứng mảng câu hỏi

    @Data
    public static class QuestionRequestDTO {
        private String questionText;
        private String questionType;
        private List<OptionRequestDTO> options; // Hứng mảng đáp án
    }

    @Data
    public static class OptionRequestDTO {
        private String optionId;
        private String optionText;
        @JsonProperty("isCorrect")
        private boolean isCorrect;
    }
}