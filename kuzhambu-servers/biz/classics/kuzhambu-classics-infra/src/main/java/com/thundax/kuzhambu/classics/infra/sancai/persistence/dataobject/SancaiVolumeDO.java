package com.thundax.kuzhambu.classics.infra.sancai.persistence.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("classics_sancai_volume")
public class SancaiVolumeDO {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long categoryId;
    private String title;
    private String volumeType;
    private Integer priority;
}
