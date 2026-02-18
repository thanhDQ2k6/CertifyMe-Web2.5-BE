package main.backend.dto.response;

import lombok.Data;

@Data
public class QuestionResponseDTO {
    private Long questionId;
    private String questionText;
    private String optionA;
    private String optionB;
    private String optionC;
    private String optionD;
}