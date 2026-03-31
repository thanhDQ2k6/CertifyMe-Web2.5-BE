package main.backend.quiz.service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import main.backend.auth.entity.User;
import main.backend.auth.service.UserQueryService;
import main.backend.classroom.entity.ClassEntity;
import main.backend.classroom.repository.ClassRepository;
import main.backend.common.enums.EnrollmentStatus;
import main.backend.common.enums.QuizStatus;
import main.backend.common.exception.BusinessException;
import main.backend.common.exception.ResourceNotFoundException;
import main.backend.common.util.IdGenerator;
import main.backend.course.dto.response.QuizResponseDTO;
import main.backend.course.dto.response.QuizSubmissionResponseDTO;
import main.backend.enrollment.entity.Enrollment;
import main.backend.enrollment.repository.EnrollmentRepository;
import main.backend.quiz.dto.request.QuizRequestDTO;
import main.backend.quiz.dto.request.QuizSubmissionRequest;
import main.backend.quiz.dto.response.QuizResultDetailResponse;
import main.backend.quiz.dto.response.QuizResultResponse;
import main.backend.quiz.entity.Question;
import main.backend.quiz.entity.Quiz;
import main.backend.quiz.entity.QuizAttempt;
import main.backend.quiz.repository.QuestionRepository;
import main.backend.quiz.repository.QuizAttemptRepository;
import main.backend.quiz.repository.QuizRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class QuizService {

  private final QuizRepository quizRepository;
  private final QuizAttemptRepository quizAttemptRepository;
  private final QuestionRepository questionRepository;
  private final EnrollmentRepository enrollmentRepository;
  private final ClassRepository classRepository;
  private final UserQueryService userQueryService;

  // FIX QS1: filter by classId thay vì findAll()
  // FIX QS3: dùng status thực tế thay vì hardcode "active"
  public List<QuizResponseDTO> getQuizzesByClassResponse(String classId) {
    List<Quiz> quizzes = quizRepository.findByClassEntity_ClassIdAndStatusNot(
      classId,
      QuizStatus.CLOSED
    );
    return quizzes.stream().map(this::mapToQuizResponse).toList();
  }

  public QuizResponseDTO getQuizDetail(String quizId) {
    Quiz quiz = quizRepository
      .findById(quizId)
      .orElseThrow(() ->
        new ResourceNotFoundException("Quiz not found: " + quizId)
      );

    QuizResponseDTO dto = mapToQuizResponse(quiz);

    if (quiz.getQuestions() != null) {
      List<QuizResponseDTO.QuestionResponseDTO> questionDTOs = quiz
        .getQuestions()
        .stream()
        .map(q -> {
          QuizResponseDTO.QuestionResponseDTO qDto =
            new QuizResponseDTO.QuestionResponseDTO();
          qDto.setQuestionId(q.getQuestionId());
          qDto.setQuestionText(q.getQuestionText());
          qDto.setOptionA(q.getOptionA());
          qDto.setOptionB(q.getOptionB());
          qDto.setOptionC(q.getOptionC());
          qDto.setOptionD(q.getOptionD());
          return qDto;
        })
        .toList();
      dto.setQuestions(questionDTOs);
    }

    return dto;
  }

  @Transactional
  public QuizResponseDTO createQuiz(QuizRequestDTO dto) {
    ClassEntity clazz = classRepository
      .findById(dto.getClassId())
      .orElseThrow(() ->
        new ResourceNotFoundException("Class not found: " + dto.getClassId())
      );

    Quiz quiz = new Quiz();
    quiz.setQuizId(IdGenerator.generateQuizId());
    quiz.setTitle(dto.getQuizName());
    quiz.setDurationMinutes(dto.getDuration());
    quiz.setPassingScore(dto.getPassingScore());
    quiz.setStatus(QuizStatus.DRAFT);
    quiz.setClassEntity(clazz);

    Quiz savedQuiz = quizRepository.save(quiz);
    saveQuestions(savedQuiz, dto.getQuestions());

    return mapToQuizResponse(savedQuiz);
  }

  // FIX QS4: update TẤT CẢ fields, không chỉ title
  @Transactional
  public QuizResponseDTO updateQuiz(String quizId, QuizRequestDTO dto) {
    Quiz quiz = quizRepository
      .findById(quizId)
      .orElseThrow(() ->
        new ResourceNotFoundException("Quiz not found: " + quizId)
      );

    if (dto.getQuizName() != null) quiz.setTitle(dto.getQuizName());
    if (dto.getDuration() != null) quiz.setDurationMinutes(dto.getDuration());
    if (dto.getPassingScore() != null) quiz.setPassingScore(
      dto.getPassingScore()
    );

    quizRepository.save(quiz);

    if (dto.getQuestions() != null && !dto.getQuestions().isEmpty()) {
      questionRepository.deleteAll(quiz.getQuestions());
      saveQuestions(quiz, dto.getQuestions());
    }

    return mapToQuizResponse(quiz);
  }

  public void softDeleteQuiz(String quizId) {
    Quiz quiz = quizRepository
      .findById(quizId)
      .orElseThrow(() ->
        new ResourceNotFoundException("Quiz not found: " + quizId)
      );
    quiz.setStatus(QuizStatus.CLOSED);
    quizRepository.save(quiz);
  }

  public List<QuizSubmissionResponseDTO> getQuizSubmissions(String quizId) {
    List<QuizAttempt> attempts = quizAttemptRepository.findByQuiz_QuizId(
      quizId
    );

    return attempts
      .stream()
      .map(a -> {
        QuizSubmissionResponseDTO sub = new QuizSubmissionResponseDTO();
        sub.setSubmissionId(String.valueOf(a.getAttemptId()));
        sub.setStudentId(a.getStudent().getUserId());
        sub.setStudentName(a.getStudent().getFullName());
        sub.setStudentEmail(a.getStudent().getEmail());
        sub.setScore(a.getScore());
        sub.setPassed(a.getIsPassed());
        sub.setSubmittedAt(a.getSubmittedAt());
        return sub;
      })
      .toList();
  }

  public QuizResultDetailResponse getQuizResultForStudent(String quizId, String studentId) {
    Quiz quiz = quizRepository
      .findById(quizId)
      .orElseThrow(() -> new ResourceNotFoundException("Quiz not found: " + quizId));

    List<QuizAttempt> attempts = quizAttemptRepository
      .findByStudentAndQuizOrderByScoreDesc(studentId, quizId);

    if (attempts.isEmpty()) {
      throw new ResourceNotFoundException("No attempt found for this quiz");
    }

    QuizAttempt bestAttempt = attempts.get(0);
    double passingScore = quiz.getPassingScore() != null ? quiz.getPassingScore() : 5.0;

    return QuizResultDetailResponse.builder()
      .quizId(quiz.getQuizId())
      .quizTitle(quiz.getTitle())
      .score(bestAttempt.getScore())
      .maxScore(10.0)
      .passingScore(passingScore)
      .status(bestAttempt.getIsPassed() ? "PASSED" : "FAILED")
      .attemptCount(attempts.size())
      .submittedAt(bestAttempt.getSubmittedAt().toString())
      .build();
  }

  @Transactional
  public QuizResultResponse submitQuiz(
    String quizId,
    QuizSubmissionRequest request
  ) {
    String studentId = request.getStudentId();

    Quiz quiz = quizRepository
      .findById(quizId)
      .orElseThrow(() ->
        new ResourceNotFoundException("Quiz not found: " + quizId)
      );

    List<Question> questions = quiz.getQuestions();
    if (questions == null || questions.isEmpty()) {
      throw new BusinessException("This quiz has no questions");
    }

    int correctCount = 0;
    int totalQuestions = questions.size();

    Map<String, String> userAnswersMap = request
      .getAnswers()
      .stream()
      .collect(
        Collectors.toMap(
          QuizSubmissionRequest.AnswerRequest::getQuestionId,
          QuizSubmissionRequest.AnswerRequest::getSelectedOption,
          (existing, replacement) -> replacement
        )
      );

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
    double finalScore =
      Math.round((((double) correctCount / totalQuestions) * maxScore) * 10.0) /
      10.0;
    // FIX QS5: dùng quiz.getPassingScore() thay vì hardcode
    double passingScore =
      quiz.getPassingScore() != null ? quiz.getPassingScore() : 5.0;
    boolean passed = finalScore >= passingScore;

    User student = userQueryService.getByIdOrThrow(studentId);

    // FIX QS6: set startedAt
    QuizAttempt attempt = QuizAttempt.builder()
      .student(student)
      .quiz(quiz)
      .score(finalScore)
      .isPassed(passed)
      .startedAt(LocalDateTime.now())
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
    Enrollment enrollment = enrollmentRepository
      .findByStudent_UserIdAndClassEntity_ClassId(studentId, classId)
      .orElse(null);
    if (enrollment == null) return;

    List<QuizAttempt> classAttempts =
      quizAttemptRepository.findByStudentAndClass(studentId, classId);

    Map<String, Optional<QuizAttempt>> bestAttempts = classAttempts
      .stream()
      .collect(
        Collectors.groupingBy(
          a -> a.getQuiz().getQuizId(),
          Collectors.maxBy(Comparator.comparingDouble(QuizAttempt::getScore))
        )
      );

    long passedCount = bestAttempts
      .values()
      .stream()
      .filter(opt ->
        opt
          .map(a -> {
            double ps =
              a.getQuiz().getPassingScore() != null
                ? a.getQuiz().getPassingScore()
                : 5.0;
            return a.getScore() >= ps;
          })
          .orElse(false)
      )
      .count();

    double avgScore = bestAttempts
      .values()
      .stream()
      .flatMap(Optional::stream)
      .mapToDouble(QuizAttempt::getScore)
      .average()
      .orElse(0.0);

    int totalQuizzesInClass = Optional.ofNullable(
      enrollment.getClassEntity().getTotalQuizzes()
    ).orElse(0);

    enrollment.setPassedQuizzes((int) passedCount);
    enrollment.setFinalGrade(Math.round(avgScore * 10.0) / 10.0);

    if (totalQuizzesInClass > 0 && passedCount >= totalQuizzesInClass) {
      enrollment.setStatus(EnrollmentStatus.PASSED);
    } else {
      enrollment.setStatus(EnrollmentStatus.LEARNING);
    }
    enrollmentRepository.save(enrollment);
  }

  private void saveQuestions(
    Quiz quiz,
    List<QuizRequestDTO.QuestionRequestDTO> questionDTOs
  ) {
    if (questionDTOs == null) return;

    for (QuizRequestDTO.QuestionRequestDTO qDto : questionDTOs) {
      Question question = new Question();
      question.setQuiz(quiz);
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
          String optionText =
            opt.getOptionText() != null ? opt.getOptionText() : "";
          switch (optionId) {
            case "A" -> question.setOptionA(optionText);
            case "B" -> question.setOptionB(optionText);
            case "C" -> question.setOptionC(optionText);
            case "D" -> question.setOptionD(optionText);
          }
          if (opt.isCorrect()) {
            question.setCorrectAnswer(Question.AnswerOption.valueOf(optionId));
            hasCorrectAnswer = true;
          }
        }
      }
      // FIX QS8: throw error thay vì default về A
      if (!hasCorrectAnswer) {
        throw new BusinessException(
          "Question '" + qDto.getQuestionText() + "' must have a correct answer"
        );
      }
      questionRepository.save(question);
    }
  }

  private QuizResponseDTO mapToQuizResponse(Quiz q) {
    QuizResponseDTO dto = new QuizResponseDTO();
    dto.setQuizId(q.getQuizId());
    dto.setQuizName(q.getTitle());
    dto.setDuration(
      q.getDurationMinutes() != null ? q.getDurationMinutes() : 60
    );
    dto.setPassingScore(
      q.getPassingScore() != null ? q.getPassingScore() : 5.0
    );
    dto.setStatus(
      q.getStatus() != null ? q.getStatus().name().toLowerCase() : "draft"
    );
    return dto;
  }
}
