package com.myroom.bookingservice.usecase.pipeline.filters;

import com.myroom.bookingservice.api.constants.ApiConstants;
import com.myroom.bookingservice.api.model.BookingOrderResponseModel;
import com.myroom.bookingservice.exception.BookingServiceRuntimeException;
import com.myroom.bookingservice.usecase.pipeline.BookingContext;
import com.myroom.bookingservice.usecase.pipeline.BookingFilter;
import com.myroom.bookingservice.usecase.pipeline.BookingFilterChain;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@Order(2)
@Slf4j
public class RoomLockFilter implements BookingFilter {

    private final RedissonClient redissonClient;

    public RoomLockFilter(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    @Override
    public BookingOrderResponseModel execute(BookingContext context, BookingFilterChain chain) {
        var bookingRequestDetails = context.getBookingRequestDetails();
        String lockKey = "lock:room:" + bookingRequestDetails.getRoomId();
        RLock lock = redissonClient.getLock(lockKey);
        boolean locked = false;
        try {
            locked = lock.tryLock(5, 20, TimeUnit.SECONDS);
            if (!locked) {
                log.warn("Could not acquire lock for key: {}", lockKey);
                throw new BookingServiceRuntimeException(
                        "The room is currently being booked by another user. Please try again.");
            }
            context.setRedissonLock(lock);
            context.setRedissonLockKey(lockKey);
            return chain.doFilter(context);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            throw new BookingServiceRuntimeException("Interrupted while acquiring booking lock");
        } finally {
            if (locked) {
                try {
                    lock.unlock();
                } catch (Exception ex) {
                    log.warn("Failed to unlock key {}: {}", lockKey, ex.getMessage());
                }
            }
        }
    }
}
