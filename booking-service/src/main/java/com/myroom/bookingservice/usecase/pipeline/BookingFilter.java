package com.myroom.bookingservice.usecase.pipeline;

import com.myroom.bookingservice.api.model.BookingOrderResponseModel;

/**
 * A single processing step in the booking pipeline.
 */
public interface BookingFilter {
    BookingOrderResponseModel execute(BookingContext context, BookingFilterChain chain);
}
