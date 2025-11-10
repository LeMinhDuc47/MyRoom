package com.myroom.bookingservice.usecase.impl;

import com.myroom.bookingservice.api.model.BookingDataResponseModel;
import com.myroom.bookingservice.api.model.dashboard.BookingsCountStatisticsResponseModel;
import com.myroom.bookingservice.api.model.dashboard.BookingsRecordsResponseModel;
import com.myroom.bookingservice.api.model.dashboard.BookingsStatisticsResponseModel;
import com.myroom.bookingservice.api.model.dashboard.PeopleCountsResponseModel;
import com.myroom.bookingservice.data.entity.BookingDetails;
import com.myroom.bookingservice.data.mapper.BookingDashboardMapper;
import com.myroom.bookingservice.repository.BookingRepository;
import com.myroom.bookingservice.usecase.BookingDashboardService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.Date;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@Slf4j
public class BookingDashboardServiceImpl implements BookingDashboardService {
    @Autowired
    BookingRepository bookingRepository;

    @Autowired
    BookingDashboardMapper bookingDashboardMapper;

    @Override
    public BookingsStatisticsResponseModel getBookingStatistics(String organizationId, String startDate,
            String endDate) {
        log.info("Fetching the bookings statistics");
        Long total = bookingRepository.countBookingsByOrganizationIdAndDateRange(organizationId,
                Instant.parse(startDate), Instant.parse(endDate));
        List<Object[]> data = bookingRepository.countBookingsByOrganizationIdAndDateRangeGroupByDate(organizationId,
                Instant.parse(startDate), Instant.parse(endDate));
        Long totalBookings = bookingRepository.getTotalBookingByOrganizationId(organizationId);

        Map<LocalDate, Long> resultMap = new HashMap<>();
        for (Object[] row : data) {
            LocalDate date = ((Date) row[0]).toLocalDate();
            Long count = (Long) row[1];
            resultMap.put(date, count);
        }

        LocalDate currentDate = Instant.parse(startDate).atZone(ZoneId.systemDefault()).toLocalDate();
        List<Long> counts = new ArrayList<>();
        while (!currentDate.isAfter(Instant.parse(endDate).atZone(ZoneId.systemDefault()).toLocalDate())) {
            counts.add(resultMap.getOrDefault(currentDate, 0L));
            currentDate = currentDate.plusDays(1);
        }

        BookingsStatisticsResponseModel bookingsStatisticsResponseModel = BookingsStatisticsResponseModel.builder()
                .totalBookings(totalBookings)
                .total(total)
                .data(counts)
                .build();

        log.info("Fetched booking statistics: {}", bookingsStatisticsResponseModel);
        return bookingsStatisticsResponseModel;
    }

    @Override
    public BookingsCountStatisticsResponseModel handleGetAllBookings(String organizationId, String startDate,
            String endDate) {
        log.info("Fetching the booking count");
        Long checkIn = bookingRepository.countBookingsByOrganizationIdAndCheckInDateRange(organizationId,
                Instant.parse(startDate), Instant.parse(endDate));
        Long checkOut = bookingRepository.countBookingsByOrganizationIdAndCheckOutDateRange(organizationId,
                Instant.parse(startDate), Instant.parse(endDate));

        BookingsCountStatisticsResponseModel BookingsCountStatisticsResponseModel = com.myroom.bookingservice.api.model.dashboard.BookingsCountStatisticsResponseModel
                .builder()
                .checkIn(checkIn)
                .checkOut(checkOut)
                .build();

        log.info("Fetched booking count: {}", BookingsCountStatisticsResponseModel);
        return BookingsCountStatisticsResponseModel;
    }

    @Override
    public PeopleCountsResponseModel handleGetPeopleCounts(String organizationId) {
        log.info("Fetching the people counts");
        Object[] data = bookingRepository.countUniqueByPeopleCountByOrganizationId(organizationId);
        Map<String, Long> mapped = new HashMap<>();
        long single = 0L;
        long dbl = 0L;
        long others = 0L;

        if (data != null) {
            // Case 1: direct numbers in array [single, double, others]
            if (data.length >= 3 && data[0] instanceof Number) {
                single = toLong(data[0]);
                dbl = toLong(data[1]);
                others = toLong(data[2]);
            } else if (data.length == 1) {
                // Some JPA implementations may return a single element which is Object[] or a
                // Map
                Object first = data[0];
                if (first instanceof Object[]) {
                    Object[] row = (Object[]) first;
                    if (row.length >= 3) {
                        single = toLong(row[0]);
                        dbl = toLong(row[1]);
                        others = toLong(row[2]);
                    }
                } else if (first instanceof java.util.Map) {
                    java.util.Map<?, ?> m = (java.util.Map<?, ?>) first;
                    // try keys by alias or take first three numeric values
                    if (m.containsKey("single") || m.containsKey("double") || m.containsKey("others")) {
                        single = toLong(m.get("single"));
                        dbl = toLong(m.get("double"));
                        others = toLong(m.get("others"));
                    } else {
                        int idx = 0;
                        for (Object val : m.values()) {
                            if (idx == 0)
                                single = toLong(val);
                            else if (idx == 1)
                                dbl = toLong(val);
                            else if (idx == 2) {
                                others = toLong(val);
                                break;
                            }
                            idx++;
                        }
                    }
                }
            } else if (data.length > 0 && data[0] instanceof java.util.Map) {
                // sometimes returns array of maps
                java.util.Map<?, ?> m = (java.util.Map<?, ?>) data[0];
                if (m.containsKey("single") || m.containsKey("double") || m.containsKey("others")) {
                    single = toLong(m.get("single"));
                    dbl = toLong(m.get("double"));
                    others = toLong(m.get("others"));
                } else {
                    int idx = 0;
                    for (Object val : m.values()) {
                        if (idx == 0)
                            single = toLong(val);
                        else if (idx == 1)
                            dbl = toLong(val);
                        else if (idx == 2) {
                            others = toLong(val);
                            break;
                        }
                        idx++;
                    }
                }
            }
        }

        mapped.put("single", single);
        mapped.put("double", dbl);
        mapped.put("others", others);
        log.info("Fetched people counts: {}", mapped);
        return bookingDashboardMapper.toPeopleCountsResponseModel(mapped);
    }

    private long toLong(Object o) {
        if (o == null)
            return 0L;
        if (o instanceof Number)
            return ((Number) o).longValue();
        try {
            return Long.parseLong(o.toString());
        } catch (Exception ex) {
            return 0L;
        }
    }

    @Override
    public BookingsRecordsResponseModel handleGetBookingRecords(String organizationId, String startDate,
            String endDate) {
        log.info("Fetching the booking records");
        List<BookingDetails> bookings = bookingRepository.findByOrganizationIdAndCreatedAtBetween(organizationId,
                Instant.parse(startDate), Instant.parse(endDate).plus(1, ChronoUnit.DAYS));
        List<BookingDataResponseModel> bookingsRecord = bookings.stream()
                .map(booking -> bookingDashboardMapper.toBookingDataResponseModel(booking))
                .collect(Collectors.toList());
        return bookingDashboardMapper.toBookingsRecordsResponseModel(bookings.size(), bookingsRecord);
    }
}
