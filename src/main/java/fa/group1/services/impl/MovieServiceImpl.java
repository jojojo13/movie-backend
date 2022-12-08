package fa.group1.services.impl;

import fa.group1.dto.MovieAddDTO;
import fa.group1.dto.MovieByTypeDTO;
import fa.group1.dto.UserDTO;
import fa.group1.entities.*;
import fa.group1.exceptions.ResourceNotFoundException;
import fa.group1.repository.*;
import fa.group1.services.MovieService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.transaction.Transactional;

@Service
public class MovieServiceImpl implements MovieService {
    @Autowired
    ModelMapper modelMapper;
    @Autowired
    private CinemaRoomRepository cinemaRoomRepository;
    @Autowired
    private ScheduleRepository scheduleRepository;
    @Autowired
    TypeRepository typeRepository;
    @Autowired
    private MovieRepository movieRepository;
    @Autowired
    private ScheduleMovieRepository scheduleMovieRepository;

    @Transactional
    @Override
    public void deleteMovie(Integer movieId) {
        Movie movie = movieRepository.findById(movieId).get();
        movie.getTypes().removeAll(movie.getTypes());
        movieRepository.delete(movie);
    }

    @Override
    public Movie findMovieByID(Integer id) {
        return movieRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Movie not found"));
    }

    @Override
    public Map<String, Object> getAllMovieBySearch(String searchTxt, int page, int size) {
        Map<String, Object> response = new HashMap<>();
        Page<Movie> pageMovies;
        List<Movie> list = new ArrayList<>();

        Pageable sortedByName = PageRequest.of(page, size,
                Sort.by("movie_name_english").descending().and(Sort.by("movie_name_vn").descending()));
        ;
        if (searchTxt.isEmpty()) {
            Pageable paging = PageRequest.of(page, size);
            pageMovies = movieRepository.findAll(paging);
        } else {
            pageMovies = movieRepository.findMovieByName(searchTxt, sortedByName);
        }

        list = pageMovies.getContent();
        response.put("movie", list);
        response.put("currentPage", pageMovies.getNumber());
        response.put("totalItem", pageMovies.getTotalElements());
        response.put("totalPage", pageMovies.getTotalPages());
        return response;

    }

    @Override
    public List<MovieByTypeDTO> getMovieByType(Long typeID) {
        List<MovieByTypeDTO> movieDTOList = movieRepository.getAll(3, typeID)
                .stream()
                .map(movie -> modelMapper.map(movie, MovieByTypeDTO.class))
                .collect(Collectors.toList());
        return movieDTOList;
    }

    @Override
    public Map<String, Object> getAllMovieByPaging(int page, int size) {
        Map<String, Object> response = new HashMap<>();
        Page<Movie> pageMovies;
        List<Movie> list = new ArrayList<>();

        Pageable paging = PageRequest.of(page, size);
        pageMovies = movieRepository.findAll(paging);
        if (pageMovies.isEmpty()) {
            throw new ResourceNotFoundException("Movie is empty!!!");
        }

        list = pageMovies.getContent();
        response.put("movie", list);
        response.put("currentPage", pageMovies.getNumber());
        response.put("totalItem", pageMovies.getTotalElements());
        response.put("totalPage", pageMovies.getTotalPages());
        return response;

    }

    @Override
    public ResponseEntity<?> checkMovie(MovieAddDTO movieDTO, String cinemaRoomId) {
        Map<String, Object> response = new HashMap<>();
        Movie movie = modelMapper.map(movieDTO, Movie.class);
        List<Type> listType = new ArrayList<>();
        movieDTO.getListTypes().forEach(id -> {
            Type type = typeRepository.findById(id).get();
            System.out.println(type.getTypeId() + "--------------");
            listType.add(type);
        });
        movie.setTypes(listType);
        try {

            LocalDate start = movieDTO.getFrom_date();
            LocalDate end = movieDTO.getTo_date();

            int compareValue = start.compareTo(end);
            if (compareValue > 0) {
                response.put("message", "End date is latter than Start date !!!");
                return new ResponseEntity<>(response, HttpStatus.CONFLICT);
            }
//            if (!scheduleRepository.existsById(Integer.valueOf(scheduleId))) {
//                response.put("message", "Schedule is not exists !!!");
//                return new ResponseEntity<>(response, HttpStatus.CONFLICT);
//            }
//            if (!cinemaRoomRepository.existsById(Integer.valueOf(cinemaRoomId))) {
//                response.put("message", "CinemaRoom is not exists !!!");
//                return new ResponseEntity<>(response, HttpStatus.CONFLICT);
//            }

            CinemaRoom cinemaRoom = cinemaRoomRepository.getById(Integer.valueOf(cinemaRoomId));

            Stream.iterate(start, date -> date.plusDays(1)).limit(ChronoUnit.DAYS.between(start, end) + 1)
                    .forEach(t -> {
                        movieRepository.save(movie);
                        movieDTO.getListSchedule().forEach(scheduleId -> {
                            Schedule schedule = scheduleRepository.getById(Integer.valueOf(scheduleId));
                            ScheduleMovie scheduleMovie = new ScheduleMovie();
                            scheduleMovie.setSchedule(schedule);
                            scheduleMovie.setDate(t);
                            scheduleMovie.setCinemaRoom(cinemaRoom);
                            scheduleMovie.setMovie(movie);
                            scheduleMovieRepository.save(scheduleMovie);
                        });

                    });
            response.put("message", "Create successfully!!!");
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("-------------------------------------sads" + movie.getMovieId());
            deleteMovie(movie.getMovieId());
//			movieRepository.delete(movie);
            response.put("message", "Duplicate show date!!!");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

    }

    @Override
    public ResponseEntity<?> findMovieById(Integer movieId) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (!movieRepository.existsById(movieId)) {
                response.put("message", "Movie is not exists !!!");
                return new ResponseEntity<>(response, HttpStatus.CONFLICT);
            }
            Movie movie = movieRepository.findById(movieId).get();
            System.out.println("-----------------------");
            System.out.println(movie);
            return new ResponseEntity<>(movie, HttpStatus.OK);
        } catch (Exception e) {
            response.put("message", "BAD REQUEST!!!");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

    }

}
