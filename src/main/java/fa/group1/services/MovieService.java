package fa.group1.services;

import fa.group1.dto.MovieByTypeDTO;
import fa.group1.entities.Movie;
import org.springframework.http.ResponseEntity;

import fa.group1.dto.MovieAddDTO;

import java.util.List;
import java.util.Map;

public interface MovieService {
	Map<String, Object> getAllMovieByPaging(int page, int size);
	ResponseEntity<?> checkMovie(MovieAddDTO movie, String cinemaRoomId);
	ResponseEntity<?> findMovieById(Integer movieId);
	void deleteMovie(Integer movieId);
	Movie findMovieByID(Integer id);
	Map<String,Object> getAllMovieBySearch(String searchTxt,int page,int size);
	List<MovieByTypeDTO> getMovieByType(Long typeID);
}
