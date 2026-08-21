package com.thundax.kuzhambu.knowledge.infra.graph.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.thundax.kuzhambu.knowledge.infra.graph.persistence.dataobject.GraphExtractionTaskDeleteReceiptDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface GraphExtractionTaskDeleteReceiptMapper extends BaseMapper<GraphExtractionTaskDeleteReceiptDO> {
    @Select(
            "select * from knowledge_graph_extraction_task_delete_receipt where operator_id = #{operatorId} and idempotency_key = #{idempotencyKey} limit 1")
    GraphExtractionTaskDeleteReceiptDO selectByOperatorIdAndIdempotencyKey(
            @Param("operatorId") Long operatorId, @Param("idempotencyKey") String idempotencyKey);

    @Select(
            "select * from knowledge_graph_extraction_task_delete_receipt where operator_id = #{operatorId} and idempotency_key = #{idempotencyKey} limit 1 for update")
    GraphExtractionTaskDeleteReceiptDO selectByOperatorIdAndIdempotencyKeyForUpdate(
            @Param("operatorId") Long operatorId, @Param("idempotencyKey") String idempotencyKey);
}
