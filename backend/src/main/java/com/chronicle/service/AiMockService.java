package com.chronicle.service;

import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class AiMockService {

    public String generateEntry(String rawData, String historyData) {
        return "<p>【AI生成条目】</p>" +
                "<p>根据提供的原始数据分析，2024年度相关工作取得显著进展。全年共完成重点项目12个，同比增长15.3%。其中，基础设施建设投资达到128.5亿元。</p>" +
                "<p>在政策推进方面，先后出台配套文件8项。全年地区生产总值实现稳步增长，城镇居民人均可支配收入达到52800元，增长6.2%。</p>";
    }

    public String rewrite(String text) {
        return "<p>【AI润色结果】</p><p>" + stripHtml(text).substring(0, Math.min(100, stripHtml(text).length())) +
                "……经AI润色优化后，文本表述更加规范、精炼，符合年鉴编纂规范要求。</p>";
    }

    public String expand(String text) {
        return "<p>【AI扩写结果】</p><p>" + stripHtml(text).substring(0, Math.min(80, stripHtml(text).length())) +
                "</p><p>在此基础上，进一步深化改革措施，推动高质量发展取得新突破。产业结构持续优化，第三产业增加值占比提升至54.3%。</p>";
    }

    public List<Map<String, Object>> detectConflicts(String content) {
        return List.of(
                Map.of("type", "数据冲突", "description", "GDP数据与统计局公报数据不一致（偏差2.3%）", "location", "第2段第3行", "severity", "high"),
                Map.of("type", "时间错误", "description", "事件发生时间与上下文不符", "location", "第3段第1行", "severity", "medium"),
                Map.of("type", "格式规范", "description", "百分比数据应保留一位小数", "location", "第1段第2行", "severity", "low")
        );
    }

    public String botAnswer(String question) {
        return "关于「" + question + "」的回答：根据年鉴编纂规范和相关数据分析，建议参考最新统计年报数据，结合实际工作情况进行核实。";
    }

    private String stripHtml(String html) {
        return html == null ? "" : html.replaceAll("<[^>]*>", "");
    }
}
