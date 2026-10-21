package com.yf.modules.exam.repo.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.type.TypeReference;
import com.yf.base.api.api.dto.PagingReqDTO;
import com.yf.base.api.exception.ServiceException;
import com.yf.base.utils.BeanMapper;
import com.yf.base.utils.jackson.JsonHelper;
import com.yf.modules.exam.repo.dto.ImportResult;
import com.yf.modules.exam.repo.dto.RepoQuAnswerDTO;
import com.yf.modules.exam.repo.dto.RepoQuDTO;
import com.yf.modules.exam.repo.dto.request.RepoQuDetailDTO;
import com.yf.modules.exam.repo.dto.request.RepoQuListReqDTO;
import com.yf.modules.exam.repo.dto.request.ReviewReqDTO;
import com.yf.modules.exam.repo.entity.RepoQu;
import com.yf.modules.exam.repo.mapper.RepoQuMapper;
import com.yf.modules.exam.repo.service.RepoQuAnswerService;
import com.yf.modules.exam.repo.service.RepoQuService;
import com.yf.system.modules.user.UserUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.*;

/**
 * <p>
 * 问题题目业务实现类
 * </p>
 *
 * @author 聪明笨狗
 * @since 2025-04-11 09:42
 */
@RequiredArgsConstructor
@Service
public class RepoQuServiceImpl extends ServiceImpl<RepoQuMapper, RepoQu> implements RepoQuService {


    private final RepoQuAnswerService repoQuAnswerService;

    @Override
    public IPage<RepoQuDTO> paging(PagingReqDTO<RepoQuListReqDTO> reqDTO) {

        //查询条件
        QueryWrapper<RepoQu> wrapper = new QueryWrapper<>();

        // 请求参数
        RepoQuListReqDTO params = reqDTO.getParams();

        if (params != null) {
            if (StringUtils.isNotBlank(params.getContent())) {
                wrapper.lambda().like(RepoQu::getContent, params.getContent());
            }
            if (StringUtils.isNotBlank(params.getRepoId())) {
                wrapper.lambda().eq(RepoQu::getRepoId, params.getRepoId());
            }
            if (StringUtils.isNotBlank(params.getQuType())) {
                wrapper.lambda().eq(RepoQu::getQuType, params.getQuType());
            }
            if (StringUtils.isNotBlank(params.getDifficultyLevel())) {
                wrapper.lambda().eq(RepoQu::getDifficultyLevel, params.getDifficultyLevel());
            }
            if (StringUtils.isNotBlank(params.getReviewStatus())) {
                wrapper.lambda().eq(RepoQu::getReviewStatus, params.getReviewStatus());
            }
        }

        wrapper.lambda().orderByDesc(RepoQu::getCreateTime);

        //获得数据
        IPage<RepoQu> page = this.page(reqDTO.toPage(), wrapper);
        return JsonHelper.parseObject(page, new TypeReference<Page<RepoQuDTO>>() {
        });
    }


    @Transactional(rollbackFor = Exception.class)
    @Override
    public void save(RepoQuDetailDTO reqDTO) {
        //复制参数
        RepoQu entity = new RepoQu();
        BeanMapper.copy(reqDTO, entity);

        boolean isNew = StringUtils.isBlank(reqDTO.getId());
        if (isNew) {
            // 新题默认进入草稿待一审
            entity.setReviewStatus("DRAFT");
            entity.setReviewLevel(0);
        } else {
            // 编辑已存在的题目：内容变更需重新走两级审核
            RepoQu old = this.getById(reqDTO.getId());
            if (old != null && !"DRAFT".equals(old.getReviewStatus())) {
                entity.setReviewStatus("DRAFT");
                entity.setReviewLevel(0);
                entity.setFirstReviewer(null);
                entity.setFirstReviewTime(null);
                entity.setSecondReviewer(null);
                entity.setSecondReviewTime(null);
                entity.setReviewRemark(null);
            }
        }

        this.saveOrUpdate(entity);

        // 保存选项
        repoQuAnswerService.saveAll(entity.getId(), reqDTO.getAnswerList());
    }

    @Override
    public void delete(List<String> ids) {
        //批量删除
        this.removeByIds(ids);
    }

    @Override
    public RepoQuDetailDTO detail(String id) {
        // 基本信息
        RepoQu entity = this.getById(id);
        if (entity == null) {
            return null;
        }
        RepoQuDetailDTO dto = new RepoQuDetailDTO();
        BeanMapper.copy(entity, dto);

        List<RepoQuAnswerDTO> answerList = repoQuAnswerService.listByQuId(id);
        dto.setAnswerList(answerList);
        return dto;
    }

