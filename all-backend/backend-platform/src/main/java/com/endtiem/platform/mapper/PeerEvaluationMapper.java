/*
 * Copyright 2024 Endtiem. All rights reserved.
 */

package com.endtiem.platform.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.endtiem.platform.entity.PeerEvaluation;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface PeerEvaluationMapper extends BaseMapper<PeerEvaluation> {

    default List<PeerEvaluation> selectByEvaluatorId(Integer evaluatorId) {
        return selectList(
                new LambdaQueryWrapper<PeerEvaluation>()
                        .eq(PeerEvaluation::getEvaluatorId, evaluatorId));
    }

    default List<PeerEvaluation> selectByEvaluatedId(Integer evaluatedId) {
        return selectList(
                new LambdaQueryWrapper<PeerEvaluation>()
                        .eq(PeerEvaluation::getEvaluatedId, evaluatedId));
    }

    default PeerEvaluation selectByEvaluatorIdAndEvaluatedId(
            Integer evaluatorId, Integer evaluatedId) {
        return selectOne(
                new LambdaQueryWrapper<PeerEvaluation>()
                        .eq(PeerEvaluation::getEvaluatorId, evaluatorId)
                        .eq(PeerEvaluation::getEvaluatedId, evaluatedId));
    }

    default boolean existsByEvaluatorIdAndEvaluatedId(Integer evaluatorId, Integer evaluatedId) {
        return selectCount(
                        new LambdaQueryWrapper<PeerEvaluation>()
                                .eq(PeerEvaluation::getEvaluatorId, evaluatorId)
                                .eq(PeerEvaluation::getEvaluatedId, evaluatedId))
                > 0;
    }

    @Select(
            "SELECT activity_type AS activityType, COUNT(*) AS cnt FROM peer_evaluation WHERE"
                    + " evaluated_id = #{evaluatedId} GROUP BY activity_type")
    List<Map<String, Object>> countByActivityTypeForEvaluated(
            @Param("evaluatedId") Integer evaluatedId);

    @Select("SELECT AVG(rating) FROM peer_evaluation WHERE evaluated_id = #{evaluatedId}")
    Double getAverageRatingForEvaluated(@Param("evaluatedId") Integer evaluatedId);

    @Select("SELECT COUNT(*) FROM peer_evaluation WHERE evaluated_id = #{evaluatedId}")
    Integer countEvaluationsForEvaluated(@Param("evaluatedId") Integer evaluatedId);
}
