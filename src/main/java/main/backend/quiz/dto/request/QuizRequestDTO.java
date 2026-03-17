package main.backend.quiz.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import java.util.List;

@Data
public class QuizRequestDTO {
    @NotBlank
    private String classId;
    @NotBlank
    private String quizName;
    @NotNull @Positive
    private Integer duration;
    @NotNull
    private Double passingScore;
    private Integer maxScore;
    @NotEmpty @Valid
    private List<QuestionRequestDTO> questions;

    @Data
    public static class QuestionRequestDTO {
        @NotBlank
        private String questionText;
        @NotBlank
        private String questionType;
        @NotEmpty @Valid
        private List<OptionRequestDTO> options;
    }

    @Data
    public static class OptionRequestDTO {
        private String optionId;
        @NotBlank
        private String optionText;
        @JsonProperty("isCorrect")
        private boolean isCorrect;
    }
}
