package com.myroom.bookingservice.usecase.pipeline;

import com.myroom.bookingservice.api.model.BookingOrderRequestModel;
import com.myroom.bookingservice.api.model.BookingOrderResponseModel;
import com.myroom.bookingservice.data.entity.BookingDetails;
import com.myroom.bookingservice.data.entity.BookingRequestDetails;
import org.redisson.api.RLock;

/**
 * Mutable context passed between filters.
 */
public class BookingContext {
    private String bookingRequestId;
    private BookingOrderRequestModel bookingOrderRequestModel;
    private BookingRequestDetails bookingRequestDetails;
    private BookingDetails bookingDetails;
    private RLock redissonLock;
    private String redissonLockKey;
    private BookingOrderResponseModel bookingOrderResponseModel;

    public String getBookingRequestId() { return bookingRequestId; }
    public void setBookingRequestId(String bookingRequestId) { this.bookingRequestId = bookingRequestId; }

    public BookingOrderRequestModel getBookingOrderRequestModel() { return bookingOrderRequestModel; }
    public void setBookingOrderRequestModel(BookingOrderRequestModel bookingOrderRequestModel) { this.bookingOrderRequestModel = bookingOrderRequestModel; }

    public BookingRequestDetails getBookingRequestDetails() { return bookingRequestDetails; }
    public void setBookingRequestDetails(BookingRequestDetails bookingRequestDetails) { this.bookingRequestDetails = bookingRequestDetails; }

    public BookingDetails getBookingDetails() { return bookingDetails; }
    public void setBookingDetails(BookingDetails bookingDetails) { this.bookingDetails = bookingDetails; }

    public RLock getRedissonLock() { return redissonLock; }
    public void setRedissonLock(RLock redissonLock) { this.redissonLock = redissonLock; }

    public String getRedissonLockKey() { return redissonLockKey; }
    public void setRedissonLockKey(String redissonLockKey) { this.redissonLockKey = redissonLockKey; }

    public BookingOrderResponseModel getBookingOrderResponseModel() { return bookingOrderResponseModel; }
    public void setBookingOrderResponseModel(BookingOrderResponseModel bookingOrderResponseModel) { this.bookingOrderResponseModel = bookingOrderResponseModel; }
}
