package com.myroom.onboardingservice.exception;

import com.myroom.onboardingservice.api.constants.ApiConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

@ControllerAdvice
@Slf4j
public class OnboardingServiceExceptionHandler {
    @ExceptionHandler({ OrganizationServiceException.class })
    public ResponseEntity<OnboardingServiceWebException> handleOrganizationServiceException(
            OrganizationServiceException exception, WebRequest request) {
        log.error("OrganizationServiceException: errorCode={}, message={}, details={}",
                exception.getErrorCode(), exception.getMessage(), exception.getDetails());
        return new ResponseEntity<>(
                new OnboardingServiceWebException(exception.getErrorCode(), exception.getMessage(),
                        exception.getDetails()),
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({ OnboardingServiceRuntimeException.class })
    public ResponseEntity<OnboardingServiceWebException> handleOnboardingServiceRuntimeException(
            OnboardingServiceRuntimeException exception, WebRequest request) {
        log.error("OnboardingServiceRuntimeException: {}", exception.getMessage(), exception);
        // Check if the error message indicates service unavailable
        String errorMessage = exception.getMessage();
        if (errorMessage != null && errorMessage.contains("not available")) {
            return new ResponseEntity<>(
                    new OnboardingServiceWebException(
                            ApiConstants.INTERNAL_SERVER_ERROR,
                            errorMessage,
                            "Please ensure all required services are running and registered with Eureka"),
                    HttpStatus.SERVICE_UNAVAILABLE);
        }
        return new ResponseEntity<>(
                new OnboardingServiceWebException(
                        ApiConstants.INTERNAL_SERVER_ERROR,
                        errorMessage != null ? errorMessage : ApiConstants.MESSAGE_INTERNAL_SERVER_ERROR,
                        ApiConstants.MESSAGE_INTERNAL_SERVER_ERROR),
                HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler({ Exception.class })
    public ResponseEntity<OnboardingServiceWebException> handleGenericException(Exception exception,
            WebRequest request) {
        log.error("Unexpected exception: {}", exception.getMessage(), exception);
        return new ResponseEntity<>(
                new OnboardingServiceWebException(
                        ApiConstants.INTERNAL_SERVER_ERROR,
                        ApiConstants.MESSAGE_INTERNAL_SERVER_ERROR,
                        exception.getMessage() != null ? exception.getMessage() : "An unexpected error occurred"),
                HttpStatus.INTERNAL_SERVER_ERROR);
    }
}