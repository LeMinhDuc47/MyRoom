package com.myroom.bookingservice.usecase.pipeline;

import com.myroom.bookingservice.api.model.BookingOrderResponseModel;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class BookingPipeline {
    private final List<BookingFilter> filters;

    public BookingPipeline(List<BookingFilter> filters) {
        this.filters = filters;
    }

    public BookingOrderResponseModel execute(BookingContext context) {
        BookingFilterChain chain = new BookingFilterChain(filters);
        return chain.doFilter(context);
    }
}
