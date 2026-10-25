package com.xvda.controller;

import com.xvda.common.BizException;
import com.xvda.common.Result;
import com.xvda.dto.ProfileDTO;
import com.xvda.entity.SysUser;
import com.xvda.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/profile")
    public Result<ProfileDTO> profile(@RequestAttribute("userId") Long userId) {
        SysUser u = userService.getById(userId);
        if (u == null) {
            throw new BizException(404, "用户不存在");
        }
        ProfileDTO dto = new ProfileDTO();
        dto.setNickname(u.getNickname());
        dto.setAvatarUrl(u.getAvatarUrl());
        dto.setCity(u.getCity());
        dto.setHeightCm(u.getHeightCm());
        dto.setWeightKg(u.getWeightKg());
        dto.setBodyType(u.getBodyType());
        dto.setStylePrefs(u.getStylePrefs());
        return Result.ok(dto);
    }

    @PutMapping("/profile")
    public Result<Void> updateProfile(@RequestAttribute("userId") Long userId,
                                      @RequestBody ProfileDTO dto) {
        userService.updateProfile(userId, dto);
        return Result.ok();
    }
}
