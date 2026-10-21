package com.yf.modules.exam.repo.service.impl;

import com.yf.modules.exam.repo.dto.RepoQuAnswerDTO;
import com.yf.modules.exam.repo.dto.request.RepoQuDetailDTO;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Word(.docx) 题库解析器
 * <p>
 * 兼容血透中心真实考核文档的排版变体：
 * A. 题干行 / 选项行 / 答案区"答案：C 解析：xx"（每行一题）
 * B. 题干+选项同行（题干 + 多空格 + A. xx  B. xx）/ 答案区"× 解析：xx"或"B 解析：xx"
 * C. 答案区单行紧凑"一、判断题：1.√（解析：xx）；2.×（解析：xx）"
 * D. 答案区"一、选择题：1.B  2.B  3.C"数字-答案对，判断附（备注）
 */
public class DocxQuParser {

    private static final Map<String, String> TYPE_MAP = new LinkedHashMap<>();

    static {
        TYPE_MAP.put("单选", "radio");
        TYPE_MAP.put("单项选择题", "radio");
        TYPE_MAP.put("单选题", "radio");
        TYPE_MAP.put("选择题", "radio");
        TYPE_MAP.put("多选", "multi");
        TYPE_MAP.put("多项选择题", "multi");
        TYPE_MAP.put("多选题", "multi");
        TYPE_MAP.put("判断", "judge");
        TYPE_MAP.put("判断题", "judge");
        TYPE_MAP.put("填空", "fill");
        TYPE_MAP.put("填空题", "fill");
        TYPE_MAP.put("简答", "essay");
        TYPE_MAP.put("简答题", "essay");
    }

    private static final Pattern TYPE_HEAD = Pattern.compile("^[一二三四五六七八九十]+[、.．]\\s*(.+?)(?:[（(]|[:：]|$)");
    private static final Pattern OPT_INLINE = Pattern.compile("([A-H])\\s*[.、．]\\s*(.+?)(?=\\s+[A-H]\\s*[.、．]|$)");
    private static final Pattern ANS_LINE = Pattern.compile("^答案[:：]\\s*([A-H√×]+)\\s*(?:解析[:：]?\\s*(.*))?$", Pattern.DOTALL);
    private static final Pattern ANS_SIMPLE = Pattern.compile("^([A-H√×])\\s*(?:解析[:：]?\\s*(.*))?$", Pattern.DOTALL);
    private static final Pattern ANS_COMPACT = Pattern.compile("(\\d+)\\s*[.．、]\\s*([A-H√×]+)\\s*[（(]?\\s*解析[:：]?\\s*(.*?)\\s*[）)]");
    private static final Pattern ANS_NUM = Pattern.compile("(\\d+)\\s*[.．、]\\s*([A-H√×])");
    private static final Pattern STEM_PAT = Pattern.compile("[（(]\\s*[）)]|_{2,}");
    private static final Pattern NUM_PREFIX = Pattern.compile("^\\d+\\s*[.、．]\\s*");

    /** 单题解析中间结构 */
    private static class ParsedQu {
        String type;
        String stem = "";
        List<String[]> options = new ArrayList<>(); // [字母, 文本]
        String answer = "";
        String analysis = "";
    }

