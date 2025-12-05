package com.myroom.bookingservice.usecase.pipeline;

import com.myroom.bookingservice.api.model.BookingOrderResponseModel;

import java.util.List;

/**
 * Maintains the list of filters and the current execution index.
 */
public class BookingFilterChain {
    private final List<BookingFilter> filters;
    private int index = 0;

    public BookingFilterChain(List<BookingFilter> filters) {
        this.filters = filters;
    }

    public BookingOrderResponseModel doFilter(BookingContext context) {
        if (index < filters.size()) {
            BookingFilter next = filters.get(index++);
            return next.execute(context, this);
        }
        return context.getBookingOrderResponseModel();
    }
}
