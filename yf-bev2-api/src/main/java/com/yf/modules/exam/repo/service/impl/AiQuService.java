package com.yf.modules.exam.repo.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.yf.base.api.exception.ServiceException;
import com.yf.base.utils.jackson.JsonHelper;
import com.keryo.training.common.security.XssSanitizeUtil;
import com.keryo.training.modules.aiconfig.service.AiConfigService;
import com.yf.modules.exam.repo.dto.RepoQuAnswerDTO;
import com.yf.modules.exam.repo.dto.request.RepoQuDetailDTO;
import com.yf.modules.exam.repo.service.RepoQuService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.*;

/**
 * AI 出题服务（对接 OpenAI 兼容接口，供应商/模型/Key 由管理端「AI 设置」配置）
 * <p>
 * 三个能力：
 * 1. 简答/填空 AI 生成参考答案
 * 2. AI 出题（输入主题生成完整题目 JSON）
 * 3. 根据培训（课件）内容 AI 出题 → 入库 DRAFT → 走两级审核
 */
@Log4j2
@Service
@RequiredArgsConstructor
public class AiQuService {

    private final RepoQuService repoQuService;
    private final AiConfigService aiConfigService;

    private static final Set<String> QUTYPES = Set.of("radio", "multi", "judge", "fill", "essay");

