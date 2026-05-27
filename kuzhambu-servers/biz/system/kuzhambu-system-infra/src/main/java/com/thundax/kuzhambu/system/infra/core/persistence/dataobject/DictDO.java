package com.thundax.kuzhambu.system.infra.core.persistence.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("system_dict")
public class DictDO {
    @TableId(type = IdType.INPUT)
    private Long id;

    private String type;
    private String label;
    private String value;
    private Integer priority;
    private String remarks;
}
