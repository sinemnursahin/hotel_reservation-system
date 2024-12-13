package com.hotelprject.hotelproject.repository;
import com.hotelprject.hotelproject.model.Reservation;
import com.hotelprject.hotelproject.model.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findByReservationDate(LocalDate reservationDate);
    Optional<Reservation> findByRoomAndReservationDate(Room room, LocalDate reservationDate);
}