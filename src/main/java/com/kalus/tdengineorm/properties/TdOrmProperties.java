package com.kalus.tdengineorm.properties;

import com.kalus.tdengineorm.enums.TdLogLevelEnum;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author Klaus
 */
@Data
@ConfigurationProperties(prefix = "td-orm")
public class TdOrmProperties {
    private TdLogLevelEnum logLevel;
}
