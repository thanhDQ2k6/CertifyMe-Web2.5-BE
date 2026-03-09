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
    private List<QuestionRequestDTO> questions;

    @Data
    public static class QuestionRequestDTO {
        private String questionText;
        private String questionType;
        private List<OptionRequestDTO> options;
    }

    @Data
    public static class OptionRequestDTO {
        private String optionId;
        private String optionText;
        @JsonProperty("isCorrect")
        private boolean isCorrect;
    }
}