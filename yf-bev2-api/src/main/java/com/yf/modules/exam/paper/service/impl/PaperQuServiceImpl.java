package com.yf.modules.exam.paper.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yf.base.api.exception.ServiceException;
import com.yf.base.utils.LetterUtils;
import com.yf.base.utils.BeanMapper;
import com.yf.base.utils.DecimalUtils;
import com.yf.modules.exam.cheat.service.ExamCheatLogService;
import com.yf.modules.exam.exam.entity.ExamRecord;
import com.yf.modules.exam.exam.mapper.ExamRecordMapper;
import com.yf.modules.exam.paper.dto.request.PaperQuFillReqDTO;
import com.yf.modules.exam.paper.dto.response.PaperQuCardItemRespDTO;
import com.yf.modules.exam.paper.dto.response.PaperQuCardRespDTO;
import com.yf.modules.exam.paper.dto.response.PaperQuDetailDTO;
import com.yf.modules.exam.paper.dto.response.PaperQuFillRespDTO;
import com.yf.modules.exam.paper.entity.Paper;
import com.yf.modules.exam.paper.entity.PaperQu;
import com.yf.modules.exam.paper.entity.PaperQuAnswer;
import com.yf.modules.exam.paper.mapper.PaperMapper;
import com.yf.modules.exam.paper.mapper.PaperQuMapper;
import com.yf.modules.exam.paper.service.PaperQuAnswerService;
import com.yf.modules.exam.paper.service.PaperQuService;
import com.yf.modules.exam.repo.dto.RepoQuAnswerDTO;
import com.yf.modules.exam.repo.dto.request.RepoQuDetailDTO;
import com.yf.modules.exam.repo.service.RepoQuAnswerService;
import com.yf.system.modules.user.UserUtils;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * <p>
 * 试卷考题业务实现类
 * </p>
 *
 * @author 聪明笨狗
 * @since 2025-04-14 17:40
 */
@RequiredArgsConstructor
@Service
public class PaperQuServiceImpl extends ServiceImpl<PaperQuMapper, PaperQu> implements PaperQuService {

    private final PaperQuAnswerService paperQuAnswerService;

    private final RepoQuAnswerService repoQuAnswerService;

    private final ExamCheatLogService cheatLogService;

    private final PaperMapper paperMapper;

    private final ExamRecordMapper examRecordMapper;


    @Transactional(rollbackFor = Exception.class)
    @Override
    public void saveToPaper(String paperId, BigDecimal perScore, List<RepoQuDetailDTO> quList, int startSort) {

        List<PaperQu> paperQuList = new ArrayList<>();
        List<PaperQuAnswer> answerList = new ArrayList<>();

        int sort = startSort;

        for (RepoQuDetailDTO dto : quList) {
            PaperQu entity = new PaperQu();
            entity.setPaperId(paperId);
            entity.setQuType(dto.getQuType());
            entity.setActualScore(DecimalUtils.zero());
            entity.setScore(perScore);
            entity.setQuId(dto.getId());
            entity.setAnswered(false);
            entity.setSort(sort);
            paperQuList.add(entity);

            List<RepoQuAnswerDTO> answers = dto.getAnswerList();

            int i = 0;
            for (RepoQuAnswerDTO answer : answers) {
                PaperQuAnswer ae = new PaperQuAnswer();
                ae.setPaperId(paperId);
                ae.setQuId(dto.getId());
                ae.setAnswerId(answer.getId());
                ae.setChecked(false);
                ae.setIsRight(answer.getIsRight());
                ae.setAbc(LetterUtils.getLetter(i));
                ae.setSort(i);
                answerList.add(ae);
                i++;
            }

            sort++;
        }

        // 保存题目
        saveBatch(paperQuList);

        // 保存选项
        paperQuAnswerService.saveBatch(answerList);

    }

