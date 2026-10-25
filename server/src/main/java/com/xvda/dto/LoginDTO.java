package com.xvda.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginDTO {

    /** wx.login 获取的临时凭证 */
    @NotBlank(message = "code 不能为空")
    private String code;
}
