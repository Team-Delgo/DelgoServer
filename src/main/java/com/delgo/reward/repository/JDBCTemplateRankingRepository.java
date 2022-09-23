package com.delgo.reward.repository;

import com.delgo.reward.domain.ranking.RankingCategory;
import com.delgo.reward.domain.ranking.RankingPoint;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@RequiredArgsConstructor
@Repository
public class JDBCTemplateRankingRepository{

    private final JdbcTemplate jdbcTemplate;

    public List<RankingPoint> findRankingByPoint() {
        return jdbcTemplate.query("select user_id, geo_code, weekly_point, RANK() over (partition by geo_code order by weekly_point desc) ranking from user;", rankingByPointRowMapper());
    }

    public List<RankingCategory> findRankingByCategory(String categoryCode) {
        System.out.println("select category_code, user_id, geo_code, RANK() over (partition by category_code order by user_id desc) ranking from (select category_code, user_id, geo_code, count(*) from certification where category_code = \"" + categoryCode + "\" group by user_id) by_category_code;");
        return jdbcTemplate.query("select category_code, user_id, geo_code, RANK() over (partition by category_code order by user_id desc) ranking from (select category_code, user_id, geo_code, count(*) from certification where category_code = \"" + categoryCode + "\" group by user_id) by_category_code;", rankingByCategoryRowMapper(categoryCode));
    }


    private RowMapper<RankingPoint> rankingByPointRowMapper() {
        return (rs, rowNum) -> {
            RankingPoint rankingPoint = RankingPoint.builder().userId(rs.getInt("user_id")).ranking(rs.getInt("ranking")).geoCode(rs.getString("geo_code")).build();
            return rankingPoint;
        };
    }

    private RowMapper<RankingCategory> rankingByCategoryRowMapper(String categoryCode) {
        return (rs, rowNum) -> {
            RankingCategory rankingCategory = RankingCategory.builder().userId(rs.getInt("user_id")).ranking(rs.getInt("ranking")).geoCode(rs.getString("geo_code")).categoryCode(categoryCode).build();
            return rankingCategory;
        };
    }
}