    /** 调用 AI 聊天补全（OpenAI 兼容协议），返回 AI 文本 */
    private String chat(String systemPrompt, String userPrompt, int maxTokens) {
        Map<String, String> ep = aiConfigService.effectiveEndpoint();
        if (ep == null) {
            throw new ServiceException("AI 服务未配置 API Key，请管理员在「系统设置 → AI 设置」中填写");
        }
        String apiKey = ep.get("apiKey");
        String baseUrl = ep.get("baseUrl");
        String model = ep.get("model");
        try {
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("model", model);
            List<Map<String, String>> messages = new ArrayList<>();
            messages.add(Map.of("role", "system", "content", systemPrompt));
            messages.add(Map.of("role", "user", "content", userPrompt));
            body.put("messages", messages);
            body.put("max_tokens", maxTokens);
            body.put("temperature", 0.7);

            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl))
                    .timeout(Duration.ofSeconds(60))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(JsonHelper.toJson(body), java.nio.charset.StandardCharsets.UTF_8))
                    .build();
            HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(15)).build();
            HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() != 200) {
                log.error("AI 接口错误: {} {}", resp.statusCode(), resp.body());
                throw new ServiceException("AI 服务调用失败，请稍后重试或检查 AI 设置");
            }
            JsonNode root = JsonHelper.getMapper().readTree(resp.body());
            String content = root.path("choices").path(0).path("message").path("content").asText("");
            return StringUtils.trim(content);
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("AI 服务异常", e);
            throw new ServiceException("AI 服务异常: " + e.getMessage());
        }
    }

    /** 剥离 AI 返回中的 ```json 代码块包裹 */
    private String stripCodeFence(String text) {
        if (text == null) {
            return "";
        }
        String t = text.trim();
        if (t.startsWith("```")) {
            t = t.replaceFirst("^```[a-zA-Z]*\\s*", "");
            t = t.replaceAll("```\\s*$", "");
        }
        // 提取第一个 { 到最后一个 }
        int s = t.indexOf('{');
        int e = t.lastIndexOf('}');
        if (s >= 0 && e > s) {
            return t.substring(s, e + 1);
        }
        return t;
    }

    /** 1. AI 生成参考答案（简答/填空等） */
    public String generateAnswer(String quType, String content) {
        if (StringUtils.isBlank(content)) {
            throw new ServiceException("请先填写题干");
        }
        String typeName = switch (quType == null ? "" : quType) {
            case "fill" -> "填空题（多个空答案用 | 分隔）";
            case "essay" -> "简答题";
            case "judge" -> "判断题";
            default -> "题目";
        };
        String sys = "你是血透中心护理培训的出题助手，专业知识扎实，回答简洁准确。";
        String user = "请为以下" + typeName + "生成参考答案（200字以内，专业准确）：\n" + content
                + "\n只返回答案文本，不要任何解释。";
        return chat(sys, user, 500);
    }

    /** 2. AI 出题：返回可直接填入表单的题目 JSON */
    public RepoQuDetailDTO generateQu(String topic, String quType, String difficulty) {
        // T-09.6 防御：topic 超长截断（防 token 耗尽/极大输入攻击）
        if (topic != null && topic.length() > 500) {
            topic = topic.substring(0, 500);
        }
        String typeName = switch (quType == null ? "" : quType) {
            case "multi" -> "多选题（正确选项2-3个）";
            case "judge" -> "判断题";
            case "fill" -> "填空题（题干含____，答案多个空用|分隔）";
            case "essay" -> "简答题";
            default -> "单选题（4个选项，1个正确）";
        };
        String diff = StringUtils.isBlank(difficulty) ? "中等" : difficulty;
        String sys = "你是血透中心护理培训的出题专家。只输出 JSON，不要输出任何其他内容。";
        String user = "请围绕主题《" + topic + "》出一道" + typeName + "，难度" + diff + "。\n"
                + "严格按以下 JSON 结构返回（不要代码块包裹）：\n"
                + "{\"content\":\"题干\",\"options\":[{\"content\":\"选项1\",\"isRight\":true},{\"content\":\"选项2\",\"isRight\":false}],\"answerText\":\"A\",\"analysis\":\"解析\"}\n"
                + "要求：content 为医学专业题干；options 为选项数组（判断题给【正确/错误】两个选项，isRight 标记正确答案）；"
                + "answerText 为正确选项字母（判断题√或×）；analysis 为解析；填空/简答 options 给空数组、answerText 填参考答案。";
        String raw = chat(sys, user, 900);
        try {
            JsonNode node = JsonHelper.getMapper().readTree(stripCodeFence(raw));
            RepoQuDetailDTO dto = new RepoQuDetailDTO();
            dto.setQuType(StringUtils.isBlank(quType) ? "radio" : quType);
            dto.setDifficultyLevel(diff);
            dto.setContent(XssSanitizeUtil.sanitize(node.path("content").asText("")));
            dto.setAnalysis(XssSanitizeUtil.sanitize(node.path("analysis").asText("")));
            List<RepoQuAnswerDTO> answers = new ArrayList<>();
            JsonNode opts = node.path("options");
            if (opts.isArray() && opts.size() > 0) {
                for (JsonNode o : opts) {
                    RepoQuAnswerDTO a = new RepoQuAnswerDTO();
                    a.setContent(XssSanitizeUtil.sanitize(o.path("content").asText("")));
                    a.setIsRight(o.path("isRight").asBoolean(false));
                    answers.add(a);
                }
            } else {
                // 填空/简答：answerText 作答案
                String ans = node.path("answerText").asText("");
                if (StringUtils.isNotBlank(ans)) {
                    if ("fill".equals(quType)) {
                        for (String blank : ans.split("\\|")) {
                            RepoQuAnswerDTO a = new RepoQuAnswerDTO();
                            a.setContent(blank.trim());
                            a.setIsRight(true);
                            answers.add(a);
                        }
                    } else {
                        RepoQuAnswerDTO a = new RepoQuAnswerDTO();
                        a.setContent(ans.trim());
                        a.setIsRight(true);
                        answers.add(a);
                    }
                }
            }
            dto.setAnswerList(answers);
            if (StringUtils.isBlank(dto.getContent()) || answers.isEmpty()) {
                throw new ServiceException("AI 生成内容不完整，请重试或调整主题");
            }
            return dto;
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("AI 出题 JSON 解析失败: {}", raw);
            throw new ServiceException("AI 出题解析失败，请重试");
        }
    }

    /** 3. 根据培训（课件）AI 出题 → 入库 DRAFT → 走两级审核
     * 支持两种模式：
     * - typeCounts + typeScores（Map: quType->数量/分值）：按精确的题型组合生成（每种多少、每题多少分可人为指定）
     * - 否则：count 总数 + quTypes 轮询分配（兼容旧调用）
     */
    public Map<String, Object> generateFromCourseware(String repoId, String coursewareTitle, String topic,
                                                      Integer count, String quTypes,
                                                      Map<String, Integer> typeCounts, Map<String, BigDecimal> typeScores) {
        if (StringUtils.isBlank(repoId)) {
            throw new ServiceException("请选择目标题库");
        }

        // 题型数量计划：typeCounts 优先，否则轮询均分
        List<PlanItem> plan = new ArrayList<>();
        if (typeCounts != null && !typeCounts.isEmpty()) {
            for (Map.Entry<String, Integer> e : typeCounts.entrySet()) {
                String qt = normalizeType(e.getKey());
                if (qt != null && e.getValue() != null && e.getValue() > 0) {
                    BigDecimal score = typeScores == null ? null : typeScores.get(qt);
                    if (score == null) {
                        score = typeScores == null ? null : typeScores.get(typeName(qt));
                    }
                    plan.add(new PlanItem(qt, Math.min(20, e.getValue()), score));
                }
            }
        }
        if (plan.isEmpty()) {
            int n = count == null ? 5 : Math.max(1, Math.min(20, count));
            List<String> typeSeq = resolveTypes(quTypes);
            for (int i = 0; i < n; i++) {
                plan.add(new PlanItem(typeSeq.get(i % typeSeq.size()), 1, null));
            }
        }

        int success = 0;
        int fail = 0;
        List<String> errors = new ArrayList<>();
        StringBuilder usedStems = new StringBuilder(); // 已生成题干，用于去重
        int guard = 0;
        int totalPlanned = plan.stream().mapToInt(PlanItem::count).sum();
        int made = 0;
        outer:
        for (PlanItem item : plan) {
            for (int i = 0; i < item.count(); i++) {
                if (guard >= totalPlanned * 3) {
                    break outer;
                }
                guard++;
                made++;
                String qt = item.type();
                RepoQuDetailDTO dto = generateOne(repoId, coursewareTitle, topic, qt, item.score(), usedStems.toString());
                if (dto == null) {
                    fail++;
                    errors.add("第" + made + "题(" + typeName(qt) + ") AI 生成失败，已跳过");
                    continue;
                }
                try {
                    dto.setRepoId(repoId);
                    repoQuService.save(dto); // 内部置 DRAFT 待审核
                    success++;
                    usedStems.append(made).append(". ").append(dto.getContent()).append("\n");
                } catch (Exception ex) {
                    fail++;
                    errors.add(ex.getMessage());
                }
            }
        }
        Map<String, Object> res = new LinkedHashMap<>();
        res.put("total", success + fail);
        res.put("success", success);
        res.put("fail", fail);
        res.put("errors", errors);
        return res;
    }

    /** 生成计划项：题型 + 数量 + 每题分值 */
    private record PlanItem(String type, int count, BigDecimal score) {
    }

    /** 规范化题型编码（兼容中文/英文） */
    private String normalizeType(String t) {
        if (t == null) return null;
        if (t.contains("multi") || t.contains("多选")) return "multi";
        if (t.contains("radio") || t.contains("单选")) return "radio";
        if (t.contains("judge") || t.contains("判断")) return "judge";
        if (t.contains("fill") || t.contains("填空")) return "fill";
        if (t.contains("essay") || t.contains("简答")) return "essay";
        return null;
    }

    /** 题型中文名 */
    private String typeName(String qt) {
        return switch (qt == null ? "" : qt) {
            case "multi" -> "多选题";
            case "judge" -> "判断题";
            case "fill" -> "填空题";
            case "essay" -> "简答题";
            default -> "单选题";
        };
    }

    /** 解析题型选择为轮询序列（quTypes 如"单选、判断、填空、简答混合"/"单选题"/null） */
    private List<String> resolveTypes(String quTypes) {
        List<String> seq = new ArrayList<>();
        String t = quTypes == null ? "" : quTypes;
        if (t.contains("多选")) {
            seq.add("multi");
        }
        if (t.contains("单选")) {
            seq.add("radio");
        }
        if (t.contains("判断")) {
            seq.add("judge");
        }
        if (t.contains("填空")) {
            seq.add("fill");
        }
        if (t.contains("简答")) {
            seq.add("essay");
        }
        if (seq.isEmpty()) {
            seq.addAll(List.of("radio", "judge", "fill", "essay")); // 默认混合
        }
        return seq;
    }

    /** 生成单题（指定题型 + 每题分值 + 已出题去重；AI 输出不合法返回 null） */
    private RepoQuDetailDTO generateOne(String repoId, String coursewareTitle, String topic,
                                        String quType, BigDecimal score, String usedStems) {
        // T-09.6 防御：topic 超长截断
        if (topic != null && topic.length() > 500) {
            topic = topic.substring(0, 500);
        }
        if (coursewareTitle != null && coursewareTitle.length() > 200) {
            coursewareTitle = coursewareTitle.substring(0, 200);
        }
        String typeName = switch (quType == null ? "" : quType) {
            case "multi" -> "多选题（正确选项2-3个）";
            case "judge" -> "判断题";
            case "fill" -> "填空题（题干含____，答案多个空用|分隔）";
            case "essay" -> "简答题";
            default -> "单选题（4个选项，1个正确）";
        };
        String sys = "你是血透中心护理培训的出题专家。只输出 JSON 对象，不要输出任何其他内容。";
        String dedup = StringUtils.isBlank(usedStems) ? "" : "\n已出过的题（请避免与其雷同）：\n" + usedStems;
        String user = "基于培训《" + coursewareTitle + "》的主题内容：" + topic
                + "\n请出1道" + typeName + "，难度中等。" + dedup + "\n"
                + "严格按以下 JSON 结构返回（不要代码块包裹）：\n"
                + "{\"quType\":\"" + quType + "\",\"content\":\"题干\",\"options\":[{\"content\":\"选项1\",\"isRight\":true},{\"content\":\"选项2\",\"isRight\":false}],\"answerText\":\"A\",\"analysis\":\"解析\"}\n"
                + "要求：content 为医学专业题干且与已出题不同；判断题 options 给【正确/错误】、answerText 给√或×；"
                + "填空/简答 options 给空数组、answerText 给参考答案；必须含 analysis 解析。";
        String raw = chat(sys, user, 900);
        try {
            JsonNode node = JsonHelper.getMapper().readTree(stripCodeFence(raw));
            RepoQuDetailDTO dto = new RepoQuDetailDTO();
            String qt = node.path("quType").asText(quType);
            dto.setQuType(QUTYPES.contains(qt) ? qt : quType);
            dto.setDifficultyLevel("中等");
            dto.setScore(score); // 题目默认分值（AI 出题时人为指定）
            dto.setContent(XssSanitizeUtil.sanitize(node.path("content").asText("")));
            dto.setAnalysis(XssSanitizeUtil.sanitize(node.path("analysis").asText("")));
            List<RepoQuAnswerDTO> answers = new ArrayList<>();
            JsonNode opts = node.path("options");
            if (opts.isArray() && opts.size() > 0) {
                for (JsonNode o : opts) {
                    RepoQuAnswerDTO a = new RepoQuAnswerDTO();
                    a.setContent(XssSanitizeUtil.sanitize(o.path("content").asText("")));
                    a.setIsRight(o.path("isRight").asBoolean(false));
                    answers.add(a);
                }
            } else {
                String ans = node.path("answerText").asText("");
                if (StringUtils.isNotBlank(ans)) {
                    if ("fill".equals(dto.getQuType())) {
                        for (String b : ans.split("\\|")) {
                            RepoQuAnswerDTO a = new RepoQuAnswerDTO();
                            a.setContent(b.trim());
                            a.setIsRight(true);
                            answers.add(a);
                        }
                    } else {
                        RepoQuAnswerDTO a = new RepoQuAnswerDTO();
                        a.setContent(ans.trim());
                        a.setIsRight(true);
                        answers.add(a);
                    }
                }
            }
            if (StringUtils.isBlank(dto.getContent()) || answers.isEmpty()) {
                return null;
            }
            dto.setAnswerList(answers);
            return dto;
        } catch (Exception e) {
            log.error("AI 单题生成解析失败: {}", raw.length());
            return null;
        }
    }
}

