package com.thundax.kuzhambu.system.infra.auth.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.thundax.kuzhambu.system.infra.auth.persistence.dataobject.PrincipalCredentialDO;
import java.util.Date;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface PrincipalCredentialMapper extends BaseMapper<PrincipalCredentialDO> {

    @Update(
            """
            UPDATE system_auth_principal_credential
            SET failed_limit = #{failedLimit},
                failed_count = failed_count + 1,
                status = CASE
                    WHEN failed_count + 1 >= #{failedLimit} THEN 'LOCKED'
                    ELSE status
                END,
                locked_until = CASE
                    WHEN failed_count + 1 >= #{failedLimit} THEN #{lockedUntil}
                    ELSE locked_until
                END,
                last_verified_at = NULL
            WHERE id = #{id}
              AND status = 'ACTIVE'
            """)
    int recordFailure(
            @Param("id") Long id, @Param("failedLimit") Integer failedLimit, @Param("lockedUntil") Date lockedUntil);
}
