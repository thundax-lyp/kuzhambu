package com.thundax.kuzhambu.discovery.application.qa.support;

import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class KnowledgeItemTextRenderer {

    public String render(KnowledgeDocument.Knowledge knowledge) {
        if (knowledge == null) {
            return "";
        }
        StringBuilder text = new StringBuilder();
        appendLabeledField(text, "标题", knowledge.title());
        appendLabeledField(text, "位置", knowledge.categoryPath());
        appendLabeledField(text, "摘要", knowledge.summary());
        appendLabeledField(text, "正文", knowledge.body());
        appendLabeledField(text, "原文", knowledge.originalText());
        appendLabeledField(text, "译文", knowledge.translationText());
        appendLabeledField(text, "原文摘录", knowledge.originalExcerpts());
        appendTagField(text, knowledge.tags());
        appendQaPairs(text, knowledge.qaPairs());
        return text.toString().trim();
    }

    private void appendLabeledField(StringBuilder text, String label, String value) {
        if (StringUtils.isBlank(value)) {
            return;
        }
        text.append(label).append("：").append(value).append('\n');
    }

    private void appendTagField(StringBuilder text, List<String> tags) {
        List<String> validTags = validStrings(tags);
        if (validTags.isEmpty()) {
            return;
        }
        text.append("标签：").append(String.join("、", validTags)).append('\n');
    }

    private void appendQaPairs(StringBuilder text, List<KnowledgeDocument.QaPair> qaPairs) {
        List<KnowledgeDocument.QaPair> validQaPairs = qaPairs == null
                ? List.of()
                : qaPairs.stream().filter(this::isValidQaPair).toList();
        if (validQaPairs.isEmpty()) {
            return;
        }
        text.append('\n');
        text.append("已确认问答：").append('\n');
        for (KnowledgeDocument.QaPair qaPair : validQaPairs) {
            text.append("Q: ").append(qaPair.question()).append('\n');
            text.append("A: ").append(qaPair.answer()).append('\n');
        }
    }

    private boolean isValidQaPair(KnowledgeDocument.QaPair qaPair) {
        return qaPair != null && StringUtils.isNotBlank(qaPair.question()) && StringUtils.isNotBlank(qaPair.answer());
    }

    private List<String> validStrings(List<String> values) {
        if (values == null || values.isEmpty()) {
            return List.of();
        }
        return values.stream().filter(StringUtils::isNotBlank).toList();
    }
}
