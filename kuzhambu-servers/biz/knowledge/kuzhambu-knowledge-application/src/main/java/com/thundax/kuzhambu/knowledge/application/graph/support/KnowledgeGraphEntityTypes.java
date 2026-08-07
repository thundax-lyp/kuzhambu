package com.thundax.kuzhambu.knowledge.application.graph.support;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

public final class KnowledgeGraphEntityTypes {

    public static final String PERSON = "人物";
    public static final String WORK = "作品";
    public static final String PLACE = "地点";
    public static final String TIME = "时间";
    public static final String EVENT = "事件";
    public static final String CONCEPT = "概念";
    public static final String OTHER = "其他";
    public static final List<String> VALUES = List.of(PERSON, WORK, PLACE, TIME, EVENT, CONCEPT, OTHER);

    private static final Map<String, String> ALIASES = Map.ofEntries(
            Map.entry(PERSON, PERSON),
            Map.entry("PERSON", PERSON),
            Map.entry("PEOPLE", PERSON),
            Map.entry("人物实体", PERSON),
            Map.entry(WORK, WORK),
            Map.entry("WORK", WORK),
            Map.entry("BOOK", WORK),
            Map.entry("作品实体", WORK),
            Map.entry(PLACE, PLACE),
            Map.entry("PLACE", PLACE),
            Map.entry("LOCATION", PLACE),
            Map.entry("地点实体", PLACE),
            Map.entry(TIME, TIME),
            Map.entry("TIME", TIME),
            Map.entry("DATE", TIME),
            Map.entry("时间实体", TIME),
            Map.entry(EVENT, EVENT),
            Map.entry("EVENT", EVENT),
            Map.entry("事件实体", EVENT),
            Map.entry(CONCEPT, CONCEPT),
            Map.entry("CONCEPT", CONCEPT),
            Map.entry("TERM", CONCEPT),
            Map.entry("概念实体", CONCEPT),
            Map.entry(OTHER, OTHER),
            Map.entry("OTHER", OTHER),
            Map.entry("UNKNOWN", OTHER),
            Map.entry("未分类", OTHER));

    private KnowledgeGraphEntityTypes() {}

    public static String normalize(String value) {
        if (StringUtils.isBlank(value)) {
            return OTHER;
        }
        String trimmed = value.trim();
        String normalized = ALIASES.get(trimmed);
        if (normalized != null) {
            return normalized;
        }
        normalized = ALIASES.get(trimmed.toUpperCase(Locale.ROOT));
        return normalized == null ? OTHER : normalized;
    }
}