    @Override
    public List<PaperQuCardRespDTO> listQuCard(String paperId) {

        // IDOR 防护：校验试卷归属
        this.checkPaperOwner(paperId);

        //查找全部题目
        QueryWrapper<PaperQu> wrapper = new QueryWrapper<>();
        wrapper.lambda()
                .select(PaperQu::getQuId, PaperQu::getQuType, PaperQu::getAnswered, PaperQu::getMark)
                .eq(PaperQu::getPaperId, paperId)
                .orderByAsc(PaperQu::getScore);
        List<PaperQu> paperQuList = this.list(wrapper);

        // 使用程序转换成题型分组Map
        Map<String, List<PaperQuCardItemRespDTO>> map = new HashMap<>();
        for (PaperQu paperQu : paperQuList) {
            String key = paperQu.getQuType();

            // 复制属性
            PaperQuCardItemRespDTO item = new PaperQuCardItemRespDTO();
            BeanMapper.copy(paperQu, item);

            if (map.containsKey(key)) {
                map.get(key).add(item);
            } else {
                List<PaperQuCardItemRespDTO> list = new ArrayList<>();
                list.add(item);
                map.put(key, list);
            }
        }

        // 转换为列表并返回
        List<PaperQuCardRespDTO> dtoList = new ArrayList<>();
        for (Map.Entry<String, List<PaperQuCardItemRespDTO>> entry : map.entrySet()) {
            PaperQuCardRespDTO dto = new PaperQuCardRespDTO();
            dto.setQuType(entry.getKey());
            dto.setItemList(entry.getValue());
            dtoList.add(dto);
        }
        return dtoList;
    }

    @Override
    public PaperQuDetailDTO detailForAnswer(String paperId, String quId) {
        // IDOR 防护：校验试卷归属
        this.checkPaperOwner(paperId);
        PaperQuDetailDTO dto = baseMapper.detailForAnswer(paperId, quId);

        // 考试中不暴露主观题参考答案（fill/essay 的 answerList 即标准答案）
        if (dto != null && dto.getAnswerList() != null) {
            String quType = dto.getQuType() == null ? "" : dto.getQuType();
            if ("fill".equalsIgnoreCase(quType) || "essay".equalsIgnoreCase(quType)) {
                dto.setAnswerList(List.of());
            }
        }
        return dto;
    }

    @Override
    public Paper checkPaperOwner(String paperId) {
        Paper paper = paperMapper.selectById(paperId);
        if (paper == null) {
            throw new ServiceException("试卷不存在或已被删除！");
        }
        // 管理端（阅卷权限）放行
        if (UserUtils.getRoles().contains("admin") || UserUtils.getRoles().contains("keryo_head_nurse")
                || UserUtils.getRoles().contains("keryo_assistant")) {
            return paper;
        }
        // 学员：只能操作自己的试卷
        String userId = UserUtils.getUserId();
        if (userId == null || !userId.equals(paper.getUserId())) {
            throw new ServiceException("无权操作他人试卷！");
        }
        return paper;
    }

