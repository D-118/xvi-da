package com.xvda.controller;

import com.xvda.common.BizException;
import com.xvda.common.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * 文件上传接口
 */
@Slf4j
@RestController
@RequestMapping("/api/file")
public class FileController {

    @Value("${xvda.upload-dir:./upload}")
    private String uploadDir;

    private static final Set<String> ALLOWED_EXT = Set.of("jpg", "jpeg", "png", "webp");
    private static final long MAX_SIZE = 5 * 1024 * 1024; // 5MB

    /**
     * 上传衣物图片，返回可访问的相对路径（/upload/yyyy/MM/uuid.ext）
     */
    @PostMapping("/upload")
    public Result<Map<String, String>> upload(@RequestPart("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BizException("请选择要上传的图片");
        }
        if (file.getSize() > MAX_SIZE) {
            throw new BizException("图片不能超过 5MB");
        }
        String original = StringUtils.getFilename(file.getOriginalFilename());
        String ext = original == null ? "" : original.substring(original.lastIndexOf('.') + 1).toLowerCase();
        if (!ALLOWED_EXT.contains(ext)) {
            throw new BizException("仅支持 jpg/jpeg/png/webp 格式");
        }
        // 按年月分目录，uuid 防重名
        String month = LocalDate.now().toString().substring(0, 7).replace('-', '/');
        String name = UUID.randomUUID().toString().replace("-", "") + "." + ext;
        Path dir = Paths.get(uploadDir, month).toAbsolutePath().normalize();
        try {
            Files.createDirectories(dir);
            file.transferTo(dir.resolve(name).toFile());
        } catch (IOException e) {
            log.error("图片保存失败", e);
            throw new BizException("图片保存失败，请稍后重试");
        }
        return Result.ok(Map.of("url", "/upload/" + month + "/" + name));
    }
}
