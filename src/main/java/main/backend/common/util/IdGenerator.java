package main.backend.common.util;

import java.util.UUID;

public class IdGenerator {

  /**
   * Generate user ID dạng UUID
   * Format: 36 ký tự (VD: 550e8400-e29b-41d4-a716-446655440000)
   */
  public static String generateUserId() {
    return UUID.randomUUID().toString();
  }

  /**
   * Generate course ID
   */
  public static String generateCourseId() {
    return "CRS_" + System.currentTimeMillis() + "_" + generateShortUUID();
  }

  /**
   * Generate class ID
   */
  public static String generateClassId() {
    return "CLS_" + System.currentTimeMillis() + "_" + generateShortUUID();
  }

  /**
   * Generate quiz ID
   */
  public static String generateQuizId() {
    return "QZ_" + System.currentTimeMillis() + "_" + generateShortUUID();
  }

  /**
   * Generate certificate ID
   */
  public static String generateCertificateId() {
    return "CERT_" + System.currentTimeMillis() + "_" + generateShortUUID();
  }

  /**
   * Generate short UUID (8 ký tự đầu)
   */
  private static String generateShortUUID() {
    return UUID.randomUUID().toString().substring(0, 8);
  }
}
