package com.myroom.bookingservice.usecase.pipeline.filters;

import com.myroom.bookingservice.api.model.BookingOrderResponseModel;
import com.myroom.bookingservice.api.model.BookingOrderRequestModel;
import com.myroom.bookingservice.data.entity.BookingDetails;
import com.myroom.bookingservice.data.entity.BookingRequestDetails;
import com.myroom.bookingservice.data.mapper.BookingMapper;
import com.myroom.bookingservice.data.mapper.RoomMapper;
import com.myroom.bookingservice.data.model.BookingPaymentMetaDataModel;
import com.myroom.bookingservice.data.model.RoomMetaDataModel;
import com.myroom.bookingservice.exception.BookingServiceRuntimeException;
import com.myroom.bookingservice.repository.BookingRepository;
import com.myroom.bookingservice.api.constants.BookingStatus;
import com.myroom.bookingservice.data.dto.RoomDetailsDto;
import com.myroom.bookingservice.usecase.KafkaMessageService;
import com.myroom.bookingservice.usecase.RoomService;
import com.myroom.bookingservice.usecase.pipeline.BookingContext;
import com.myroom.bookingservice.usecase.pipeline.BookingFilter;
import com.myroom.bookingservice.usecase.pipeline.BookingFilterChain;
import lombok.extern.slf4j.Slf4j;

import org.json.JSONObject;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@Order(3)
@Slf4j
public class BookingPersistenceFilter implements BookingFilter {

    private final RoomService roomService;
    private final RoomMapper roomMapper;
    private final BookingRepository bookingRepository;
    private final BookingMapper bookingMapper;
private final KafkaMessageService kafkaMessageService;
    public BookingPersistenceFilter(RoomService roomService,
                                    RoomMapper roomMapper,
                                    BookingRepository bookingRepository,
                                    BookingMapper bookingMapper, KafkaMessageService kafkaMessageService) {
        this.roomService = roomService;
        this.roomMapper = roomMapper;
        this.bookingRepository = bookingRepository;
        this.bookingMapper = bookingMapper;
        this.kafkaMessageService = kafkaMessageService;
    }

    @Override
    public BookingOrderResponseModel execute(BookingContext context, BookingFilterChain chain) {
        log.info("creating booking record");
        BookingRequestDetails bookingRequestDetails = context.getBookingRequestDetails();
        try {
            RoomDetailsDto roomDetailsDto = roomService.getRoom(bookingRequestDetails.getRoomId());
            String amount = roomMapper.toAmount(roomDetailsDto);
            RoomMetaDataModel roomMetaDataModel = roomMapper.toRoomMetaDataModel(roomDetailsDto);
            BookingPaymentMetaDataModel bookingPaymentMetaDataModel = roomMapper.toBookingPaymentMetaDataModel(
                    bookingRequestDetails.getPaymentMethodType(), amount, roomMetaDataModel.getPrices().getCurrency(),
                    null);
            BookingStatus status = BookingStatus.PENDING_PAYMENT;
            Instant bookingDate = Instant.now();

            BookingDetails bookingDetails = new BookingDetails();
            bookingDetails.setBookingRequestId(bookingRequestDetails.getId());
            bookingDetails.setOrganizationId(bookingRequestDetails.getOrganizationId());
            bookingDetails.setCheckIn(Instant.parse(bookingRequestDetails.getCheckIn()));
            bookingDetails.setCheckOut(Instant.parse(bookingRequestDetails.getCheckOut()));
            bookingDetails.setPeopleCount((long) (bookingRequestDetails.getGuests().getAdults()
                    + bookingRequestDetails.getGuests().getChildren()));
            bookingDetails.setGuests(bookingRequestDetails.getGuests());
            bookingDetails.setContactDetails(bookingRequestDetails.getContactDetails());
            bookingDetails.setPaymentType(bookingRequestDetails.getPaymentType());
            bookingDetails.setUid(bookingRequestDetails.getUid());
            bookingDetails.setBookingDate(bookingDate);
            bookingDetails.setAmount(amount);
            bookingDetails.setRoomDetails(roomMetaDataModel);
            bookingDetails.setPaymentMetaDataModel(bookingPaymentMetaDataModel);
            bookingDetails.setStatus(status);

            bookingDetails = bookingRepository.save(bookingDetails);
            log.info("created booking record: {}", bookingDetails);
            try {
                log.info("Sending Kafka notification for booking: {}", bookingDetails.getId());
                
                JSONObject message = new JSONObject();
                message.put("bookingId", bookingDetails.getId());
                message.put("uid", bookingDetails.getUid());
                message.put("email", bookingDetails.getContactDetails().getEmailId()); 
                message.put("status", "CREATED");

                kafkaMessageService.sendMessage("booking.mail", message.toString());
                
            } catch (Exception e) {
                log.error("Failed to send Kafka message: {}", e.getMessage());
            }
            // ---------------------------------------------------------
            context.setBookingDetails(bookingDetails);
            return chain.doFilter(context);
        } catch (Exception ex) {
            log.error("Some error occurred: {}", ex.getMessage());
            throw new BookingServiceRuntimeException(ex.getMessage());
        }
    }
}
