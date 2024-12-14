package com.hotelprject.hotelproject.controller;

import com.hotelprject.hotelproject.model.HotelUser;
import com.hotelprject.hotelproject.model.Reservation;
import com.hotelprject.hotelproject.model.Room;
import com.hotelprject.hotelproject.model.dto.ReservationDto;
import com.hotelprject.hotelproject.service.AdminService;
import com.hotelprject.hotelproject.service.HotelUserService;
import com.hotelprject.hotelproject.service.ReservationService;
import com.hotelprject.hotelproject.service.RoomService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final HotelUserService hotelUserService;
    private final ReservationService reservationService;
    private final RoomService roomService;

    @GetMapping("/admin/login")
    public String adminLoginPage() {
        return "adminLogin";
    }

    @PostMapping("/admin/login")
    public String adminLogin(@RequestParam String email,
                             @RequestParam String password,
                             HttpSession session,
                             Model model) {
        Optional<HotelUser> adminUserOptional = hotelUserService.findAdminUserByEmailAndPassword(email, password);

        if (adminUserOptional.isPresent()) {
            session.setAttribute("loggedInAdmin", email);
            return "redirect:/admin/panel";
        } else {
            model.addAttribute("loginError", "Email veya şifre yanlış. Lütfen tekrar deneyin!");
            return "adminLogin";
        }
    }

    @GetMapping("/admin/panel")
    public String adminPanel(HttpSession session, Model model) {
        String adminEmail = (String) session.getAttribute("loggedInAdmin");
        if (adminEmail == null) {
            return "redirect:/admin/login";
        }
        model.addAttribute("loggedInAdmin", adminEmail);
        return "adminPanel";
    }

    @PostMapping("/admin/logout")
    public String adminLogout(HttpSession session) {
        session.invalidate();
        return "redirect:/admin/login";
    }

    @GetMapping("/admin/manage-users")
    public String manageUsers(HttpSession session, Model model) {
        String adminEmail = (String) session.getAttribute("loggedInAdmin");
        if (adminEmail == null) {
            return "redirect:/admin/login";
        }
        model.addAttribute("users", hotelUserService.findAll());
        return "manage-users";
    }

    @GetMapping("/admin/manage-reservations")
    public String manageReservations(Model model, HttpSession session) {
        String adminEmail = (String) session.getAttribute("loggedInAdmin");
        if (adminEmail == null) {
            return "redirect:/admin/login";
        }

        try {
            List<ReservationDto> reservationDtoList = reservationService.findAllReservations()
                    .stream()
                    .map(r -> ReservationDto.builder()
                            .id(r.getId())
                            .fullName(r.getUserInfo())
                            .roomNumber(r.getRoom().getId())
                            .checkInDate(r.getReservationDate())
                            .checkOutDate(r.getEndDate())
                            .totalPrice(r.getRoom().getPrice() *
                                    ChronoUnit.DAYS.between(r.getReservationDate(), r.getEndDate()))
                            .status(r.getStatus())
                            .build())
                    .toList();

            model.addAttribute("reservations", reservationDtoList);
            return "manage-reservations";
        } catch (Exception e) {
            model.addAttribute("error", "Rezervasyonları yüklerken bir hata oluştu: " + e.getMessage());
            return "error";
        }
    }

    @GetMapping("/admin/edit-reservation/{id}")
    public String showEditReservationForm(@PathVariable("id") Long reservationId,
                                          Model model,
                                          HttpSession session) {
        String adminEmail = (String) session.getAttribute("loggedInAdmin");
        if (adminEmail == null) {
            return "redirect:/admin/login";
        }

        Optional<Reservation> reservation = reservationService.findById(reservationId);
        if (reservation.isEmpty()) {
            return "redirect:/admin/manage-reservations";
        }

        List<Room> allRooms = roomService.getAllRooms();
        model.addAttribute("reservation", reservation.get());
        model.addAttribute("rooms", allRooms);
        return "admin/edit-reservation";
    }

    @PostMapping("/admin/update-reservation/{id}")
    public String updateReservation(@PathVariable("id") Long reservationId,
                                    @RequestParam("roomId") Long roomId,
                                    @RequestParam("reservationDate") String reservationDate,
                                    @RequestParam("endDate") String endDate,
                                    HttpSession session) {
        String adminEmail = (String) session.getAttribute("loggedInAdmin");
        if (adminEmail == null) {
            return "redirect:/admin/login";
        }

        try {
            Optional<Reservation> reservationOpt = reservationService.findById(reservationId);
            if (reservationOpt.isEmpty()) {
                return "redirect:/admin/manage-reservations";
            }

            reservationService.cancelReservation(reservationId);

            LocalDate startDate = LocalDate.parse(reservationDate);
            LocalDate end = LocalDate.parse(endDate);

            Reservation oldReservation = reservationOpt.get();
            reservationService.makeReservation(roomId, startDate, end, oldReservation.getUserInfo());

            return "redirect:/admin/manage-reservations";
        } catch (Exception e) {
            return "redirect:/admin/manage-reservations?error=true";
        }
    }

    @PostMapping("/admin/delete-reservation/{id}")
    public String deleteReservation(@PathVariable("id") Long reservationId,
                                    HttpSession session) {
        String adminEmail = (String) session.getAttribute("loggedInAdmin");
        if (adminEmail == null) {
            return "redirect:/admin/login";
        }

        try {
            reservationService.cancelReservation(reservationId);
            return "redirect:/admin/manage-reservations?success=true";
        } catch (Exception e) {
            return "redirect:/admin/manage-reservations?error=true";
        }
    }
}