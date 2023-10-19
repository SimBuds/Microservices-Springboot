package ca.georgebrown.userservice.service;

import ca.georgebrown.userservice.dto.UserRequest;
import ca.georgebrown.userservice.dto.UserResponse;
import ca.georgebrown.userservice.model.User;
import ca.georgebrown.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    @Override
    @Transactional
    public void createUser(UserRequest userRequest) {
        User user = new User();
        if (userRepository.getUserByUsername(userRequest.getUsername()) != null) {
            logger.error("User already exists with username: {}", userRequest.getUsername());
            throw new RuntimeException("User already exists");
        }
        user.setUsername(userRequest.getUsername());
        user.setPassword(passwordEncoder.encode(userRequest.getPassword()));
        user.setEmail(userRequest.getEmail());
        user.setFullName(userRequest.getFullName());
        userRepository.save(user);
    }

    @Override
    @Transactional
    public String updateUser(String username, UserRequest userRequest) {
        User user = userRepository.getUserByUsername(username);
        if (user == null) {
            logger.error("User not found with username: {}", username);
            throw new UsernameNotFoundException("User not found");
        }

        user.setUsername(userRequest.getUsername());
        user.setPassword(passwordEncoder.encode(userRequest.getPassword()));
        user.setEmail(userRequest.getEmail());
        user.setFullName(userRequest.getFullName());
        userRepository.save(user);
        return user.getUsername();
    }

    @Override
    @Transactional
    public void deleteUser(String userId) {
        User user = userRepository.getUserByUsername(userId);
        if (user == null) {
            logger.error("User not found with username: {}", userId);
            throw new UsernameNotFoundException("User not found");
        } else {
            userRepository.delete(user);
        }
    }

    @Override
    public List<UserResponse> getAllUsers() {
        List<User> users = userRepository.findAll();
        return users.stream().map(this::convertToUserResponse).collect(Collectors.toList());
    }

    @Override
    public String getUserName(String userId) {
        User user = userRepository.getUserByUsername(userId);
        if(user != null) {
            return user.getUsername();
        }
        return userId;
    }

    @Override
    public String loginUser(UserRequest userRequest) {
        User user = userRepository.getUserByUsername(userRequest.getUsername());
        if(user != null) {
            if (passwordEncoder.matches(userRequest.getPassword(), user.getPassword())) {
                return user.getUsername();
            }
        }
        return null;
    }

    @Override
    public String logoutUser(UserRequest userRequest) {
        User user = userRepository.getUserByUsername(userRequest.getUsername());
        if(user != null) {
            if (passwordEncoder.matches(userRequest.getPassword(), user.getPassword())) {
                return user.getUsername();
            }
        }
        return null;
    }

    private UserResponse convertToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId().toString())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .build();
    }
}