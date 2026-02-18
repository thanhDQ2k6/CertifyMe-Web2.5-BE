package main.backend.dto.request;

import lombok.Data;
import java.util.Map;

@Data
public class QuizSubmissionRequest {
    private String studentId;
    private String classId;
    private String quizId;
    // Key là questionId, Value là đáp án SV chọn (A, B, C, D)
    private Map<Long, String> answers;
}