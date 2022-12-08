package fa.group1.services.impl;

import fa.group1.entities.Role;
import fa.group1.entities.User;
import fa.group1.exceptions.ResourceNotFoundException;
import fa.group1.exceptions.UniqueConstraintException;
import fa.group1.payload.request.LoginRequest;
import fa.group1.repository.RoleRepository;
import fa.group1.repository.UserRepository;
import fa.group1.utils.JwtUtils;
import fa.group1.security.services.UserDetailsImpl;
import fa.group1.services.AuthService;
import fa.group1.utils.TokenAuthenticationUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class AuthServiceImpl implements AuthService {
    @Autowired
    AuthenticationManager authenticationManager;
    @Autowired
    UserRepository userRepository;
    @Autowired
    RoleRepository roleRepository;
    @Autowired
    PasswordEncoder encoder;

    @Override
    public Map<String, Object> signIn(LoginRequest loginRequest) {

        Map<String, Object> responseJwt = new HashMap<>();
        Map<String, Object> userInfo = new HashMap<>();
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = JwtUtils.generateJwtToken(authentication);
        responseJwt.put("token", jwt);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        String role = userDetails.getAuthoritity().getAuthority();
        userInfo.put("username", userDetails.getUsername());
        userInfo.put("id", userDetails.getId());
        userInfo.put("role", role);
        responseJwt.put("data", userInfo);
        return responseJwt;
    }

    @Override
    public Map<String, Object> signUp(User user) {
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
        Optional<Role> role = roleRepository.findByRoleName("Customer");
        String encodePwd = encoder.encode(user.getPassword());
        user.setRegisterDate(LocalDate.now());
        user.setStatus(1);
        user.setPassword(encodePwd);
        userRepository.save(user);
        response.put("message", "Signup successfully!!!");
        return response;
    }

    @Override
    public Map<String, Object> getUserInfoByToken(String token) {
        Map<String, Object> response = new HashMap<>();
        Authentication authentication = TokenAuthenticationUtils.getAuthentication(token);
        Optional<User> u = userRepository.findByUsername(authentication.getPrincipal().toString());
        if(!u.isPresent()){
            throw new ResourceNotFoundException("User not found");
        }
        response.put("id", u.get().getAccountId());
        response.put("username", u.get().getUsername());
        response.put("role", u.get().getRole().getRoleName());
        return response;


    }
}