    @Override
    public List<RepoQuDTO> list(RepoQuDTO reqDTO) {

        //分页查询并转换
        QueryWrapper<RepoQu> wrapper = new QueryWrapper<>();

        //转换并返回
        List<RepoQu> list = this.list(wrapper);

        //转换数据
        List<RepoQuDTO> dtoList = BeanMapper.mapList(list, RepoQuDTO.class);

        return dtoList;
    }

    @Override
    public List<RepoQuDetailDTO> listForPaper(String repoId, String quType, Integer quCount) {

        Map<String, List<String>> map = this.typeQuIds(repoId);
        if (!map.containsKey(quType)) {
            throw new ServiceException("组卷失败，题型" + quType + "没有试题！");
        }

        List<String> ids = map.get(quType);
        if (ids.size() < quCount) {
            throw new ServiceException("组卷失败，题型" + quType + "试题不足！");
        }

        // 随机排序
        Collections.shuffle(ids);

        // 逐个构建详情，注意效率问题
        List<RepoQuDetailDTO> resultList = new ArrayList<>();
        ids.subList(0, quCount).forEach(id -> resultList.add(this.detail(id)));

        return resultList;
    }


    /**
     * 把题库的全部ID找出来，组成一个题型--IDS列表的MAP
     * 仅取审核通过(APPROVED)的题目，DRAFT/REJECTED 不进入考试
     *
     * @param repoId
     * @return
     */
    private Map<String, List<String>> typeQuIds(String repoId) {
        Map<String, List<String>> map = new HashMap<>();

        //分页查询并转换
        QueryWrapper<RepoQu> wrapper = new QueryWrapper<>();
        wrapper.lambda()
                .select(RepoQu::getId, RepoQu::getQuType)
                .eq(RepoQu::getRepoId, repoId)
                .eq(RepoQu::getReviewStatus, "APPROVED");

        List<RepoQu> list = this.list(wrapper);
        if (CollectionUtils.isEmpty(list)) {
            return map;
        }

        for (RepoQu repoQu : list) {
            if (map.containsKey(repoQu.getQuType())) {
                map.get(repoQu.getQuType()).add(repoQu.getId());
            } else {
                List<String> ids = new ArrayList<>();
                ids.add(repoQu.getId());
                map.put(repoQu.getQuType(), ids);
            }
        }

        return map;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void review(ReviewReqDTO req) {
        RepoQu qu = this.getById(req.getQuId());
        if (qu == null) {
            throw new ServiceException("题目不存在");
        }

        String currentUserId = UserUtils.getUserId();
        String action = req.getAction();
        Integer level = req.getLevel();

        // 重新提交：仅被驳回的题目可由编辑重置回草稿
        if ("RESUBMIT".equals(action)) {
            if (!"REJECTED".equals(qu.getReviewStatus())) {
                throw new ServiceException("仅被驳回的题目可重新提交");
            }
            resetReview(qu);
            this.updateById(qu);
            return;
        }

        if (level == null || (level != 1 && level != 2)) {
            throw new ServiceException("审核层级不合法");
        }

        if (level == 1) {
            // 一审：仅草稿或被驳回可进入一审
            if (!"DRAFT".equals(qu.getReviewStatus()) && !"REJECTED".equals(qu.getReviewStatus())) {
                throw new ServiceException("当前状态不可进行一审");
            }
            // 约束：不可审核自己创建的题目（总护理长/管理员可豁免）
            if (currentUserId.equals(qu.getCreateBy()) && !isSuperReviewer()) {
                throw new ServiceException("不能审核自己创建的题目");
            }
            if ("APPROVE".equals(action)) {
                qu.setReviewStatus("REVIEW2");
                qu.setReviewLevel(1);
                qu.setFirstReviewer(currentUserId);
                qu.setFirstReviewTime(new Date());
                qu.setSecondReviewer(null);
                qu.setSecondReviewTime(null);
            } else if ("REJECT".equals(action)) {
                qu.setReviewStatus("REJECTED");
                qu.setReviewLevel(1);
                qu.setFirstReviewer(currentUserId);
                qu.setFirstReviewTime(new Date());
                qu.setSecondReviewer(null);
                qu.setSecondReviewTime(null);
            } else {
                throw new ServiceException("审核动作不合法");
            }
        } else {
            // 二审：必须先完成一审
            if (!"REVIEW2".equals(qu.getReviewStatus())) {
                throw new ServiceException("请先完成一审");
            }
            // 约束：一二审不可为同一人（总护理长/管理员可豁免）
            if (currentUserId.equals(qu.getFirstReviewer()) && !isSuperReviewer()) {
                throw new ServiceException("二审人不能与一审人相同");
            }
            if ("APPROVE".equals(action)) {
                qu.setReviewStatus("APPROVED");
                qu.setReviewLevel(2);
                qu.setSecondReviewer(currentUserId);
                qu.setSecondReviewTime(new Date());
            } else if ("REJECT".equals(action)) {
                qu.setReviewStatus("REJECTED");
                qu.setReviewLevel(2);
                qu.setSecondReviewer(currentUserId);
                qu.setSecondReviewTime(new Date());
            } else {
                throw new ServiceException("审核动作不合法");
            }
        }

        qu.setReviewRemark(req.getRemark());
        this.updateById(qu);
    }

    /**
     * 一键审核（总护理长/管理员）：选中题目直接置 APPROVED
     */
    @Override
    public void batchApprove(List<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return;
        }
        if (!isSuperReviewer()) {
            throw new ServiceException("仅总护理长/管理员可一键审核");
        }
        String uid = UserUtils.getUserId();
        Date now = new Date();
        for (String id : ids) {
            RepoQu qu = this.getById(id);
            if (qu == null || "APPROVED".equals(qu.getReviewStatus())) {
                continue;
            }
            qu.setReviewStatus("APPROVED");
            qu.setReviewLevel(2);
            qu.setFirstReviewer(uid);
            qu.setFirstReviewTime(now);
            qu.setSecondReviewer(uid);
            qu.setSecondReviewTime(now);
            qu.setReviewRemark("管理员一键审核");
            this.updateById(qu);
        }
    }

