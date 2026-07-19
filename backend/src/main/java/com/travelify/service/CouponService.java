package com.travelify.service;

import com.travelify.exception.InvalidCouponException;
import com.travelify.model.Coupon;
import com.travelify.repository.CouponRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class CouponService {

    private final CouponRepository couponRepository;

    public BigDecimal validateCoupon(String code, BigDecimal totalAmount) {
        Coupon coupon = couponRepository.findByCode(code)
                .orElseThrow(() -> new InvalidCouponException("Coupon not found."));

        if (!coupon.getIsActive()) {
            throw new InvalidCouponException("Coupon is not active.");
        }

        LocalDate today = LocalDate.now();
        if (coupon.getValidFrom() != null && today.isBefore(coupon.getValidFrom())) {
            throw new InvalidCouponException("Coupon is not yet valid.");
        }
        if (coupon.getValidTo() != null && today.isAfter(coupon.getValidTo())) {
            throw new InvalidCouponException("Coupon has expired.");
        }

        if (totalAmount.compareTo(coupon.getMinSpend()) < 0) {
            throw new InvalidCouponException("Minimum spend of " + coupon.getMinSpend() + " not met.");
        }

        if (coupon.getUsageLimit() != null && coupon.getUsedCount() >= coupon.getUsageLimit()) {
            throw new InvalidCouponException("Coupon usage limit reached.");
        }

        BigDecimal discountAmount;
        if (coupon.getDiscountType() == Coupon.DiscountType.PERCENTAGE) {
            discountAmount = totalAmount.multiply(coupon.getDiscountValue().divide(BigDecimal.valueOf(100)));
            if (coupon.getMaxDiscount() != null && discountAmount.compareTo(coupon.getMaxDiscount()) > 0) {
                discountAmount = coupon.getMaxDiscount();
            }
        } else { // FIXED
            discountAmount = coupon.getDiscountValue();
        }

        return discountAmount;
    }

    @Transactional
    public void incrementUsage(String code) {
        Coupon coupon = couponRepository.findByCode(code)
                .orElseThrow(() -> new InvalidCouponException("Coupon not found."));
        coupon.setUsedCount(coupon.getUsedCount() + 1);
        couponRepository.save(coupon);
    }
}
