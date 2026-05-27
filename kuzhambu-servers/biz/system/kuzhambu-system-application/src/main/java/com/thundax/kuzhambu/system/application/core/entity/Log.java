package com.thundax.kuzhambu.system.application.core.entity;

import com.thundax.kuzhambu.system.application.core.entity.enums.LogType;
import com.thundax.kuzhambu.system.domain.model.valueobject.LogId;
import java.util.Date;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Log {
    private LogId id;

    private String userId;

    private LogType type;
    private Date logDate;
    private String title;
    private String remoteAddr;
    private String userAgent;
    private String method;
    private String requestUri;
    private String requestParams;
    private String remarks;
    private Date createDate;

    public void setRequestParamMap(Map<String, String[]> paramMap) {
        if (paramMap != null) {
            StringBuilder params = new StringBuilder();
            for (Map.Entry<String, String[]> param : paramMap.entrySet()) {
                params.append("".equals(params.toString()) ? "" : "&")
                        .append(param.getKey())
                        .append("=");
                String paramValue = "";
                if (param.getValue() != null && param.getValue().length > 0) {
                    paramValue = param.getValue()[0];
                }
                String safeParamValue = StringUtils.endsWithIgnoreCase(param.getKey(), "password") ? "" : paramValue;
                params.append(safeParamValue.length() > 100 ? safeParamValue.substring(0, 100) : safeParamValue);
            }
            String requestParams = params.toString();
            this.setRequestParams(requestParams.length() > 300 ? requestParams.substring(0, 300) : requestParams);
        }
    }

    public void setType(String type) {
        this.type = StringUtils.isBlank(type) ? null : LogType.from(type);
    }

    public void setType(LogType type) {
        this.type = type;
    }
}
