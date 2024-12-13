package com.hotelprject.hotelproject.service;

import com.hotelprject.hotelproject.model.Reservation;
import com.hotelprject.hotelproject.model.Room;
import com.hotelprject.hotelproject.repository.ReservationRepository;
import com.hotelprject.hotelproject.repository.RoomRepository;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReservationServiceImpl implements ReservationService {

    private final ReservationRepository reservationRepository;
    private final RoomRepository roomRepository;

    @Override
    public Reservation makeReservation(Long roomId, LocalDate reservationDate, LocalDate endDate, String user) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found with id: " + roomId));

        // Odanın müsaitlik durumunu güncelle
        room.setAvailable(false);
        roomRepository.save(room);

        // Yeni rezervasyon oluştur
        Reservation reservation = new Reservation();
        reservation.setRoom(room);
        reservation.setReservationDate(reservationDate);
        reservation.setEndDate(endDate);
        reservation.setUserInfo(user);
        reservation.setStatus("ONAYLANDI");

        return reservationRepository.save(reservation);
    }

    @Override
    public List<Reservation> findAllReservations() {
        return reservationRepository.findAll();
    }

    @Override
    public Optional<Reservation> findById(Long reservationId) {
        return reservationRepository.findById(reservationId);
    }

    @Override
    public void cancelReservation(Long reservationId, String username) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Rezervasyon bulunamadı"));

        // Kullanıcı kontrolü
        if (!reservation.getUserInfo().equals(username)) {
            throw new RuntimeException("Bu rezervasyonu iptal etme yetkiniz yok");
        }

        cancelReservationProcess(reservation);
    }

    @Override
    public void cancelReservation(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Rezervasyon bulunamadı"));

        cancelReservationProcess(reservation);
    }

    // Yardımcı metod - rezervasyon iptal işlemleri
    private void cancelReservationProcess(Reservation reservation) {
        // Odayı tekrar müsait yap
        Room room = reservation.getRoom();
        room.setAvailable(true);
        roomRepository.save(room);

        // Rezervasyon durumunu güncelle
        reservation.setStatus("İPTAL EDİLDİ");
        reservationRepository.save(reservation);
    }
}