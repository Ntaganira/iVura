package com.ntaganira.heritier.iVura.service;

import com.ntaganira.heritier.iVura.dto.UserDto;
import com.ntaganira.heritier.iVura.entity.Role;
import com.ntaganira.heritier.iVura.entity.User;
import com.ntaganira.heritier.iVura.repository.RoleRepository;
import com.ntaganira.heritier.iVura.repository.UserRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.HashSet;
import java.util.Set;

@Service
public class UserService {

    private final UserRepository userRepo;
    private final RoleRepository roleRepo;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepo, RoleRepository roleRepo, PasswordEncoder passwordEncoder) {
        this.userRepo = userRepo;
        this.roleRepo = roleRepo;
        this.passwordEncoder = passwordEncoder;
    }

    public Page<User> findPage(String search, String status, int page, int size) {
        Specification<User> spec = (root, query, cb) -> {
            Predicate predicate = cb.conjunction();
            if (StringUtils.hasText(search)) {
                String term = "%" + search.trim().toLowerCase() + "%";
                predicate = cb.and(predicate, cb.or(
                        cb.like(cb.lower(root.get("username")), term),
                        cb.like(cb.lower(root.get("email")), term),
                        cb.like(cb.lower(root.get("fullName")), term),
                        cb.like(cb.lower(root.get("phone")), term)
                ));
            }
            if ("active".equalsIgnoreCase(status)) {
                predicate = cb.and(predicate, cb.isTrue(root.get("enabled")));
            } else if ("inactive".equalsIgnoreCase(status)) {
                predicate = cb.and(predicate, cb.isFalse(root.get("enabled")));
            }
            return predicate;
        };
        return userRepo.findAll(spec, PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));
    }

    public User findById(Long id) {
        return userRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }

    @Transactional
    public User create(UserDto dto) {
        if (userRepo.findByUsername(dto.getUsername()).isPresent()) {
            throw new RuntimeException("Username is already taken: " + dto.getUsername());
        }
        if (userRepo.findByEmail(dto.getEmail()).isPresent()) {
            throw new RuntimeException("Email is already registered: " + dto.getEmail());
        }
        String rawPassword = StringUtils.hasText(dto.getPassword())
                ? dto.getPassword() : "password123";
        User user = User.builder()
                .username(dto.getUsername())
                .email(dto.getEmail())
                .password(passwordEncoder.encode(rawPassword))
                .fullName(dto.getFullName())
                .phone(dto.getPhone())
                .enabled(dto.isEnabled())
                .roles(resolveRoles(dto))
                .build();
        return userRepo.save(user);
    }

    @Transactional
    public User update(Long id, UserDto dto) {
        User user = findById(id);
        userRepo.findByUsername(dto.getUsername())
                .filter(u -> !u.getId().equals(id))
                .ifPresent(u -> {
                    throw new RuntimeException("Username is already taken: " + dto.getUsername());
                });
        userRepo.findByEmail(dto.getEmail())
                .filter(u -> !u.getId().equals(id))
                .ifPresent(u -> {
                    throw new RuntimeException("Email is already registered: " + dto.getEmail());
                });
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setFullName(dto.getFullName());
        user.setPhone(dto.getPhone());
        user.setEnabled(dto.isEnabled());
        if (StringUtils.hasText(dto.getPassword())) {
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
        }
        user.setRoles(resolveRoles(dto));
        return userRepo.save(user);
    }

    @Transactional
    public User toggleStatus(Long id) {
        User user = findById(id);
        user.setEnabled(!user.isEnabled());
        return userRepo.save(user);
    }

    @Transactional
    public void resetPassword(Long id, String newPassword) {
        User user = findById(id);
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepo.save(user);
    }

    private Set<Role> resolveRoles(UserDto dto) {
        if (dto.getRoleIds() == null || dto.getRoleIds().isEmpty()) {
            return new HashSet<>();
        }
        return new HashSet<>(roleRepo.findAllById(dto.getRoleIds()));
    }
}
