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
@TableName("system_menu")
public class MenuDO {
    @TableId(type = IdType.INPUT)
    private Long id;

    private Long parentId;
    private Integer lft;
    private Integer rgt;
    private String name;
    private String perms;
    private Integer ranks;
    private String visibility;
    private String displayParams;
    private String url;
    private String target;
    private String remarks;

    public Integer treeSpan() {
        return this.rgt - this.lft + 1;
    }
}
