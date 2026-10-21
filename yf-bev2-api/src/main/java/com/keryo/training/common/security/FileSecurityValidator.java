package com.keryo.training.common.security;

import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/** 文件上传安全校验：扩展名、MIME 与文件头必须相互一致。 */
public final class FileSecurityValidator {

    private static final Set<String> ALLOWED_EXT = Set.of(
            "jpg", "jpeg", "png", "gif", "webp", "pdf",
            "doc", "docx", "xls", "xlsx", "ppt", "pptx",
            "mp4", "mp3", "zip", "txt", "csv");

    private static final Map<String, Set<String>> MIME_BY_EXT = Map.ofEntries(
            Map.entry("jpg", Set.of("image/jpeg", "application/octet-stream")),
            Map.entry("jpeg", Set.of("image/jpeg", "application/octet-stream")),
            Map.entry("png", Set.of("image/png", "application/octet-stream")),
            Map.entry("gif", Set.of("image/gif", "application/octet-stream")),
            Map.entry("webp", Set.of("image/webp", "application/octet-stream")),
            Map.entry("pdf", Set.of("application/pdf", "application/octet-stream")),
            Map.entry("doc", Set.of("application/msword", "application/octet-stream")),
            Map.entry("docx", Set.of("application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                    "application/zip", "application/octet-stream")),
            Map.entry("xls", Set.of("application/vnd.ms-excel", "application/octet-stream")),
            Map.entry("xlsx", Set.of("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                    "application/zip", "application/octet-stream")),
            Map.entry("ppt", Set.of("application/vnd.ms-powerpoint", "application/octet-stream")),
            Map.entry("pptx", Set.of("application/vnd.openxmlformats-officedocument.presentationml.presentation",
                    "application/zip", "application/octet-stream")),
            Map.entry("mp4", Set.of("video/mp4", "application/octet-stream")),
            Map.entry("mp3", Set.of("audio/mpeg", "audio/mp3", "application/octet-stream")),
            Map.entry("zip", Set.of("application/zip", "application/x-zip-compressed", "application/octet-stream")),
            Map.entry("txt", Set.of("text/plain", "application/octet-stream")),
            Map.entry("csv", Set.of("text/csv", "text/plain", "application/octet-stream")));

    private FileSecurityValidator() {
    }

    public static void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new SecurityException("文件不能为空");
        }
        String original = file.getOriginalFilename();
        if (original == null || original.isEmpty()) {
            throw new SecurityException("文件名不能为空");
        }

        String ext = original.contains(".")
                ? original.substring(original.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT)
                : "";
        if (!ALLOWED_EXT.contains(ext)) {
            throw new SecurityException("不支持的文件类型: ." + ext);
        }

        String mime = file.getContentType();
        if (mime != null && !MIME_BY_EXT.get(ext).contains(mime.toLowerCase(Locale.ROOT))) {
            throw new SecurityException("文件MIME类型与扩展名不匹配: " + mime);
        }

        byte[] header = new byte[16];
        int read = 0;
        try (InputStream is = file.getInputStream()) {
            while (read < header.length) {
                int current = is.read(header, read, header.length - read);
                if (current < 0) {
                    break;
                }
                if (current == 0) {
                    continue;
                }
                read += current;
            }
        } catch (Exception e) {
            throw new SecurityException("文件读取失败: " + e.getMessage());
        }

        if (!matchesMagic(ext, header, read)) {
            throw new SecurityException("文件签名不匹配，可能不是有效的 ." + ext + " 文件");
        }
    }

    private static boolean matchesMagic(String ext, byte[] h, int n) {
        return switch (ext) {
            case "jpg", "jpeg" -> n >= 3 && (h[0] & 0xff) == 0xff && (h[1] & 0xff) == 0xd8
                    && (h[2] & 0xff) == 0xff;
            case "png" -> n >= 8 && (h[0] & 0xff) == 0x89 && h[1] == 'P' && h[2] == 'N' && h[3] == 'G'
                    && (h[4] & 0xff) == 0x0d && (h[5] & 0xff) == 0x0a;
            case "gif" -> n >= 4 && h[0] == 'G' && h[1] == 'I' && h[2] == 'F' && h[3] == '8';
            case "webp" -> n >= 12 && h[0] == 'R' && h[1] == 'I' && h[2] == 'F' && h[3] == 'F'
                    && h[8] == 'W' && h[9] == 'E' && h[10] == 'B' && h[11] == 'P';
            case "pdf" -> n >= 4 && h[0] == '%' && h[1] == 'P' && h[2] == 'D' && h[3] == 'F';
            case "doc", "xls", "ppt" -> n >= 8 && (h[0] & 0xff) == 0xd0 && (h[1] & 0xff) == 0xcf
                    && (h[2] & 0xff) == 0x11 && (h[3] & 0xff) == 0xe0;
            case "docx", "xlsx", "pptx", "zip" -> n >= 4 && h[0] == 'P' && h[1] == 'K'
                    && ((h[2] & 0xff) == 0x03 || (h[2] & 0xff) == 0x05 || (h[2] & 0xff) == 0x07);
            case "mp4" -> n >= 12 && h[4] == 'f' && h[5] == 't' && h[6] == 'y' && h[7] == 'p';
            case "mp3" -> n >= 3 && ((h[0] == 'I' && h[1] == 'D' && h[2] == '3')
                    || ((h[0] & 0xff) == 0xff && (h[1] & 0xe0) == 0xe0));
            case "txt", "csv" -> n > 0 && isText(h, n);
            default -> false;
        };
    }

    private static boolean isText(byte[] bytes, int length) {
        for (int i = 0; i < length; i++) {
            if (bytes[i] == 0) {
                return false;
            }
        }
        return true;
    }

    /** 文件名消毒：去掉路径穿越字符，只保留字母数字中文._-。 */
    public static String sanitize(String name) {
        if (name == null) return "untitled";
        return name.replaceAll("[^a-zA-Z0-9\\u4e00-\\u9fa5._-]", "_")
                .replaceAll("\\.{2,}", ".");
    }
}

