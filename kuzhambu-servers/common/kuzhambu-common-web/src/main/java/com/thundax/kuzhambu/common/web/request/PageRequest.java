package com.thundax.kuzhambu.common.web.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class PageRequest implements Serializable {
    @Min(value = 1, message = "页码不能小于1")
    private Integer pageNo;

    @Min(value = 1, message = "单页记录数不能小于1")
    @Max(value = 500, message = "单页记录数不能超过500")
    private Integer pageSize;
}
