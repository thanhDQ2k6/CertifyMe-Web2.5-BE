package main.backend.LMSQuizService.service;

import lombok.RequiredArgsConstructor;
import main.backend.common.util.IdGenerator;
import main.backend.auth.entity.User;
import main.backend.auth.repository.UserRepository;
import main.backend.constant.EnrollmentStatus;
import main.backend.LMSCourseService.model.ClassEntity;
import main.backend.LMSLearningService.model.Enrollment;
import main.backend.LMSLearningService.repository.EnrollmentRepository;
import main.backend.lms.dto.request.QuizRequestDTO;
import main.backend.LMSQuizService.dto.response.QuizResultResponse;
import main.backend.LMSCourseService.dto.response.QuizResponseDTO;
import main.backend.LMSCourseService.dto.response.QuizSubmissionResponseDTO;
import main.backend.LMSQuizService.model.Question;
import main.backend.LMSQuizService.model.Quiz;
import main.backend.LMSQuizService.model.QuizAttempt;
import main.backend.LMSQuizService.repository.QuestionRepository;
import main.backend.LMSQuizService.repository.QuizAttemptRepository;
import main.backend.LMSQuizService.repository.QuizRepository;
import main.backend.lms.dto.request.QuizSubmissionRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuizService {

    private final QuizRepository quizRepository;
    private final QuizAttemptRepository quizAttemptRepository;
    private final UserRepository userRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final QuestionRepository questionRepository;

    public List<QuizResponseDTO> getQuizzesByClassResponse(String classId) {
        List<Quiz> quizzes = quizRepository.findAll();
        return quizzes.stream().map(q -> {
            QuizResponseDTO dto = new QuizResponseDTO();
            dto.setQuizId(q.getQuizId());
            dto.setQuizName(q.getTitle());
            dto.setDuration(q.getDurationMinutes() != null ? q.getDurationMinutes() : 60);
            dto.setPassingScore(q.getPassingScore() != null ? q.getPassingScore() : 5.0);
            dto.setStatus("active");
            return dto;
        }).collect(Collectors.toList());
    }

    @Transactional
    public QuizResponseDTO createQuiz(QuizRequestDTO dto) {
        Quiz quiz = new Quiz();
        quiz.setQuizId(IdGenerator.generateQuizId());
        quiz.setTitle(dto.getQuizName());
        quiz.setDurationMinutes(dto.getDuration());
        quiz.setPassingScore(dto.getPassingScore());
        quiz.setStatus(Quiz.QuizStatus.DRAFT);

        if (dto.getClassId() != null) {
            ClassEntity clazz = new ClassEntity();
            clazz.setClassId(dto.getClassId());
            quiz.setClassEntity(clazz);
        }
        Quiz savedQuiz = quizRepository.save(quiz);

        if (dto.getQuestions() != null) {
            for (QuizRequestDTO.QuestionRequestDTO qDto : dto.getQuestions()) {
                Question question = new Question();
                question.setQuiz(savedQuiz);
                question.setQuestionText(qDto.getQuestionText());

                question.setOptionA("");
                question.setOptionB("");
                question.setOptionC("");
                question.setOptionD("");

                boolean hasCorrectAnswer = false;

                if (qDto.getOptions() != null) {
                    for (QuizRequestDTO.OptionRequestDTO opt : qDto.getOptions()) {
                        if (opt.getOptionId() == null) continue;

                        String optionId = opt.getOptionId().toUpperCase();
                        String optionText = opt.getOptionText() != null ? opt.getOptionText() : "";

                        switch (optionId) {
                            case "A": question.setOptionA(optionText); break;
                            case "B": question.setOptionB(optionText); break;
                            case "C": question.setOptionC(optionText); break;
                            case "D": question.setOptionD(optionText); break;
                        }

                        if (opt.isCorrect()) {
                            question.setCorrectAnswer(Question.AnswerOption.valueOf(optionId));
                            hasCorrectAnswer = true;
                        }
                    }
                }

                if (!hasCorrectAnswer) {
                    question.setCorrectAnswer(Question.AnswerOption.A);
                }

                questionRepository.save(question);
            }
        }

        QuizResponseDTO response = new QuizResponseDTO();
        response.setQuizId(savedQuiz.getQuizId());
        response.setQuizName(savedQuiz.getTitle());
        response.setStatus("draft");
        return response;
    }

    public QuizResponseDTO getQuizDetail(String quizId) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new RuntimeException("Quiz không tồn tại với ID: " + quizId));

        QuizResponseDTO dto = new QuizResponseDTO();
        dto.setQuizId(quiz.getQuizId());
        dto.setQuizName(quiz.getTitle());
        dto.setDuration(quiz.getDurationMinutes());
        dto.setPassingScore(quiz.getPassingScore());
        dto.setStatus(quiz.getStatus() != null ? quiz.getStatus().name().toLowerCase() : "published");

        if (quiz.getQuestions() != null) {
            List<QuizResponseDTO.QuestionResponseDTO> questionDTOs = quiz.getQuestions().stream()
                    .map(q -> {
                        QuizResponseDTO.QuestionResponseDTO qDto = new QuizResponseDTO.QuestionResponseDTO();
                        qDto.setQuestionId(q.getQuestionId());
                        qDto.setQuestionText(q.getQuestionText());
                        qDto.setOptionA(q.getOptionA());
                        qDto.setOptionB(q.getOptionB());
                        qDto.setOptionC(q.getOptionC());
                        qDto.setOptionD(q.getOptionD());
                        return qDto;
                    }).collect(Collectors.toList());
            dto.setQuestions(questionDTOs);
        }

        return dto;
    }
    public QuizResponseDTO updateQuiz(String quizId, QuizRequestDTO dto) {
        Quiz existingQuiz = quizRepository.findById(quizId).orElseThrow();
        existingQuiz.setTitle(dto.getQuizName());
        quizRepository.save(existingQuiz);

        QuizResponseDTO res = new QuizResponseDTO();
        res.setQuizId(quizId);
        res.setQuizName(dto.getQuizName());
        res.setStatus("active");
        return res;
    }

    public void softDeleteQuiz(String quizId) {
        Quiz quiz = quizRepository.findById(quizId).orElseThrow();
        quiz.setStatus(Quiz.QuizStatus.CLOSED);
        quizRepository.save(quiz);
    }

    public List<QuizSubmissionResponseDTO> getQuizSubmissions(String quizId) {
        List<QuizAttempt> attempts = quizAttemptRepository.findByQuiz_QuizId(quizId);

        return attempts.stream().map(a -> {
            QuizSubmissionResponseDTO sub = new QuizSubmissionResponseDTO();
            sub.setSubmissionId(String.valueOf(a.getAttemptId()));
            sub.setStudentId(a.getStudent().getUserId());
            sub.setStudentName(a.getStudent().getFullName());
            sub.setStudentEmail(a.getStudent().getEmail());
            sub.setScore(a.getScore());
            sub.setPassed(a.getIsPassed());
            sub.setSubmittedAt(a.getSubmittedAt());
            return sub;
        }).collect(Collectors.toList());
    }

    @Transactional
    public QuizResultResponse submitQuiz(String quizId, QuizSubmissionRequest request) {
        String studentId = request.getStudentId();

        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new RuntimeException("Quiz không tồn tại"));

        List<Question> questions = quiz.getQuestions();
        if (questions == null || questions.isEmpty())
            throw new RuntimeException("Bài thi này chưa có câu hỏi nào!");

        int correctCount = 0;
        int totalQuestions = questions.size();

        Map<String, String> userAnswersMap = request.getAnswers().stream()
                .collect(Collectors.toMap(
                        main.backend.lms.dto.request.QuizSubmissionRequest.AnswerRequest::getQuestionId,
                        main.backend.lms.dto.request.QuizSubmissionRequest.AnswerRequest::getSelectedOption,
                        (existing, replacement) -> replacement
                ));


        for (Question q : questions) {
            String questionIdStr = String.valueOf(q.getQuestionId());
            String userSelected = userAnswersMap.get(questionIdStr);

            if (userSelected != null && q.getCorrectAnswer() != null) {
                if (q.getCorrectAnswer().name().equalsIgnoreCase(userSelected.trim())) {
                    correctCount++;
                }
            }
        }


        double maxScore = 10.0;
        double finalScore = Math.round((((double) correctCount / totalQuestions) * maxScore) * 10.0) / 10.0;
        boolean passed = finalScore >= (quiz.getPassingScore() != null ? quiz.getPassingScore() : 5.0);

        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Sinh viên không tồn tại"));

        QuizAttempt attempt = QuizAttempt.builder()
                .student(student)
                .quiz(quiz)
                .score(finalScore)
                .isPassed(passed)
                .submittedAt(LocalDateTime.now())
                .build();

        quizAttemptRepository.save(attempt);

        if (quiz.getClassEntity() != null) {
            updateStudentProgress(studentId, quiz.getClassEntity().getClassId());
        }

        return QuizResultResponse.builder()
                .score(finalScore)
                .maxScore(maxScore)
                .status(passed ? "PASSED" : "FAILED")
                .submittedAt(attempt.getSubmittedAt())
                .build();
    }

    private void updateStudentProgress(String studentId, String classId) {
        Enrollment enrollment = enrollmentRepository.findByStudent_UserIdAndClassEntity_ClassId(studentId, classId).orElse(null);
        if (enrollment == null) return;

        List<QuizAttempt> classAttempts = quizAttemptRepository.findByStudent_UserId(studentId).stream()
                .filter(a -> a.getQuiz().getClassEntity().getClassId().equals(classId))
                .collect(Collectors.toList());

        Map<String, Optional<QuizAttempt>> bestAttempts = classAttempts.stream().collect(Collectors.groupingBy(a -> a.getQuiz().getQuizId(), Collectors.maxBy(Comparator.comparingDouble(QuizAttempt::getScore))));

        long passedCount = bestAttempts.values().stream().filter(opt -> opt.map(a -> a.getScore() >= 5.0).orElse(false)).count();
        double avgScore = bestAttempts.values().stream().flatMap(Optional::stream).mapToDouble(QuizAttempt::getScore).average().orElse(0.0);
        int totalQuizzesInClass = Optional.ofNullable(enrollment.getClassEntity().getTotalQuizzes()).orElse(0);

        enrollment.setFinalGrade(Math.round(avgScore * 10.0) / 10.0);
        if (totalQuizzesInClass > 0 && passedCount == totalQuizzesInClass) {
            enrollment.setStatus(Enrollment.EnrollmentStatus.PASSED);
        } else {
            enrollment.setStatus(Enrollment.EnrollmentStatus.LEARNING);
        }
        enrollmentRepository.save(enrollment);
    }
}