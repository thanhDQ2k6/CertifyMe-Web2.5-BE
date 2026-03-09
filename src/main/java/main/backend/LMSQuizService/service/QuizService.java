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

                if (qDto.getOptions() != null) {
                    for (QuizRequestDTO.OptionRequestDTO opt : qDto.getOptions()) {
                        String optionId = opt.getOptionId().toUpperCase();
                        switch (optionId) {
                            case "A": question.setOptionA(opt.getOptionText()); break;
                            case "B": question.setOptionB(opt.getOptionText()); break;
                            case "C": question.setOptionC(opt.getOptionText()); break;
                            case "D": question.setOptionD(opt.getOptionText()); break;
                        }
                        // FIX: Ép kiểu sang Enum chuẩn
                        if (opt.isCorrect()) question.setCorrectAnswer(Question.AnswerOption.valueOf(optionId));
                    }
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
        QuizSubmissionResponseDTO sub = new QuizSubmissionResponseDTO();
        sub.setSubmissionId("sub1");
        sub.setStudentName("Nguyễn Văn An");
        sub.setScore(8.5);
        sub.setPassed(true);
        return List.of(sub);
    }

    @Transactional
    public QuizResultResponse submitQuiz(String quizId, String studentId, List<String> userAnswers) {
        Quiz quiz = quizRepository.findById(quizId).orElseThrow(() -> new RuntimeException("Quiz không tồn tại"));
        List<Question> questions = quiz.getQuestions();
        if (questions == null || questions.isEmpty()) throw new RuntimeException("Bài thi này chưa có câu hỏi nào!");

        int correctCount = 0;
        int totalQuestions = questions.size();
        for (int i = 0; i < totalQuestions; i++) {
            if (i < userAnswers.size()) {
                String correctAns = questions.get(i).getCorrectAnswer().toString();
                if (correctAns.equalsIgnoreCase(userAnswers.get(i))) correctCount++;
            }
        }

        double maxScore = 10.0; // FIX: Cố định điểm tối đa
        double finalScore = Math.round((((double) correctCount / totalQuestions) * maxScore) * 10.0) / 10.0;
        boolean passed = finalScore >= quiz.getPassingScore();

        User student = userRepository.findById(studentId).orElseThrow();
        QuizAttempt attempt = QuizAttempt.builder().student(student).quiz(quiz).score(finalScore).isPassed(passed).submittedAt(LocalDateTime.now()).build();
        quizAttemptRepository.save(attempt);
        updateStudentProgress(studentId, quiz.getClassEntity().getClassId());

        return QuizResultResponse.builder().score(finalScore).maxScore(maxScore).status(passed ? "PASSED" : "FAILED").submittedAt(attempt.getSubmittedAt()).build();
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