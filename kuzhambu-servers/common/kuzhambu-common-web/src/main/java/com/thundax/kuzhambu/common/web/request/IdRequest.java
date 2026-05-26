package com.thundax.kuzhambu.common.web.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class IdRequest implements Serializable {
    @NotEmpty(message = "ID不能为空")
    @Size(max = 64, message = "ID长度不能超过64")
    private String id;
}
