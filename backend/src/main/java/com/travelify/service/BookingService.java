package com.travelify.service;

import com.travelify.dto.BookingDtos;
import com.travelify.exception.ApiException;
import com.travelify.model.Booking;
import com.travelify.model.BookingStatus;
import com.travelify.model.TravelPackage;
import com.travelify.model.User;
import com.travelify.repository.BookingRepository;
import com.travelify.repository.TravelPackageRepository;
import com.travelify.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class BookingService {
    private final BookingRepository bookingRepository;
    private final TravelPackageRepository packageRepository;
    private final UserRepository userRepository;

    public BookingService(BookingRepository bookingRepository,
                          TravelPackageRepository packageRepository,
                          UserRepository userRepository) {
        this.bookingRepository = bookingRepository;
        this.packageRepository = packageRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public BookingDtos.BookingResponse create(BookingDtos.BookingRequest request, String customerEmail) {
        User customer = userRepository.findByEmail(customerEmail)
                .orElseThrow(() -> new ApiException("User not found", HttpStatus.NOT_FOUND));
        TravelPackage travelPackage = packageRepository.findById(request.getPackageId())
                .orElseThrow(() -> new ApiException("Package not found", HttpStatus.NOT_FOUND));
        if (!Boolean.TRUE.equals(travelPackage.getActive())) {
            throw new ApiException("Package is not available", HttpStatus.BAD_REQUEST);
        }
        BigDecimal total = travelPackage.getPrice().multiply(BigDecimal.valueOf(request.getTravelers()));
        Booking booking = bookingRepository.save(Booking.builder()
                .customer(customer)
                .travelPackage(travelPackage)
                .travelDate(request.getTravelDate())
                .travelers(request.getTravelers())
                .totalPrice(total)
                .status(BookingStatus.PENDING)
                .build());
        return toResponse(booking);
    }

    public List<BookingDtos.BookingResponse> forCustomer(String email) {
        User customer = userRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException("User not found", HttpStatus.NOT_FOUND));
        return bookingRepository.findByCustomer(customer).stream().map(this::toResponse).toList();
    }

    public List<BookingDtos.BookingResponse> all() {
        return bookingRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional
    public BookingDtos.BookingResponse updateStatus(Long id, BookingStatus status) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ApiException("Booking not found", HttpStatus.NOT_FOUND));
        booking.setStatus(status);
        return toResponse(booking);
    }

    private BookingDtos.BookingResponse toResponse(Booking booking) {
        return BookingDtos.BookingResponse.builder()
                .id(booking.getId())
                .packageId(booking.getTravelPackage().getId())
                .packageTitle(booking.getTravelPackage().getTitle())
                .destination(booking.getTravelPackage().getDestination())
                .travelDate(booking.getTravelDate())
                .travelers(booking.getTravelers())
                .totalPrice(booking.getTotalPrice())
                .status(booking.getStatus())
                .customerEmail(booking.getCustomer().getEmail())
                .build();
    }
}