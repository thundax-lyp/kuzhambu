package com.thundax.kuzhambu.common.security.permission;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.Test;

public class PrefixPermissionMatcherTest {

    private final PrefixPermissionMatcher matcher = new PrefixPermissionMatcher();

    @Test
    public void shouldMatchSamePermission() {
        assertTrue(matcher.matches("sys:role:view", "sys:role:view"));
    }

    @Test
    public void shouldMatchChildPermissionWhenParentPermissionExists() {
        assertTrue(matcher.matches("sys:role", "sys:role:view"));
        assertTrue(matcher.matches("sys", "sys:role:view"));
    }

    @Test
    public void shouldNotMatchParentPermissionWhenOnlyChildPermissionExists() {
        assertFalse(matcher.matches("sys:role:view", "sys:role"));
    }

    @Test
    public void shouldNotMatchPermissionWithDifferentSegmentPrefix() {
        assertFalse(matcher.matches("sys:role", "sys:roleManage:view"));
    }

    @Test
    public void shouldMatchAnyOwnedPermission() {
        assertTrue(matcher.matches(Arrays.asList("user", "sys:role"), "sys:role:edit"));
    }

    @Test
    public void shouldRejectBlankOrEmptyPermission() {
        assertFalse(matcher.matches(Collections.<String>emptyList(), "sys:role:view"));
        assertFalse(matcher.matches(Arrays.asList(" ", null), "sys:role:view"));
        assertFalse(matcher.matches("sys:role", " "));
    }
}
