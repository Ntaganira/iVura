package com.ntaganira.heritier.iVura.service;

import com.ntaganira.heritier.iVura.dto.PermissionDto;
import com.ntaganira.heritier.iVura.entity.Permission;
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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PermissionService {

    private final PermissionRepository permissionRepo;
    private final RoleRepository roleRepo;

    public PermissionService(PermissionRepository permissionRepo, RoleRepository roleRepo) {
        this.permissionRepo = permissionRepo;
        this.roleRepo = roleRepo;
    }

    public Page<Permission> findPage(String search, String module, int page, int size) {
        Specification<Permission> spec = (root, query, cb) -> {
            Predicate predicate = cb.conjunction();
            if (StringUtils.hasText(search)) {
                String term = "%" + search.trim().toLowerCase() + "%";
                predicate = cb.and(predicate, cb.or(
                        cb.like(cb.lower(root.get("name")), term),
                        cb.like(cb.lower(root.get("code")), term),
                        cb.like(cb.lower(root.get("module")), term)
                ));
            }
            if (StringUtils.hasText(module)) {
                predicate = cb.and(predicate, cb.equal(cb.lower(root.get("module")), module.toLowerCase()));
            }
            return predicate;
        };
        return permissionRepo.findAll(spec, PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "module").and(Sort.by(Sort.Direction.ASC, "action"))));
    }

    public Permission findById(Long id) {
        return permissionRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Permission not found with id: " + id));
    }

    @Transactional
    public Permission create(PermissionDto dto) {
        if (permissionRepo.findByCode(dto.getCode()).isPresent()) {
            throw new RuntimeException("Permission code already exists: " + dto.getCode());
        }
        Permission permission = Permission.builder()
                .code(dto.getCode())
                .name(dto.getName())
                .module(dto.getModule())
                .action(dto.getAction())
                .description(dto.getDescription())
                .enabled(dto.isEnabled())
                .build();
        return permissionRepo.save(permission);
    }

    @Transactional
    public Permission update(Long id, PermissionDto dto) {
        Permission permission = findById(id);
        permissionRepo.findByCode(dto.getCode())
                .filter(p -> !p.getId().equals(id))
                .ifPresent(p -> {
                    throw new RuntimeException("Permission code already exists: " + dto.getCode());
                });
        permission.setCode(dto.getCode());
        permission.setName(dto.getName());
        permission.setModule(dto.getModule());
        permission.setAction(dto.getAction());
        permission.setDescription(dto.getDescription());
        permission.setEnabled(dto.isEnabled());
        return permissionRepo.save(permission);
    }

    @Transactional
    public void delete(Long id) {
        if (roleRepo.isPermissionAssigned(id)) {
            throw new RuntimeException("Permission is assigned to roles and cannot be deleted");
        }
        permissionRepo.deleteById(id);
    }

    @Transactional
    public Permission toggleStatus(Long id) {
        Permission permission = findById(id);
        permission.setEnabled(!permission.isEnabled());
        return permissionRepo.save(permission);
    }

    public Map<String, List<Permission>> findEnabledByModule() {
        return permissionRepo.findByEnabledTrueOrderByModuleAscActionAsc()
                .stream()
                .collect(Collectors.groupingBy(Permission::getModule, LinkedHashMap::new, Collectors.toList()));
    }

    public List<String> findModules() {
        return permissionRepo.findAllByOrderByModuleAscActionAsc()
                .stream()
                .map(Permission::getModule)
                .distinct()
                .collect(Collectors.toList());
    }
}
