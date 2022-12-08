package fa.group1.services.impl;

import fa.group1.dto.UserDTO;
import fa.group1.entities.Role;
import fa.group1.entities.User;
import fa.group1.exceptions.ResourceNotFoundException;
import fa.group1.exceptions.UniqueConstraintException;
import fa.group1.repository.RoleRepository;
import fa.group1.repository.UserRepository;
import fa.group1.services.UserDetailsService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.beans.Encoder;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserDetailsService {

	@Autowired
	UserRepository userRepository;
	@Autowired
	ModelMapper modelMapper;
	@Autowired
	RoleRepository roleRepository;

	@Autowired
	PasswordEncoder encoder;
	@Override
	public Map<String, Object> getBookingHistory(int page, int size) {
		return null;
	}

	@Override
	public List<User> getAllEmployeeList(Pageable pageable) {
		List<User> listEmployee= userRepository.getAllEmployee();

		if(listEmployee.isEmpty()){
			throw new ResourceNotFoundException("Not found any employee!!!");
		}
		return  listEmployee;
	}

	@Override
	public List<User> getAllUser() {
		List<User> list = userRepository.findAll();
		if (list.isEmpty()) {
			throw new ResourceNotFoundException("Not found any user!!!");
		}
		return list;
	}

	@Override
	public User findUserByID(Integer id) {
		// TODO Auto-generated method stub
		return userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("No user match this id"));
	}

	@Override
	public Map<String, Object> updateUser(UserDTO userDTO) {
		Map<String, Object> response = new HashMap<>();
		User user = modelMapper.map(userDTO, User.class);
		userRepository.save(user);
		response.put("message", "updated successfully");
		return response;

	}

	@Override
	public Map<String, Object> addUserAsEmployee(User user) {
		Map<String, Object> response = new HashMap<>();
		if (userRepository.findByUsername(user.getUsername()).isPresent()) {
			throw new UniqueConstraintException("Username is exsisted");
		}
		if (userRepository.findByEmail(user.getEmail()).isPresent()) {
			throw new UniqueConstraintException("Email is exsisted");
		}
		if (userRepository.findByIdentityCard(user.getIdentityCard()).isPresent()) {
			throw new UniqueConstraintException("Identity card is exsisted");
		}
		Optional<Role> role = roleRepository.findByRoleName("Employee");
		String encodePwd = encoder.encode(user.getPassword());
		user.setRegisterDate(LocalDate.now());
		user.setStatus(1);
		user.setPassword(encodePwd);
		userRepository.save(user);
		response.put("message", "Add employee successfully!!!");
		return response;
	}
}
