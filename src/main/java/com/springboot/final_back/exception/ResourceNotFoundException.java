package com.springboot.final_back.exception;

/**
 * 요청한 리소스를 찾을 수 없을 때 발생하는 예외
 */
public class ResourceNotFoundException extends RuntimeException {

  public ResourceNotFoundException(String message) {
    super(message);
  }

  public static ResourceNotFoundException memberNotFound(String userId) {
    return new ResourceNotFoundException("Member not found with userId: " + userId);
  }

  public static ResourceNotFoundException diaryNotFound(String diaryId) {
    return new ResourceNotFoundException("Diary not found with id: " + diaryId);
  }

  public static ResourceNotFoundException tourSpotNotFound(String contentId) {
    return new ResourceNotFoundException("Tour spot not found with contentId: " + contentId);
  }
}
