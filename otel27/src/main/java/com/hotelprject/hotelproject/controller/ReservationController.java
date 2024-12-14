package com.hotelprject.hotelproject.controller;

import com.hotelprject.hotelproject.model.Reservation;
import com.hotelprject.hotelproject.model.Room;
import com.hotelprject.hotelproject.model.RoomProperty;
import com.hotelprject.hotelproject.service.ReservationService;
import com.hotelprject.hotelproject.service.RoomService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Controller
@RequiredArgsConstructor
public class ReservationController {
    private final RoomService roomService;
    private final ReservationService reservationService;

    @GetMapping("/reserve")
    public String showReservationPage(@RequestParam("roomId") Long roomId, Model model) {
        try {
            Room room = roomService.getRoomById(roomId);
            if (room == null) {
                model.addAttribute("error", "Oda bulunamadı!");
                return "error";
            }

            model.addAttribute("room", room);
            model.addAttribute("properties", room.getRoomProperties().stream()
                    .map(RoomProperty::getProperty)
                    .toList());
            return "reserve";
        } catch (Exception e) {
            model.addAttribute("error", "Rezervasyon sayfası yüklenirken hata: " + e.getMessage());
            return "error";
        }
    }

    @PostMapping("/reserve")
    public String reserveRoom(@RequestParam("roomId") Long roomId,
                              @RequestParam("reservationDate") String reservationDate,
                              @RequestParam("endDate") String endDate,
                              @RequestParam("numOfPeople") int numOfPeople,
                              HttpSession session,
                              Model model) {
        try {
            String username = (String) session.getAttribute("loggedInUser");
            if (username == null) {
                return "redirect:/login";
            }

            Room room = roomService.getRoomById(roomId);
            if (room == null) {
                model.addAttribute("error", "Oda bulunamadı!");
                return "error";
            }

            LocalDate startDate = LocalDate.parse(reservationDate);
            LocalDate endDate2 = LocalDate.parse(endDate);
            Double totalPrice = room.getPrice() * ChronoUnit.DAYS.between(startDate, endDate2);

            // Rezervasyonu oluştur
            Reservation reservation = reservationService.makeReservation(
                    roomId,
                    startDate,
                    endDate2,
                    username
            );

            // Ödeme sayfası için gerekli bilgileri ekle
            model.addAttribute("reservation", reservation);
            model.addAttribute("room", room);
            model.addAttribute("checkInDate", startDate);
            model.addAttribute("checkOutDate", endDate2);
            model.addAttribute("numOfPeople", numOfPeople);
            model.addAttribute("totalPrice", totalPrice);
            model.addAttribute("customerName", username);

            return "payment";
        } catch (Exception e) {
            model.addAttribute("error", "Rezervasyon oluşturulurken hata: " + e.getMessage());
            return "error";
        }
    }

    @GetMapping("/cancel-reservation")
    public String showCancelReservationPage(Model model) {
        return "cancel-reservation";
    }

    @PostMapping("/cancel-reservation")
    public String cancelReservation(@RequestParam("reservationId") Long reservationId,
                                    HttpSession session,
                                    Model model) {
        try {
            String username = (String) session.getAttribute("loggedInUser");
            if (username == null) {
                return "redirect:/login";
            }

            reservationService.cancelReservation(reservationId);
            model.addAttribute("message", "Rezervasyon başarıyla iptal edildi.");
            return "redirect:/home";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "cancel-reservation";
        }
    }
}