package com.ntaganira.heritier.iVura.service;

import com.ntaganira.heritier.iVura.dto.RoleDto;
import com.ntaganira.heritier.iVura.entity.AppPage;
import com.ntaganira.heritier.iVura.entity.Permission;
import com.ntaganira.heritier.iVura.entity.Role;
import com.ntaganira.heritier.iVura.repository.AppPageRepository;
import com.ntaganira.heritier.iVura.repository.PermissionRepository;
import com.ntaganira.heritier.iVura.repository.RoleRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.HashSet;
import java.util.Set;

@Service
public class RoleService {

    private final RoleRepository roleRepo;
    private final PermissionRepository permissionRepo;
    private final AppPageRepository pageRepo;

    public RoleService(RoleRepository roleRepo, PermissionRepository permissionRepo, AppPageRepository pageRepo) {
        this.roleRepo = roleRepo;
        this.permissionRepo = permissionRepo;
        this.pageRepo = pageRepo;
    }

    public Page<Role> findPage(String search, String status, int page, int size) {
        Specification<Role> spec = (root, query, cb) -> {
            Predicate predicate = cb.conjunction();
            if (StringUtils.hasText(search)) {
                String term = "%" + search.trim().toLowerCase() + "%";
                predicate = cb.and(predicate, cb.or(
                        cb.like(cb.lower(root.get("name")), term),
                        cb.like(cb.lower(root.get("code")), term),
                        cb.like(cb.lower(root.get("description")), term)
                ));
            }
            if ("active".equalsIgnoreCase(status)) {
                predicate = cb.and(predicate, cb.isTrue(root.get("enabled")));
            } else if ("inactive".equalsIgnoreCase(status)) {
                predicate = cb.and(predicate, cb.isFalse(root.get("enabled")));
            }
            return predicate;
        };
        return roleRepo.findAll(spec, PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "name")));
    }

    public Role findById(Long id) {
        return roleRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Role not found with id: " + id));
    }

    @Transactional
    public Role create(RoleDto dto) {
        if (roleRepo.findByName(dto.getName()).isPresent()) {
            throw new RuntimeException("Role name already exists: " + dto.getName());
        }
        if (roleRepo.findByCode(dto.getCode()).isPresent()) {
            throw new RuntimeException("Role code already exists: " + dto.getCode());
        }
        Role role = Role.builder()
                .name(dto.getName())
                .code(dto.getCode())
                .description(dto.getDescription())
                .enabled(dto.isEnabled())
                .permissions(resolvePermissions(dto))
                .pages(resolvePages(dto))
                .build();
        return roleRepo.save(role);
    }

    @Transactional
    public Role update(Long id, RoleDto dto) {
        Role role = findById(id);
        roleRepo.findByName(dto.getName())
                .filter(r -> !r.getId().equals(id))
                .ifPresent(r -> {
                    throw new RuntimeException("Role name already exists: " + dto.getName());
                });
        roleRepo.findByCode(dto.getCode())
                .filter(r -> !r.getId().equals(id))
                .ifPresent(r -> {
                    throw new RuntimeException("Role code already exists: " + dto.getCode());
                });
        role.setName(dto.getName());
        role.setCode(dto.getCode());
        role.setDescription(dto.getDescription());
        role.setEnabled(dto.isEnabled());
        role.setPermissions(resolvePermissions(dto));
        role.setPages(resolvePages(dto));
        return roleRepo.save(role);
    }

    @Transactional
    public void delete(Long id) {
        if (roleRepo.isAssignedToUser(id)) {
            throw new RuntimeException("Role is assigned to users and cannot be deleted");
        }
        roleRepo.deleteById(id);
    }

    @Transactional
    public Role toggleStatus(Long id) {
        Role role = findById(id);
        role.setEnabled(!role.isEnabled());
        return roleRepo.save(role);
    }

    public long countUsers(Long roleId) {
        return roleRepo.countAssignedUsers(roleId);
    }

    private Set<Permission> resolvePermissions(RoleDto dto) {
        if (dto.getPermissionIds() == null || dto.getPermissionIds().isEmpty()) {
            return new HashSet<>();
        }
        return new HashSet<>(permissionRepo.findAllById(dto.getPermissionIds()));
    }

    private Set<AppPage> resolvePages(RoleDto dto) {
        if (dto.getPageIds() == null || dto.getPageIds().isEmpty()) {
            return new HashSet<>();
        }
        return new HashSet<>(pageRepo.findAllById(dto.getPageIds()));
    }
}
