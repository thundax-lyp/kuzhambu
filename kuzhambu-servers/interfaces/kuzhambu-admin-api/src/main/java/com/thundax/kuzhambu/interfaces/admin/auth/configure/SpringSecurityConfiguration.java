package com.thundax.kuzhambu.interfaces.admin.auth.configure;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thundax.kuzhambu.biz.core.service.UserService;
import com.thundax.kuzhambu.common.security.annotation.PublicApi;
import com.thundax.kuzhambu.interfaces.admin.auth.security.filter.AccessTokenAuthenticationFilter;
import com.thundax.kuzhambu.interfaces.admin.auth.service.AdminAuthService;
import com.thundax.kuzhambu.interfaces.admin.auth.service.PermissionService;
import com.thundax.kuzhambu.interfaces.admin.configure.KuzhambuProperties;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

@Configuration
@EnableMethodSecurity
public class SpringSecurityConfiguration {

    private final KuzhambuProperties properties;
    private final AdminAuthService authService;
    private final PermissionService permissionService;
    private final UserService userService;
    private final RequestMappingHandlerMapping requestMappingHandlerMapping;
    private final ObjectMapper objectMapper;

    public SpringSecurityConfiguration(
            KuzhambuProperties properties,
            AdminAuthService authService,
            PermissionService permissionService,
            UserService userService,
            RequestMappingHandlerMapping requestMappingHandlerMapping,
            ObjectMapper objectMapper) {
        this.properties = properties;
        this.authService = authService;
        this.permissionService = permissionService;
        this.userService = userService;
        this.requestMappingHandlerMapping = requestMappingHandlerMapping;
        this.objectMapper = objectMapper;
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        List<String> excludePaths = accessTokenExcludePaths();
        return web -> {
            if (!excludePaths.isEmpty()) {
                web.ignoring().requestMatchers(excludePaths.toArray(new String[0]));
            }
        };
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http.csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(requests -> requests.requestMatchers("/api/**")
                        .authenticated()
                        .anyRequest()
                        .permitAll())
                .addFilterBefore(
                        new AccessTokenAuthenticationFilter(
                                accessTokenExcludePaths(), authService, permissionService, userService, objectMapper),
                        UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    private List<String> accessTokenExcludePaths() {
        List<String> excludePaths =
                new ArrayList<>(properties.getAccessTokenFilter().getExcludePath());
        excludePaths.addAll(publicApiPaths());
        return excludePaths;
    }

    private List<String> publicApiPaths() {
        if (requestMappingHandlerMapping == null) {
            return Collections.emptyList();
        }

        List<String> paths = new ArrayList<>();
        for (Map.Entry<RequestMappingInfo, HandlerMethod> entry :
                requestMappingHandlerMapping.getHandlerMethods().entrySet()) {
            HandlerMethod handlerMethod = entry.getValue();
            if (!isPublicApi(handlerMethod)) {
                continue;
            }

            if (entry.getKey().getPathPatternsCondition() != null) {
                paths.addAll(entry.getKey().getPathPatternsCondition().getPatternValues());
            } else if (entry.getKey().getPatternsCondition() != null) {
                paths.addAll(entry.getKey().getPatternsCondition().getPatterns());
            }
        }
        return paths;
    }

    private boolean isPublicApi(HandlerMethod handlerMethod) {
        return AnnotationUtils.findAnnotation(handlerMethod.getBeanType(), PublicApi.class) != null
                || AnnotationUtils.findAnnotation(handlerMethod.getMethod(), PublicApi.class) != null;
    }
}
