package com.myroom.bookingservice.usecase.pipeline.filters;

import com.myroom.bookingservice.api.constants.ApiConstants;
import com.myroom.bookingservice.api.constants.AppConstants;
import com.myroom.bookingservice.api.model.BookingOrderResponseModel;
import com.myroom.bookingservice.api.model.BookingRequestRequestModel;
import com.myroom.bookingservice.data.entity.BookingRequestDetails;
import com.myroom.bookingservice.data.mapper.BookingRequestMapper;
import com.myroom.bookingservice.exception.BookingRequestUnauthorizedAccessException;
import com.myroom.bookingservice.exception.InvalidBookingRequestDataException;
import com.myroom.bookingservice.usecase.BookingRequestService;
import com.myroom.bookingservice.usecase.BookingRequestValidationService;
import com.myroom.bookingservice.usecase.pipeline.BookingContext;
import com.myroom.bookingservice.usecase.pipeline.BookingFilter;
import com.myroom.bookingservice.usecase.pipeline.BookingFilterChain;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(1)
@Slf4j
public class ValidationFilter implements BookingFilter {

    private final BookingRequestService bookingRequestService;
    private final BookingRequestMapper bookingRequestMapper;
    private final BookingRequestValidationService bookingRequestValidationService;

    public ValidationFilter(BookingRequestService bookingRequestService,
                            BookingRequestMapper bookingRequestMapper,
                            BookingRequestValidationService bookingRequestValidationService) {
        this.bookingRequestService = bookingRequestService;
        this.bookingRequestMapper = bookingRequestMapper;
        this.bookingRequestValidationService = bookingRequestValidationService;
    }

    @Override
    public BookingOrderResponseModel execute(BookingContext context, BookingFilterChain chain) {
        String bookingRequestId = context.getBookingRequestId();
        var bookingOrderRequestModel = context.getBookingOrderRequestModel();

        BookingRequestDetails bookingRequestDetails = bookingRequestService.getBookingRequestData(bookingRequestId);
        context.setBookingRequestDetails(bookingRequestDetails);

        BookingRequestRequestModel bookingRequestModel = bookingRequestMapper.toBookingRequestModel(bookingRequestDetails);
        AppConstants.GenericValidInvalidEnum validation = bookingRequestValidationService.validateBookingRequestData(bookingRequestModel);
        if (!validation.equals(AppConstants.GenericValidInvalidEnum.VALID)) {
            log.error("Invalid Booking Request: {}", bookingRequestDetails);
            throw new InvalidBookingRequestDataException(ApiConstants.INVALID_BOOKING_REQUEST_DATA,
                    "Booking cannot be processed due to an invalid booking request data", "");
        }

        // validateCurrentUser
        if (!bookingOrderRequestModel.getUid().equals(bookingRequestDetails.getUid())) {
            log.error("Unauthorized access: Current user '{}' is not the creator of the booking request",
                    bookingOrderRequestModel.getUid());
            throw new BookingRequestUnauthorizedAccessException(ApiConstants.UNAUTHORIZED,
                    "You do not have permission to modify this booking request.", "");
        }
        return chain.doFilter(context);
    }
}