    /** 权限模型 v2：超管/总监/督导可审自己建的题；助理不豁免（助理的题必须督导/总监审核） */
    private boolean isSuperReviewer() {
        List<String> roles = UserUtils.getRoles();
        if (CollectionUtils.isEmpty(roles)) {
            return false;
        }
        return roles.contains("admin") || roles.contains("keryo_director")
                || roles.contains("keryo_supervisor");
    }

    /**
     * 清空审核信息，重置为草稿
     */
    private void resetReview(RepoQu qu) {
        qu.setReviewStatus("DRAFT");
        qu.setReviewLevel(0);
        qu.setFirstReviewer(null);
        qu.setFirstReviewTime(null);
        qu.setSecondReviewer(null);
        qu.setSecondReviewTime(null);
        qu.setReviewRemark(null);
    }

    @Override
    public List<RepoQuDetailDTO> randomForPractice(String repoId, String quType, Integer count) {
        QueryWrapper<RepoQu> wrapper = new QueryWrapper<>();
        wrapper.lambda().eq(RepoQu::getReviewStatus, "APPROVED");
        if (StringUtils.isNotBlank(repoId)) {
            wrapper.lambda().eq(RepoQu::getRepoId, repoId);
        }
        if (StringUtils.isNotBlank(quType)) {
            wrapper.lambda().eq(RepoQu::getQuType, quType);
        }
        List<RepoQu> list = this.list(wrapper);
        // 随机打乱
        Collections.shuffle(list);
        if (count != null && list.size() > count) {
            list = list.subList(0, count);
        }
        List<RepoQuDetailDTO> result = new ArrayList<>();
        list.forEach(q -> result.add(this.detail(q.getId())));
        return result;
    }

