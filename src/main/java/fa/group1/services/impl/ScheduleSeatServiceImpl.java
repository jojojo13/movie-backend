package fa.group1.services.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import fa.group1.dto.BookingSeatDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import fa.group1.entities.ScheduleSeat;
import fa.group1.entities.Seat;
import fa.group1.entities.Ticket;
import fa.group1.entities.User;
import fa.group1.exceptions.ResourceNotFoundException;
import fa.group1.repository.ScheduleMovieRepository;
import fa.group1.repository.ScheduleSeatRepository;
import fa.group1.repository.UserRepository;
import fa.group1.services.ScheduleSeatService;
import fa.group1.services.TicketService;
import fa.group1.utils.TokenAuthenticationUtils;

@Service
public class ScheduleSeatServiceImpl implements ScheduleSeatService {

    @Autowired
    private ScheduleSeatRepository scheduleSeatRepository;
    @Autowired
    private ScheduleMovieRepository scheduleMovieRepository;
    @Autowired
    private TicketService ticketService;
    @Autowired
    UserRepository userRepository;

    @Override
    public List<ScheduleSeat> findAllScheduleSeat() {

        return scheduleSeatRepository.findAll();
    }
    @Override
    public List<ScheduleSeat> addNewScheduleSeat(Integer scheduleMovieId, String token, BookingSeatDTO bookingSeat) {
        List<ScheduleSeat> scheduleSeats = new ArrayList<>();
        Authentication authentication = TokenAuthenticationUtils.getAuthentication(token);
        Optional<User> u = userRepository.findByUsername(authentication.getPrincipal().toString());
        if (!u.isPresent()) {
            throw new ResourceNotFoundException("User not found");
        }
        Ticket ticket = ticketService.addTicket(u.get().getAccountId());

        for (Seat seat : bookingSeat.getListSeat()) {
            ScheduleSeat scheduleSeat = new ScheduleSeat();
            scheduleSeat.setSeat(seat.getSeatId());
            scheduleSeat.setTicket(ticket);
            scheduleSeat.setScheduleMovie(scheduleMovieRepository.findById(scheduleMovieId).get());
            scheduleSeats.add(scheduleSeat);
        }
        ticket.setTicketType(0);
        ticket.setPrice(bookingSeat.getPrice());
        return scheduleSeatRepository.saveAll(scheduleSeats);
    }
}
