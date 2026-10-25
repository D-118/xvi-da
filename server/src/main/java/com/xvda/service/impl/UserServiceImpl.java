package com.xvda.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xvda.common.BizException;
import com.xvda.dto.LoginVO;
import com.xvda.dto.ProfileDTO;
import com.xvda.entity.SysUser;
import com.xvda.mapper.SysUserMapper;
import com.xvda.service.UserService;
import com.xvda.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final SysUserMapper userMapper;
    private final JwtUtil jwtUtil;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${xvda.wx.appid:}")
    private String wxAppid;

    @Value("${xvda.wx.secret:}")
    private String wxSecret;

    @Override
    public LoginVO login(String code) {
        String openid = resolveOpenid(code);

        SysUser user = userMapper.selectOne(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getOpenid, openid));
        if (user == null) {
            user = new SysUser();
            user.setOpenid(openid);
            user.setCity("哈尔滨");
            userMapper.insert(user);
        }

        LoginVO vo = new LoginVO();
        vo.setToken(jwtUtil.issue(user.getId()));
        vo.setUserId(user.getId());
        vo.setNickname(user.getNickname());
        return vo;
    }

    /**
     * code -> openid
     * 已配置 appid+secret 时调微信 jscode2session；否则进入开发 Mock 模式，
     * 同一开发者工具会话返回稳定 openid，保证本地联调可用。
     */
    private String resolveOpenid(String code) {
        if (StringUtils.hasText(wxAppid) && StringUtils.hasText(wxSecret)) {
            String url = String.format(
                    "https://api.weixin.qq.com/sns/jscode2session?appid=%s&secret=%s&js_code=%s&grant_type=authorization_code",
                    wxAppid, wxSecret, code);
            Map<?, ?> resp = restTemplate.getForObject(url, Map.class);
            if (resp == null || resp.get("openid") == null) {
                log.warn("jscode2session 响应异常: {}", resp);
                throw new BizException(401, "微信登录失败，请重试");
            }
            return (String) resp.get("openid");
        }
        log.warn("未配置 wx.appid/wx.secret，使用开发 Mock openid（不可用于真机发布）");
        return "dev-mock-user";
    }

    @Override
    public SysUser getById(Long id) {
        return userMapper.selectById(id);
    }

    @Override
    public void updateProfile(Long userId, ProfileDTO dto) {
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new BizException(404, "用户不存在");
        }
        if (dto.getNickname() != null) user.setNickname(dto.getNickname());
        if (dto.getAvatarUrl() != null) user.setAvatarUrl(dto.getAvatarUrl());
        if (dto.getCity() != null) user.setCity(dto.getCity());
        if (dto.getHeightCm() != null) user.setHeightCm(dto.getHeightCm());
        if (dto.getWeightKg() != null) user.setWeightKg(dto.getWeightKg());
        if (dto.getBodyType() != null) user.setBodyType(dto.getBodyType());
        if (dto.getStylePrefs() != null) user.setStylePrefs(dto.getStylePrefs());
        userMapper.updateById(user);
    }
}