    @Override
    public ImportResult importFile(String repoId, MultipartFile file) {
        ImportResult res = new ImportResult();
        if (StringUtils.isBlank(repoId)) {
            res.setTotal(0);
            res.setSuccess(0);
            res.setFail(1);
            res.setErrors(List.of("请先选择题库再导入"));
            return res;
        }
        if (file == null || file.isEmpty()) {
            res.setTotal(0);
            res.setSuccess(0);
            res.setFail(1);
            res.setErrors(List.of("文件为空"));
            return res;
        }
        try {
            byte[] bytes = file.getBytes();
            boolean zipMagic = bytes.length > 4 && bytes[0] == 0x50 && bytes[1] == 0x4B
                    && bytes[2] == 0x03 && bytes[3] == 0x04;
            boolean xlsMagic = bytes.length > 4 && bytes[0] == (byte) 0xD0 && bytes[1] == (byte) 0xCF
                    && bytes[2] == (byte) 0x11 && bytes[3] == (byte) 0xE0;
            if (zipMagic) {
                // ZIP 家族：按内部条目区分 docx（word/document.xml）与 xlsx（xl/workbook.xml）
                boolean docx = false;
                boolean xlsx = false;
                try (java.util.zip.ZipInputStream zis = new java.util.zip.ZipInputStream(new ByteArrayInputStream(bytes))) {
                    java.util.zip.ZipEntry entry;
                    int checked = 0;
                    while ((entry = zis.getNextEntry()) != null && checked < 50) {
                        String name = entry.getName();
                        if ("word/document.xml".equals(name)) {
                            docx = true;
                            break;
                        }
                        if ("xl/workbook.xml".equals(name)) {
                            xlsx = true;
                            break;
                        }
                        checked++;
                    }
                }
                if (docx) {
                    return importWord(repoId, file);
                }
                if (xlsx) {
                    return importExcel(repoId, file);
                }
                res.setTotal(0);
                res.setSuccess(0);
                res.setFail(1);
                res.setErrors(List.of("文件格式不支持，仅支持 .xls/.xlsx/.docx"));
                return res;
            }
            if (xlsMagic) {
                return importExcel(repoId, file);
            }
            res.setTotal(0);
            res.setSuccess(0);
            res.setFail(1);
            res.setErrors(List.of("文件格式不支持，仅支持 .xls/.xlsx/.docx"));
            return res;
        } catch (Exception e) {
            res.setTotal(0);
            res.setSuccess(0);
            res.setFail(1);
            res.setErrors(List.of("解析失败: " + e.getMessage()));
            return res;
        }
    }

    /** Word(.docx) 批量导入：按血透中心考核文档排版解析 */
    @Override
    public ImportResult importWord(String repoId, MultipartFile file) {
        ImportResult res = new ImportResult();
        List<String> errors = new ArrayList<>();
        try {
            byte[] bytes = file.getBytes();
            try (org.apache.poi.xwpf.usermodel.XWPFDocument doc = new org.apache.poi.xwpf.usermodel.XWPFDocument(new ByteArrayInputStream(bytes))) {
                List<String> lines = new ArrayList<>();
                for (org.apache.poi.xwpf.usermodel.XWPFParagraph p : doc.getParagraphs()) {
                    String t = p.getText() == null ? "" : p.getText().trim();
                    if (!t.isEmpty()) {
                        lines.add(t);
                    }
                }
                List<RepoQuDetailDTO> quList = DocxQuParser.parse(lines);
                int total = quList.size();
                int success = 0;
                for (RepoQuDetailDTO dto : quList) {
                    try {
                        dto.setRepoId(repoId);
                        this.save(dto); // save 内部置为 DRAFT 待审核
                        success++;
                    } catch (Exception e) {
                        errors.add("解析失败: " + e.getMessage());
                    }
                }
                res.setTotal(total);
                res.setSuccess(success);
                res.setFail(total - success);
                res.setErrors(errors);
            }
        } catch (Exception e) {
            res.setTotal(0);
            res.setSuccess(0);
            res.setFail(1);
            res.setErrors(List.of("解析失败: " + e.getMessage()));
        }
        return res;
    }