    /** 解析 docx 提取的段落文本，输出题目 DTO 列表 */
    public static List<RepoQuDetailDTO> parse(List<String> lines) {
        List<ParsedQu> quList = new ArrayList<>();
        String curType = null;
        ParsedQu cur = null;
        boolean inAnswer = false;

        // ---------- 第一遍：正文题干/选项 ----------
        for (String raw : lines) {
            String line = raw.trim();
            if (line.isEmpty() || isSkip(line)) {
                continue;
            }
            if (line.startsWith("参考答案") || line.startsWith("答案及解析")
                    || line.startsWith("答案：") || line.startsWith("答案:")) {
                inAnswer = true;
                flush(quList, cur);
                cur = null;
                continue;
            }
            if (inAnswer) {
                continue;
            }
            Matcher tm = TYPE_HEAD.matcher(line);
            if (tm.find()) {
                flush(quList, cur);
                cur = null;
                String key = tm.group(1);
                curType = mapType(key);
                if (curType != null) {
                    cur = new ParsedQu();
                    cur.type = curType;
                }
                continue;
            }
            // 纯选项行
            if (cur != null && Pattern.matches("^[A-H]\\s*[.、．].*", line)) {
                cur.options.addAll(splitInlineOptions(line));
                continue;
            }
            // 题干行（含选项拆分）
            Matcher optM = Pattern.compile("[A-H]\\s*[.、．]").matcher(line);
            if (optM.find()) {
                String stemPart = line.substring(0, optM.start()).trim();
                List<String[]> opts = splitInlineOptions(line.substring(optM.start()));
                if (!opts.isEmpty()) {
                    flush(quList, cur);
                    cur = new ParsedQu();
                    cur.type = curType == null ? "radio" : curType;
                    cur.stem = NUM_PREFIX.matcher(stemPart).replaceFirst("");
                    cur.options.addAll(opts);
                    continue;
                }
            }
            if (STEM_PAT.matcher(line).find() || line.endsWith("？") || line.endsWith("?")) {
                flush(quList, cur);
                cur = new ParsedQu();
                cur.type = curType == null ? "radio" : curType;
                cur.stem = NUM_PREFIX.matcher(line).replaceFirst("");
                continue;
            }
            // 题干续行
            if (cur != null && cur.options.isEmpty()) {
                cur.stem = (cur.stem + "\n" + line).trim();
            }
        }
        flush(quList, cur);

        // ---------- 第二遍：答案区 ----------
        Map<String, List<String[]>> ansByType = new LinkedHashMap<>();
        for (String t : TYPE_MAP.values()) {
            ansByType.put(t, new ArrayList<>());
        }
        String curType2 = null;
        for (String raw : lines) {
            String line = raw.trim();
            if (line.isEmpty() || line.startsWith("参考答案") || line.startsWith("答案及解析")) {
                continue;
            }
            Matcher tm = TYPE_HEAD.matcher(line);
            if (tm.find()) {
                String t2 = mapType(tm.group(1));
                if (t2 != null) {
                    curType2 = t2;
                }
                String rest = line.substring(tm.end()).replaceAll("^[：:\\s]+", "");
                if (rest.isEmpty() || !ANS_NUM.matcher(rest).find()) {
                    continue;
                }
                line = rest;
            }
            if (curType2 == null) {
                continue;
            }
            List<String[]> answers = ansByType.get(curType2);
            // 变体D：数字-答案对
            Matcher numM = ANS_NUM.matcher(line);
            if (numM.find()) {
                numM.reset();
                while (numM.find()) {
                    String ans = numM.group(2).trim();
                    String seg = "";
                    Matcher nxt = ANS_NUM.matcher(line);
                    if (nxt.find(numM.end())) {
                        seg = line.substring(numM.end(), nxt.start());
                    } else {
                        seg = line.substring(numM.end());
                    }
                    seg = seg.replaceAll("[（(]|）|\\)", "").trim();
                    if (seg.length() > 60) {
                        seg = seg.substring(0, 60);
                    }
                    answers.add(new String[]{ans, seg});
                }
                continue;
            }
            // 变体C：1.√（解析：xx）；2.×（解析：xx）
            Matcher compM = ANS_COMPACT.matcher(line);
            if (compM.find()) {
                compM.reset();
                while (compM.find()) {
                    answers.add(new String[]{compM.group(2).trim(), compM.group(3).trim()});
                }
                continue;
            }
            // 变体A：答案：C 解析：xx
            Matcher aM = ANS_LINE.matcher(line);
            if (aM.find()) {
                answers.add(new String[]{aM.group(1).trim(), aM.group(2) == null ? "" : aM.group(2).trim()});
                continue;
            }
            // 变体B：× 解析：xx / B 解析：xx
            Matcher sM = ANS_SIMPLE.matcher(line);
            if (sM.find()) {
                answers.add(new String[]{sM.group(1).trim(), sM.group(2) == null ? "" : sM.group(2).trim()});
            }
        }

        // 回填答案
        for (Map.Entry<String, List<String[]>> e : ansByType.entrySet()) {
            int idx = 0;
            for (ParsedQu q : quList) {
                if (q.type.equals(e.getKey())) {
                    if (idx < e.getValue().size()) {
                        q.answer = e.getValue().get(idx)[0];
                        q.analysis = e.getValue().get(idx)[1];
                    }
                    idx++;
                }
            }
        }

        // 转 DTO
        List<RepoQuDetailDTO> result = new ArrayList<>();
        for (ParsedQu q : quList) {
            RepoQuDetailDTO dto = toDto(q);
            if (dto != null) {
                result.add(dto);
            }
        }
        return result;
    }

