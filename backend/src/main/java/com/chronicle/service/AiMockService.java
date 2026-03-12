package com.chronicle.service;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Random;

@Service
public class AiMockService {

    private static final String[] GENERATED_TEMPLATES = {
        "【%s】\n\n%d年，%s扎实推进各项工作，取得显著成效。全年共完成重点任务%d项，完成率达%d%%，各项核心指标均超额完成年度计划目标。\n\n在工作推进方面，积极贯彻落实上级决策部署，坚持问题导向，统筹推进各项工作任务。全年召开专题会议%d次，研究解决重要问题%d个，形成工作方案%d份。\n\n在创新发展方面，深化体制机制改革，推动工作提质增效。全年推出创新举措%d项，有效提升了工作效率和服务水平，得到上级部门充分肯定。\n\n在成效评估方面，通过年终考核评估，综合得分位居全市同类单位前列，多项工作获得市级以上表彰奖励，社会满意度持续提升。",
        "【%s】\n\n%d年度，%s认真贯彻党的路线方针政策，紧紧围绕年度中心工作，全力推进各项重点任务落实落地，各项工作呈现良好发展态势。\n\n一、主要工作完成情况\n全年完成各类工作任务%d项，其中重点任务完成率%d%%，一般任务完成率%d%%，超额完成年度工作计划。\n\n二、重点工作亮点\n在重点工作推进过程中，创新工作方法，强化协调联动，形成了一批具有推广价值的工作经验和典型案例，多次在市级以上会议上作交流发言。\n\n三、存在问题与改进方向\n当前工作中仍存在一些不足之处，下一步将持续深化改革创新，不断提升工作质量和水平，为全市高质量发展贡献更大力量。"
    };

    public String generateEntry(String rawData, String historyData) {
        Random rand = new Random();
        String template = GENERATED_TEMPLATES[rand.nextInt(GENERATED_TEMPLATES.length)];
        int year = 2024;
        String subject = "相关单位";
        if (rawData != null && !rawData.isBlank()) {
            String[] words = rawData.split("[\\s，。、]");
            if (words.length > 0) subject = words[0].length() > 10 ? "相关单位" : words[0];
        }
        return String.format(template, subject, year, subject,
                80 + rand.nextInt(20), 90 + rand.nextInt(10),
                15 + rand.nextInt(10), 30 + rand.nextInt(20), 20 + rand.nextInt(15),
                5 + rand.nextInt(5));
    }

    public String rewrite(String content) {
        if (content == null || content.isBlank()) return content;
        return "【润色后】\n\n" + content
                .replaceAll("（", "。经优化调整，")
                .replaceAll("进行了", "扎实推进了")
                .replaceAll("完成了", "高质量完成了")
                .replaceAll("开展了", "深入开展了")
                + "\n\n（以上内容已经AI润色，请根据实际情况进行审核修改。）";
    }

    public String expand(String content) {
        if (content == null || content.isBlank()) return content;
        return content + "\n\n在具体推进过程中，相关部门建立健全了工作机制，明确责任分工，强化督导落实，确保各项工作任务按时按质完成。" +
                "通过系统总结经验做法，形成了一套可复制、可推广的工作模式，为下一阶段工作的深入推进奠定了坚实基础。\n\n" +
                "（以上为AI扩写内容，请根据实际情况进行审核修改。）";
    }

    public List<Map<String, Object>> detectConflicts(String content) {
        return List.of(
                Map.of("type", "数据核实", "description", "文中提到的统计数据需要与原始资料进行核对",
                        "location", "正文第二段", "severity", "medium"),
                Map.of("type", "时间核实", "description", "年份或时间节点描述需要确认准确性",
                        "location", "正文第一段", "severity", "low"),
                Map.of("type", "表述规范", "description", "部分词语表述建议参照年鉴规范用语进行调整",
                        "location", "正文第三段", "severity", "low")
        );
    }

    public String botAnswer(String question) {
        if (question == null || question.isBlank()) return "请输入您的问题。";
        Map<String, String> qaMap = Map.of(
                "格式", "年鉴条目通常采用「标题+正文」结构，正文以第三人称客观叙述为主，避免主观评价性语言。",
                "字数", "一般条目字数要求在500-3000字之间，具体以大纲分配要求为准。",
                "数据", "数据引用需标注来源，数字统一使用阿拉伯数字，万亿等大数字可使用汉字单位。",
                "时间", "时间表述统一采用「年内」「截至年底」「同比」等规范用语。",
                "风格", "年鉴文体要求客观平实、简洁凝练，避免华丽辞藻和情感性表达。"
        );
        for (Map.Entry<String, String> entry : qaMap.entrySet()) {
            if (question.contains(entry.getKey())) {
                return entry.getValue();
            }
        }
        return "根据您的问题，建议参考《地方志工作条例》及年鉴编纂规范手册中的相关规定。如需更具体的指导，请联系系统管理员或参阅年鉴编纂说明文档。";
    }
}
