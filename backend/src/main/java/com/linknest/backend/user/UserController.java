package com.linknest.backend.user;

import com.linknest.backend.user.dto.UserCreateReq;
import com.linknest.backend.user.dto.UserRes;
import com.linknest.backend.user.dto.UserUpdateReq;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {
    private final UserService service;

    @PostMapping
    public UserRes create(@RequestBody @Valid UserCreateReq req) {
        return service.create(req);
    }

    @GetMapping("/{id}")
    public UserRes get(@PathVariable Long id) {
        return service.get(id);
    }

    @PatchMapping("/{id}")
    public UserRes update(@PathVariable Long id, @RequestBody @Valid UserUpdateReq req) {
        return service.update(id, req);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
