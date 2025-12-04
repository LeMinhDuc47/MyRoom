package com.myroom.bookingservice.exception;

import com.myroom.bookingservice.api.constants.ApiConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;

@ControllerAdvice
@Slf4j
public class BookingServiceExceptionHandler {
    @ExceptionHandler({ PaymentServiceException.class })
    public ResponseEntity<BookingServiceWebException> handlePaymentServiceException(PaymentServiceException exception,
            WebRequest request) {
        log.error("Error", exception);
        return new ResponseEntity<>(new BookingServiceWebException(exception.getErrorCode(), exception.getMessage(),
                exception.getDetails()), HttpStatus.BAD_REQUEST);
    }

        @ExceptionHandler({ PaymentServiceUnavailableException.class, CallNotPermittedException.class })
        public ResponseEntity<BookingServiceWebException> handlePaymentServiceUnavailable(Exception exception, WebRequest request) {
                log.error("Service Unavailable", exception);
                return new ResponseEntity<>(new BookingServiceWebException(
                                ApiConstants.PAYMENT_SERVICE_UNAVAILABLE,
                                ApiConstants.MESSAGE_SERVICE_UNAVAILABLE,
                                exception.getMessage()
                ), HttpStatus.SERVICE_UNAVAILABLE);
        }

    @ExceptionHandler({ InvalidBookingRequestDataException.class })
    public ResponseEntity<BookingServiceWebException> handleInvalidBookingRequestDataException(
            InvalidBookingRequestDataException exception, WebRequest request) {
        log.error("Error", exception);
        return new ResponseEntity<>(new BookingServiceWebException(exception.getErrorCode(), exception.getMessage(),
                exception.getDetails()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({ InvalidPaymentMethodTypeException.class })
    public ResponseEntity<BookingServiceWebException> handleInvalidPaymentMethodTypeException(
            InvalidPaymentMethodTypeException exception, WebRequest request) {
        log.error("Error", exception);
        return new ResponseEntity<>(new BookingServiceWebException(exception.getErrorCode(), exception.getMessage(),
                exception.getDetails()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({ InvalidPaymentTypeException.class })
    public ResponseEntity<BookingServiceWebException> handleInvalidPaymentTypeException(
            InvalidPaymentTypeException exception, WebRequest request) {
        log.error("Error", exception);
        return new ResponseEntity<>(new BookingServiceWebException(exception.getErrorCode(), exception.getMessage(),
                exception.getDetails()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({ RoomUnavailableException.class, })
    public ResponseEntity<BookingServiceWebException> handleRoomUnavailableException(RoomUnavailableException exception,
            WebRequest request) {
        log.error("Error", exception);
        return new ResponseEntity<>(new BookingServiceWebException(exception.getErrorCode(), exception.getMessage(),
                exception.getDetails()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({ BookingRequestUnauthorizedAccessException.class })
    public ResponseEntity<BookingServiceWebException> handleBookingRequestUnauthorizedAccessException(
            BookingRequestUnauthorizedAccessException exception, WebRequest request) {
        log.error("Error", exception);
        return new ResponseEntity<>(new BookingServiceWebException(exception.getErrorCode(), exception.getMessage(),
                exception.getDetails()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({ BookingRequestNotFoundException.class })
    public ResponseEntity<BookingServiceWebException> handleBookingRequestNotFoundException(
            BookingRequestNotFoundException exception, WebRequest request) {
        log.error("Error", exception);
        return new ResponseEntity<>(new BookingServiceWebException(exception.getErrorCode(), exception.getMessage(),
                exception.getDetails()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({ OrganizationServiceException.class })
    public ResponseEntity<BookingServiceWebException> handleOrganizationServiceException(
            OrganizationServiceException exception, WebRequest request) {
        log.error("Error", exception);
        return new ResponseEntity<>(new BookingServiceWebException(exception.getErrorCode(), exception.getMessage(),
                exception.getDetails()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({ RoomServiceException.class })
    public ResponseEntity<BookingServiceWebException> handleRoomServiceException(RoomServiceException exception,
            WebRequest request) {
        log.error("Error", exception);
        return new ResponseEntity<>(new BookingServiceWebException(exception.getErrorCode(), exception.getMessage(),
                exception.getDetails()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({ Exception.class })
    public ResponseEntity<BookingServiceWebException> handleException(Exception exception, WebRequest request) {
        log.error("Error", exception);
        return new ResponseEntity<>(new BookingServiceWebException(ApiConstants.INTERNAL_SERVER_ERROR,
                ApiConstants.MESSAGE_INTERNAL_SERVER_ERROR, ""), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(BookingServiceRuntimeException.class)
    public ResponseEntity<BookingServiceWebException> handleBookingServiceRuntimeException(
            BookingServiceRuntimeException exception, WebRequest request) {
        log.warn("{}", exception.getMessage());
        return new ResponseEntity<>(
                new BookingServiceWebException(
                        "BOOKING_CONFLICT",
                        exception.getMessage(),
                        "The selected room/dates are no longer available. Please try again."),
                HttpStatus.CONFLICT);
    }
}