    @Override
    public PaperQuFillRespDTO fillAnswer(PaperQuFillReqDTO reqDTO) {

        // 参数
        String paperId = reqDTO.getPaperId();
        String quId = reqDTO.getQuId();
        List<String> checkedItems = reqDTO.getCheckedItems();

        // IDOR 防护：校验试卷归属（只能答自己的试卷）
        Paper paper = this.checkPaperOwner(paperId);

        // 安全检测（防切屏增强版）
        Date now = new Date();
        // IP_CHANGE：考试期间 IP 变化
        String curIp = getClientIp();
        if (StringUtils.isNotBlank(curIp) && StringUtils.isNotBlank(paper.getExamIp())
                && !curIp.equals(paper.getExamIp())) {
            cheatLogService.record(paperId, null, null, quId, "IP_CHANGE",
                    String.format("考试IP由%s变为%s", paper.getExamIp(), curIp), curIp);
        }
        // M2：逐题提交间隔 <2 秒（防暴力抓取）
        if (paper.getLastAnswerTime() != null) {
            long gapMs = now.getTime() - paper.getLastAnswerTime().getTime();
            if (gapMs < 2000) {
                cheatLogService.record(paperId, null, null, quId, "FAST_ANSWER",
                        String.format("两题提交间隔仅%s毫秒", gapMs), null);
            }
        }
        // 更新上次答题时间
        paper.setLastAnswerTime(now);
        paperMapper.updateById(paper);

        //查找全部题目
        QueryWrapper<PaperQu> wrapper = new QueryWrapper<>();
        wrapper.lambda()
                .select(PaperQu::getId, PaperQu::getQuId, PaperQu::getQuType, PaperQu::getScore)
                .eq(PaperQu::getPaperId, paperId)
                .eq(PaperQu::getQuId, quId);

        PaperQu paperQu = this.getOne(wrapper, false);
        if (paperQu == null) {
            throw new ServiceException("答题错误，题目不存在！");
        }

        // 防切屏：单题答题过快检测（<2秒记违规）
        if (reqDTO.getUseSeconds() != null && reqDTO.getUseSeconds() < 2) {
            cheatLogService.record(paperId, null, null, quId, "FAST_ANSWER",
                    String.format("单题答题仅%s秒", reqDTO.getUseSeconds()), null);
        }

        // 主观题（fill/essay）：走文本答案逻辑（qu_type 存储为小写）
        String quType = paperQu.getQuType() == null ? "" : paperQu.getQuType();
        if ("fill".equalsIgnoreCase(quType) || "essay".equalsIgnoreCase(quType)) {
            return fillAnswerText(paperQu, reqDTO, paper == null ? null : paper.getSessionSecret());
        }

        // 标准答案列表
        List<PaperQuAnswer> answerList = paperQuAnswerService.listForAnswer(paperId, quId);
        List<String> rightList = new ArrayList<>();

        // 正确列表
        for (PaperQuAnswer answer : answerList) {
            if (answer.getIsRight() != null && answer.getIsRight()) {
                rightList.add(answer.getAnswerId());
            }

            // 是否勾选
            answer.setChecked(checkedItems.contains(answer.getAnswerId()));
        }

        // 判断是否正确
        boolean isRight = areListsEqualIgnoreOrder(rightList, checkedItems);

        // 进行保存操作
        paperQuAnswerService.updateBatchById(answerList);

        // 更新结果
        paperQu.setIsRight(isRight);
        paperQu.setActualScore(isRight ? paperQu.getScore() : BigDecimal.ZERO);
        paperQu.setAnswered(CollectionUtils.isNotEmpty(checkedItems));
        // M7：答案 HMAC 签名（防交卷前篡改）
        if (paper != null && StringUtils.isNotBlank(paper.getSessionSecret())) {
            List<String> sorted = new ArrayList<>(checkedItems == null ? List.of() : checkedItems);
            Collections.sort(sorted);
            paperQu.setAnswerHash(hmacSha256(paper.getSessionSecret(), String.join(",", sorted)));
        }
        this.updateById(paperQu);


        PaperQuFillRespDTO respDTO = new PaperQuFillRespDTO();
        respDTO.setFilled(paperQu.getAnswered());
        return respDTO;
    }

    /**
     * 主观题（FILL/ESSAY）答题与判分
     * FILL 自动判分：与题库标准答案文本比对（去空白后包含即判对）
     * ESSAY 人工阅卷：不自动判分，actualScore=0，等待人工阅卷
     */
    private PaperQuFillRespDTO fillAnswerText(PaperQu paperQu, PaperQuFillReqDTO reqDTO, String sessionSecret) {

        String answerText = reqDTO.getAnswerText() == null ? "" : reqDTO.getAnswerText().trim();
        paperQu.setAnswerText(answerText);
        paperQu.setAnswered(StringUtils.isNotBlank(answerText));
        // M7：主观题答案 HMAC 签名
        if (StringUtils.isNotBlank(sessionSecret)) {
            paperQu.setAnswerHash(hmacSha256(sessionSecret, answerText));
        }

        if ("fill".equalsIgnoreCase(paperQu.getQuType())) {
            // 从题库答案表取标准答案（is_right=1 的 content）
            List<RepoQuAnswerDTO> stdAnswerList = repoQuAnswerService.listByQuId(paperQu.getQuId());
            boolean isRight = false;
            for (RepoQuAnswerDTO std : stdAnswerList) {
                if (Boolean.TRUE.equals(std.getIsRight()) && StringUtils.isNotBlank(std.getContent())) {
                    String stdText = std.getContent().replaceAll("\\s", "");
                    if (answerText.replaceAll("\\s", "").contains(stdText)) {
                        isRight = true;
                        break;
                    }
                }
            }
            paperQu.setIsRight(isRight);
            paperQu.setActualScore(isRight ? paperQu.getScore() : BigDecimal.ZERO);
        } else {
            // ESSAY 主观题：待人工阅卷
            paperQu.setIsRight(null);
            paperQu.setActualScore(BigDecimal.ZERO);
        }

        this.updateById(paperQu);

        PaperQuFillRespDTO respDTO = new PaperQuFillRespDTO();
        respDTO.setFilled(paperQu.getAnswered());
        return respDTO;
    }

