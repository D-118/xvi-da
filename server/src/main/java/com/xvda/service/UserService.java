package com.xvda.service;

import com.xvda.dto.LoginVO;
import com.xvda.dto.ProfileDTO;
import com.xvda.entity.SysUser;

public interface UserService {

    /** 登录：code 换 openid，不存在则建档，返回 token */
    LoginVO login(String code);

    SysUser getById(Long id);

    void updateProfile(Long userId, ProfileDTO dto);
}