    @Override
    public ImportResult importExcel(String repoId, MultipartFile file) {
        ImportResult res = new ImportResult();
        List<String> errors = new ArrayList<>();

        // 🔴 H3 XXE 防护：全局禁用外部 DTD / Schema（POI 4.x 默认已开启 FEATURE_SECURE_PROCESSING）
        System.setProperty("javax.xml.XMLConstants.ACCESS_EXTERNAL_DTD", "");
        System.setProperty("javax.xml.XMLConstants.ACCESS_EXTERNAL_SCHEMA", "");

        if (file == null || file.isEmpty()) {
            res.setTotal(0);
            res.setSuccess(0);
            res.setFail(1);
            res.setErrors(List.of("文件为空"));
            return res;
        }

        try {
            byte[] bytes = file.getBytes();
            // 魔术字节校验：仅允许真实 Office 文件，杜绝伪装
            boolean xlsx = bytes.length > 4 && bytes[0] == 0x50 && bytes[1] == 0x4B
                    && bytes[2] == 0x03 && bytes[3] == 0x04;
            boolean xls = bytes.length > 4 && bytes[0] == (byte) 0xD0 && bytes[1] == (byte) 0xCF
                    && bytes[2] == (byte) 0x11 && bytes[3] == (byte) 0xE0;
            if (!xlsx && !xls) {
                res.setTotal(0);
                res.setSuccess(0);
                res.setFail(1);
                res.setErrors(List.of("文件格式不支持，仅支持 .xls/.xlsx"));
                return res;
            }

            try (Workbook wb = xlsx
                    ? new XSSFWorkbook(new ByteArrayInputStream(bytes))
                    : new HSSFWorkbook(new ByteArrayInputStream(bytes))) {
                // T-07.3 XXE/伪装防护：校验文件头魔术字节（PK=D0CF 或 zip）
                boolean magicOk = bytes.length > 4
                        && (xlsx ? (bytes[0] == 'P' && bytes[1] == 'K')
                                 : ((bytes[0] & 0xFF) == 0xD0 && (bytes[1] & 0xFF) == 0xCF));
                if (!magicOk) {
                    res.setSuccess(0);
                    res.setFail(1);
                    res.setErrors(List.of("文件格式校验失败：不是合法的 Excel 文件"));
                    return res;
                }
                Sheet sheet = wb.getSheetAt(0);
                int total = 0, success = 0, fail = 0;
                for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                    Row row = sheet.getRow(i);
                    if (row == null) {
                        continue;
                    }
                    total++;
                    try {
                        RepoQuDetailDTO dto = parseRow(row);
                        if (dto == null) {
                            fail++;
                            errors.add("第" + (i + 1) + "行: 题型或题干缺失");
                            continue;
                        }
                        dto.setRepoId(repoId);
                        this.save(dto); // save 内部置为 DRAFT 待审核
                        success++;
                    } catch (Exception e) {
                        fail++;
                        errors.add("第" + (i + 1) + "行: " + e.getMessage());
                    }
                }
                res.setTotal(total);
                res.setSuccess(success);
                res.setFail(fail);
                res.setErrors(errors);
            }
        } catch (Exception e) {
            res.setTotal(0);
            res.setSuccess(0);
            res.setFail(1);
            res.setErrors(List.of("解析失败: " + e.getMessage()));
        }
        return res;
    }

    /**
     * 解析单行为题目 DTO
     * 列：A题型 B难度 C题干 D~G选项 H正确答案(如 A 或 A,C) I解析
     */
    private RepoQuDetailDTO parseRow(Row row) {
        String quType = cellStr(row, 0);
        String diff = cellStr(row, 1);
        String content = cellStr(row, 2);
        if (quType.isBlank() || content.isBlank()) {
            return null;
        }
        RepoQuDetailDTO dto = new RepoQuDetailDTO();
        dto.setQuType(mapType(quType));
        dto.setDifficultyLevel(mapDiff(diff));
        dto.setContent(content);
        dto.setAnalysis(cellStr(row, 8));

        String correct = cellStr(row, 7).toUpperCase();
        List<RepoQuAnswerDTO> answers = new ArrayList<>();
        for (int c = 3; c <= 6; c++) {
            String opt = cellStr(row, c);
            if (opt.isBlank()) {
                continue;
            }
            char letter = (char) ('A' + (c - 3));
            RepoQuAnswerDTO a = new RepoQuAnswerDTO();
            a.setContent(opt);
            a.setIsRight(correct.contains(String.valueOf(letter)));
            answers.add(a);
        }
        if (answers.isEmpty()) {
            return null;
        }
        dto.setAnswerList(answers);
        return dto;
    }

    private String cellStr(Row row, int idx) {
        Cell cell = row.getCell(idx);
        if (cell == null) {
            return "";
        }
        if (cell.getCellType() == CellType.NUMERIC) {
            double v = cell.getNumericCellValue();
            if (v == Math.floor(v) && !Double.isInfinite(v)) {
                return String.valueOf((long) v);
            }
            return String.valueOf(v);
        }
        String s = cell.getStringCellValue();
        return s == null ? "" : s.trim();
    }

    private String mapType(String t) {
        t = t.trim();
        if (t.contains("多选") || t.equalsIgnoreCase("multi")) return "multi";
        if (t.contains("判断") || t.equalsIgnoreCase("judge")) return "judge";
        return "radio";
    }

    private String mapDiff(String d) {
        d = d.trim().toLowerCase();
        if (d.contains("中") || d.contains("medium")) return "medium";
        if (d.contains("难") || d.contains("hard")) return "hard";
        return "easy";
    }
}