    /**
     * M7：HMAC-SHA256 签名（十六进制）
     */
    private String hmacSha256(String secret, String content) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] raw = mac.doFinal(content == null ? new byte[0] : content.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : raw) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 获取客户端 IP（兼容代理头）
     */
    private String getClientIp() {
        try {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                HttpServletRequest request = attrs.getRequest();
                String ip = request.getHeader("X-Forwarded-For");
                if (ip == null || ip.isBlank() || "unknown".equalsIgnoreCase(ip)) {
                    ip = request.getRemoteAddr();
                } else {
                    ip = ip.split(",")[0].trim();
                }
                return ip;
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    @Override
    public BigDecimal sumTotalScore(String paperId) {
        return baseMapper.sumTotalScore(paperId);
    }

    @Override
    public PaperQuFillRespDTO reviewEssay(String paperId, String quId, BigDecimal actualScore) {

        QueryWrapper<PaperQu> wrapper = new QueryWrapper<>();
        wrapper.lambda()
                .eq(PaperQu::getPaperId, paperId)
                .eq(PaperQu::getQuId, quId);

        PaperQu paperQu = this.getOne(wrapper, false);
        if (paperQu == null) {
            throw new ServiceException("阅卷失败，题目不存在！");
        }

        // 校验分数范围：0 ~ 满分
        if (actualScore == null || actualScore.compareTo(BigDecimal.ZERO) < 0
                || actualScore.compareTo(paperQu.getScore()) > 0) {
            throw new ServiceException("阅卷分数必须在 0 ~ 满分之间！");
        }

        paperQu.setActualScore(actualScore);
        paperQu.setIsRight(actualScore.compareTo(BigDecimal.ZERO) > 0);
        this.updateById(paperQu);

        // 重新汇总试卷总分 + 同步考试记录（用 Mapper 注入避免 Service 循环依赖）
        Paper paper = paperMapper.selectById(paperId);
        if (paper != null) {
            BigDecimal total = this.sumTotalScore(paperId);
            paper.setUserScore(total);
            boolean passed = DecimalUtils.ge(total, paper.getQualifyScore());
            paper.setPassed(passed);
            paperMapper.updateById(paper);

            // 更新考试记录：查记录并更新分数
            ExamRecord record = examRecordMapper.selectOne(new QueryWrapper<ExamRecord>().lambda()
                    .eq(ExamRecord::getUserId, paper.getUserId())
                    .eq(ExamRecord::getExamId, paper.getExamId()));
            if (record != null) {
                record.setLastScore(total);
                record.setPaperId(paperId);
                if (DecimalUtils.gt(total, record.getMaxScore())) {
                    record.setMaxScore(total);
                    record.setPassed(passed);
                }
                examRecordMapper.updateById(record);
            }
        }

        PaperQuFillRespDTO respDTO = new PaperQuFillRespDTO();
        respDTO.setFilled(paperQu.getAnswered());
        return respDTO;
    }

    /**
     * 两个List比较
     *
     * @param list1
     * @param list2
     * @return
     */
    private static boolean areListsEqualIgnoreOrder(List<String> list1, List<String> list2) {

        // 快速校验
        if (Objects.equals(list1, list2)) {
            return true;
        }  // 包括都为null的情况
        if (list1 == null || list2 == null) {
            return false;
        }
        if (list1.size() != list2.size()) {
            return false;
        }

        // 直接转换为 HashSet 比较
        return new HashSet<>(list1).equals(new HashSet<>(list2));
    }
}

