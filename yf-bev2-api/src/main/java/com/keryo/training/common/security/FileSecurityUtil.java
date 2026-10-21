package com.keryo.training.common.security;

import com.yf.base.api.exception.ServiceException;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

/**
 * 文件安全工具（T-07.3 XXE / T-07.5 魔术字节）
 */
public class FileSecurityUtil {

    private FileSecurityUtil() {
    }

    /** 校验文件头魔术字节（防伪装/恶意文件） */
    public static void checkMagicBytes(MultipartFile file, String... allowedSignatures) {
        if (file == null || file.isEmpty()) {
            throw new ServiceException("文件不能为空");
        }
        try (InputStream is = file.getInputStream()) {
            byte[] header = new byte[8];
            int read = is.read(header);
            if (read < 4) {
                throw new ServiceException("文件内容过短，无法识别");
            }
            for (String sig : allowedSignatures) {
                if (sig.startsWith("PK")) {
                    // ZIP 系（xlsx/docx/pptx）：PK\x03\x04
                    if (header[0] == 'P' && header[1] == 'K'
                            && (header[2] == 0x03 || header[2] == 0x05 || header[2] == 0x07)) {
                        return;
                    }
                } else if (sig.startsWith("D0CF")) {
                    // HSSF（老 .xls）：D0 CF 11 E0
                    if ((header[0] & 0xFF) == 0xD0 && (header[1] & 0xFF) == 0xCF) {
                        return;
                    }
                }
            }
            throw new ServiceException("文件格式校验失败：不是合法的 Excel/Office 文件");
        } catch (IOException e) {
            throw new ServiceException("文件读取失败：" + e.getMessage());
        }
    }

    /** 安全解析 Excel（魔术字节校验 + POI 解析；xlsx 由 POI 内部禁用 DTD，防 XXE） */
    public static Workbook safeExcel(MultipartFile file) {
        String name = file.getOriginalFilename() == null ? "" : file.getOriginalFilename();
        if (name.toLowerCase().endsWith(".xls")) {
            checkMagicBytes(file, "D0CF");
            try (InputStream is = file.getInputStream()) {
                return new HSSFWorkbook(is);
            } catch (IOException e) {
                throw new ServiceException("Excel 解析失败：" + e.getMessage());
            }
        }
        // 默认按 xlsx（zip 容器）
        checkMagicBytes(file, "PK");
        try (InputStream is = file.getInputStream()) {
            // POI 5.x 的 XSSFWorkbook 内部 XMLReader 已禁用外部实体（XXE 防护）
            return new XSSFWorkbook(is);
        } catch (IOException e) {
            throw new ServiceException("Excel 解析失败：" + e.getMessage());
        }
    }
}

