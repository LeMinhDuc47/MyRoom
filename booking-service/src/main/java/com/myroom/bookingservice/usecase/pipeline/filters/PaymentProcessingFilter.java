package com.myroom.bookingservice.usecase.pipeline.filters;

import com.myroom.bookingservice.api.constants.ApiConstants;
import com.myroom.bookingservice.api.constants.BookingRequestStatus;
import com.myroom.bookingservice.api.constants.BookingStatus;
import com.myroom.bookingservice.api.constants.PaymentType;
import com.myroom.bookingservice.api.model.BookingOrderResponseModel;
import com.myroom.bookingservice.data.entity.BookingDetails;
import com.myroom.bookingservice.data.mapper.BookingMapper;
import com.myroom.bookingservice.data.model.StripePaymentServiceProvider;
import com.myroom.bookingservice.data.dto.OnlinePaymentOrderResponseDto;
import com.myroom.bookingservice.data.dto.PaymentOrderRequestDto;
import com.myroom.bookingservice.data.dto.PaymentOrderResponseDto;
import com.myroom.bookingservice.data.dto.StripePaymentOrderResponseDto;
import com.myroom.bookingservice.exception.InvalidPaymentMethodTypeException;
import com.myroom.bookingservice.exception.InvalidPaymentTypeException;
import com.myroom.bookingservice.exception.BookingServiceRuntimeException;
import com.myroom.bookingservice.repository.BookingRepository;
import com.myroom.bookingservice.usecase.BookingRequestService;
import com.myroom.bookingservice.usecase.KafkaMessageService;
import com.myroom.bookingservice.usecase.PaymentService;
import com.myroom.bookingservice.usecase.pipeline.BookingContext;
import com.myroom.bookingservice.usecase.pipeline.BookingFilter;
import com.myroom.bookingservice.usecase.pipeline.BookingFilterChain;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(4)
@Slf4j
public class PaymentProcessingFilter implements BookingFilter {

    private final PaymentService paymentService;
    private final BookingMapper bookingMapper;
    private final BookingRepository bookingRepository;
    private final BookingRequestService bookingRequestService;
    private final KafkaMessageService kafkaMessageService;

    public PaymentProcessingFilter(PaymentService paymentService,
                                   BookingMapper bookingMapper,
                                   BookingRepository bookingRepository,
                                   BookingRequestService bookingRequestService,
                                   KafkaMessageService kafkaMessageService) {
        this.paymentService = paymentService;
        this.bookingMapper = bookingMapper;
        this.bookingRepository = bookingRepository;
        this.bookingRequestService = bookingRequestService;
        this.kafkaMessageService = kafkaMessageService;
    }

    @Override
    public BookingOrderResponseModel execute(BookingContext context, BookingFilterChain chain) {
        BookingDetails bookingDetails = context.getBookingDetails();
        PaymentType type = bookingDetails.getPaymentType();

        switch (type) {
            case PAY_AT_HOTEL: {
                log.info("Redirecting to {} as the payment type is: {}", type, type);
                updateStatus(bookingDetails, BookingStatus.PAY_AT_HOTEL);
                BookingOrderResponseModel response = bookingMapper.toBookingOrderResponseModel(bookingDetails);
                context.setBookingOrderResponseModel(response);
                bookingRequestService.updateStatus(bookingDetails.getBookingRequestId(), BookingRequestStatus.BOOKED);
                return response;
            }
            case ONLINE_PAYMENT: {
                log.info("Redirecting to {} for payment as the payment type is: {}", type, type);
                OnlinePaymentOrderResponseDto onlinePaymentOrderResponseDto = doOnlinePayment(bookingDetails);
                BookingOrderResponseModel response = bookingMapper.toBookingOrderResponseModel(onlinePaymentOrderResponseDto, bookingDetails);
                context.setBookingOrderResponseModel(response);
                return response;
            }
            default: {
                log.info("Booking cannot be processed due to an invalid payment type: {}", type);
                throw new InvalidPaymentTypeException(ApiConstants.INVALID_PAYMENT_TYPE, "Invalid payment type", "");
            }
        }
    }

    private void updateStatus(BookingDetails bookingDetails, BookingStatus bookingStatus) {
        log.info("Changing status to {}", bookingStatus);
        bookingDetails.setStatus(bookingStatus);
        log.info("Saving booking record");
        bookingRepository.save(bookingDetails);
        log.info("Saved booking record");
    }

    private OnlinePaymentOrderResponseDto doOnlinePayment(BookingDetails bookingDetails) {
        log.info("Creating booking payment order.");
        switch (bookingDetails.getPaymentMetaDataModel().getPaymentMethodType()) {
            case STRIPE: {
                log.info("Redirecting to {} payment service provider for payment order , as the payment method is: {}",
                        bookingDetails.getPaymentMetaDataModel().getPaymentMethodType(),
                        bookingDetails.getPaymentMetaDataModel().getPaymentMethodType());
                StripePaymentOrderResponseDto stripePaymentOrderResponseDto = createStripePaymentOrder(bookingDetails);
                OnlinePaymentOrderResponseDto onlinePaymentOrderResponseDto = bookingMapper
                        .toOnlinePaymentOrderResponseDto(stripePaymentOrderResponseDto);
                log.info("Created booking payment order: {}", onlinePaymentOrderResponseDto);
                return onlinePaymentOrderResponseDto;
            }
            default: {
                log.info("Booking cannot be processed due to invalid payment method type: {}",
                        bookingDetails.getPaymentMetaDataModel().getPaymentMethodType());
                throw new InvalidPaymentMethodTypeException(ApiConstants.INVALID_PAYMENT_METHOD_TYPE,
                        "Invalid payment method type", "");
            }
        }
    }

    private StripePaymentOrderResponseDto createStripePaymentOrder(BookingDetails bookingDetails) {
        PaymentOrderRequestDto paymentOrderRequestDto = bookingMapper.toPaymentOrderRequestDto(bookingDetails);

        log.info("Creating {} payment order for: {}", bookingDetails.getPaymentMetaDataModel().getPaymentMethodType(),
                paymentOrderRequestDto);
        PaymentOrderResponseDto paymentOrderResponseDto = paymentService.createPaymentOrder(paymentOrderRequestDto);
        log.info("Created {} payment order: {}", bookingDetails.getPaymentMetaDataModel().getPaymentMethodType(),
                paymentOrderResponseDto);

        updateBookingDetails(bookingDetails, paymentOrderResponseDto);
        log.info("Updated bookingDetails with {} data",
                bookingDetails.getPaymentMetaDataModel().getPaymentMethodType());

        return bookingMapper.toStripePaymentOrderResponseDto(paymentOrderResponseDto);
    }

    private void updateBookingDetails(BookingDetails bookingDetails, PaymentOrderResponseDto paymentOrderResponseDto) {
        log.info("Updating bookingDetails with {} Payment Service Provider data",
                bookingDetails.getPaymentMetaDataModel().getPaymentMethodType());
        StripePaymentServiceProvider stripePaymentServiceProvider = bookingMapper
                .toStripePaymentServiceProvider(paymentOrderResponseDto);
        bookingDetails.getPaymentMetaDataModel().setStripePaymentServiceProvider(stripePaymentServiceProvider);
        try {
            bookingRepository.updatePaymentMetaDataModelById(bookingDetails.getId(), bookingDetails.getPaymentMetaDataModel());
            log.info("Updated booking details with payment service provider");
        } catch (Exception ex) {
            log.info("Some error occurred while updating the booking details: {}", ex.getMessage());
            throw new BookingServiceRuntimeException(ex.getMessage());
        }
    }
}
