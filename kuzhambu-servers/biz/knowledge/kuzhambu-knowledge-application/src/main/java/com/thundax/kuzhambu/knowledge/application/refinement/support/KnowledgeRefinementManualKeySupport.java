package com.thundax.kuzhambu.knowledge.application.refinement.support;

import java.security.SecureRandom;
import org.springframework.stereotype.Component;

@Component
public class KnowledgeRefinementManualKeySupport {

    private static final char[] CROCKFORD = "0123456789ABCDEFGHJKMNPQRSTVWXYZ".toCharArray();
    private final SecureRandom secureRandom = new SecureRandom();

    public String nextEntityKey() {
        return "manual:entity:" + nextUlid();
    }

    public String nextRelationKey() {
        return "manual:relation:" + nextUlid();
    }

    public String nextLineageNodeKey() {
        return "manual:lineage-node:" + nextUlid();
    }

    public String nextLineageRelationKey() {
        return "manual:lineage-relation:" + nextUlid();
    }

    private String nextUlid() {
        char[] chars = new char[26];
        long timestamp = System.currentTimeMillis();
        for (int index = 9; index >= 0; index--) {
            chars[index] = CROCKFORD[(int) (timestamp & 31)];
            timestamp >>>= 5;
        }
        for (int index = 10; index < chars.length; index++) {
            chars[index] = CROCKFORD[secureRandom.nextInt(CROCKFORD.length)];
        }
        return new String(chars);
    }
}
