package fa.group1.services.impl;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import fa.group1.entities.User;
import fa.group1.exceptions.ResourceNotFoundException;
import fa.group1.utils.TokenAuthenticationUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import fa.group1.dto.BookedTicketDTO;
import fa.group1.entities.Ticket;
import fa.group1.repository.TicketRepository;
import fa.group1.repository.UserRepository;
import fa.group1.services.TicketService;

@Service
public class TicketServiceImpl implements TicketService {

	@Autowired
	private TicketRepository ticketRepository;

	@Autowired
	private UserRepository userRepository;

	@Override
	public List<Object> findAllTicket() {
		return ticketRepository.findAllTicket();
	}

	@Override
	public Ticket addTicket(Integer accountId) {
		Ticket ticket = new Ticket();
		ticket.setBookingDate(LocalDate.now());
		ticket.setUser(userRepository.findById(accountId).get());
		return ticketRepository.save(ticket);
	}

	@Override
	public List<BookedTicketDTO> getAllBookedTicketDTOs(String token) {
		Authentication authentication = TokenAuthenticationUtils.getAuthentication(token);
		Optional<User> u = userRepository.findByUsername(authentication.getPrincipal().toString());
		if(!u.isPresent()){
			throw new ResourceNotFoundException("User not found");
		}
		return ticketRepository.getAllBookedTicket(u.get().getAccountId());
	}

}
