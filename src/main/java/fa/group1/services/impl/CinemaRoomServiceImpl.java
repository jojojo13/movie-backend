package fa.group1.services.impl;

import fa.group1.entities.CinemaRoom;
import fa.group1.entities.Seat;
import fa.group1.exceptions.ResourceNotFoundException;
import fa.group1.repository.CinemaRoomRepository;
import fa.group1.repository.SeatRepository;
import fa.group1.services.CinemaRoomService;
import lombok.var;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CinemaRoomServiceImpl implements CinemaRoomService {
    @Autowired
    SeatRepository seatRepository;
    @Autowired
    CinemaRoomRepository cinemaRoomRepository;

    @Override
    public Map<String, Object> getAllCinemaRoom(int page, int size) {

        Map<String, Object> response = new HashMap<>();
        List<CinemaRoom> list = new ArrayList<>();
        Page<CinemaRoom> pageCinemas;

        Pageable paging = PageRequest.of(page, size);
        pageCinemas = cinemaRoomRepository.findAll(paging);

        list = pageCinemas.getContent();
        if(list.isEmpty()){
            throw new ResourceNotFoundException("Not found any cinema");
        }
        response.put("cinemas", list);
        response.put("currentPage", pageCinemas.getNumber());
        response.put("totalItem", pageCinemas.getTotalElements());
        response.put("totalPage", pageCinemas.getTotalPages());
        return response;

    }

    @Override
    public List<Seat> getAllSeatByCinemaID(Integer cinemaID) {

        List<Seat> seats = seatRepository.findByCinemaRoomID(cinemaID);
        if (seats.isEmpty()) {
            throw new ResourceNotFoundException("No seat in this cinema room");
        }
        return seats;
    }

    @Override
    public Map<String, Object> updateSeatInCinemaRoom(List<Seat> list) {
        Map<String, Object> response = new HashMap<>();

        seatRepository.saveAll(list);
        response.put("message", "Updated successfully");
        return response;


    }
}