    private static void flush(List<ParsedQu> quList, ParsedQu cur) {
        if (cur != null && (StringUtils.isNotBlank(cur.stem) || !cur.options.isEmpty())) {
            quList.add(cur);
        }
    }

    private static String mapType(String key) {
        if (key == null) {
            return null;
        }
        for (Map.Entry<String, String> e : TYPE_MAP.entrySet()) {
            if (key.contains(e.getKey())) {
                return e.getValue();
            }
        }
        return null;
    }

    private static boolean isSkip(String line) {
        if (line.contains("（注") || line.contains("(注") || line.contains("注：")) {
            return true;
        }
        if (line.contains("考核题") && line.length() < 30 && !TYPE_HEAD.matcher(line).find()) {
            return true;
        }
        return line.startsWith("考试说明") || line.startsWith("考核说明");
    }

    private static List<String[]> splitInlineOptions(String line) {
        List<String[]> opts = new ArrayList<>();
        Matcher m = OPT_INLINE.matcher(line);
        while (m.find()) {
            opts.add(new String[]{m.group(1), m.group(2).trim()});
        }
        return opts;
    }

    /** 组装题目 DTO；校验失败返回 null */
    private static RepoQuDetailDTO toDto(ParsedQu q) {
        if (StringUtils.isBlank(q.stem)) {
            return null;
        }
        RepoQuDetailDTO dto = new RepoQuDetailDTO();
        dto.setQuType(q.type);
        dto.setDifficultyLevel("easy");
        dto.setContent(q.stem);
        dto.setAnalysis(q.analysis);

        List<RepoQuAnswerDTO> answers = new ArrayList<>();
        switch (q.type) {
            case "radio":
            case "multi":
                for (String[] opt : q.options) {
                    RepoQuAnswerDTO a = new RepoQuAnswerDTO();
                    a.setContent(opt[1]);
                    a.setIsRight(q.answer != null && q.answer.contains(opt[0]));
                    answers.add(a);
                }
                break;
            case "judge":
                // 判断题固定 [正确, 错误]，√=正确、×=错误
                boolean rightIsCorrect = q.answer != null && q.answer.contains("√");
                RepoQuAnswerDTO t = new RepoQuAnswerDTO();
                t.setContent("正确");
                t.setIsRight(rightIsCorrect);
                answers.add(t);
                RepoQuAnswerDTO f = new RepoQuAnswerDTO();
                f.setContent("错误");
                f.setIsRight(!rightIsCorrect);
                answers.add(f);
                break;
            case "fill":
                // 多空答案以 | 分隔
                for (String blank : q.answer.split("\\|")) {
                    RepoQuAnswerDTO a = new RepoQuAnswerDTO();
                    a.setContent(blank.trim());
                    a.setIsRight(true);
                    answers.add(a);
                }
                break;
            case "essay":
                RepoQuAnswerDTO e = new RepoQuAnswerDTO();
                e.setContent(q.answer);
                e.setIsRight(true);
                answers.add(e);
                break;
            default:
                return null;
        }
        if (answers.isEmpty()) {
            return null;
        }
        dto.setAnswerList(answers);
        return dto;
    }
}

