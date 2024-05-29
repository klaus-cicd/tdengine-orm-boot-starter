package com.kalus.tdengineorm.config;

import com.kalus.tdengineorm.mapper.TDengineMapper;
import com.kalus.tdengineorm.util.JdbcTemplatePlus;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

/**
 * @author Klaus
 */
public class TDengineOrmAutoConfiguration {

    @Bean
    public JdbcTemplatePlus jdbcTemplatePlus(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        return new JdbcTemplatePlus(namedParameterJdbcTemplate);
    }


    @Bean
    public TDengineMapper tdengineMapper(JdbcTemplatePlus jdbcTemplatePlus, NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        return new TDengineMapper(jdbcTemplatePlus, namedParameterJdbcTemplate);
    }

}
