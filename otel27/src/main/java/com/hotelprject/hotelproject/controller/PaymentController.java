package com.hotelprject.hotelproject.controller;
import com.hotelprject.hotelproject.model.Reservation;
import com.hotelprject.hotelproject.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.time.LocalDate;
@Controller
@RequiredArgsConstructor
public class PaymentController {
    private final ReservationService reservationService;
    @PostMapping("/reserve/process-payment")
    public String processPayment(
            @RequestParam(name = "roomId") Long roomId,
            @RequestParam(name = "cardHolderName") String cardHolderName,
            @RequestParam(name = "cardNumber") String cardNumber,
            @RequestParam(name = "expiryDate") String expiryDate,
            @RequestParam(name = "cvv") String cvv,
            @RequestParam(name = "reservationDate") String reservationDate,
            @RequestParam(name = "endDate") String endDate
    ) {
        reservationService.makeReservation(roomId, LocalDate.parse(reservationDate), LocalDate.parse(endDate), cardHolderName);
        // Redirect to a confirmation page or success message
        return "payment-success";
    }
}
