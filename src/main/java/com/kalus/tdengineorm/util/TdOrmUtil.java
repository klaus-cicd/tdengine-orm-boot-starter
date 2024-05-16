package com.kalus.tdengineorm.util;

import com.kalus.tdengineorm.enums.TdLogLevelEnum;
import com.klaus.fd.utils.BeanUtil;
import org.springframework.core.env.Environment;

/**
 * @author Klaus
 */
public class TdOrmUtil {


    /**
     * 获取TdOrm日志等级, 可能为空
     *
     * @return {@link TdLogLevelEnum }
     */
    public static TdLogLevelEnum getLogLevel() {
        Environment environment = BeanUtil.getBean(Environment.class);
        String tdLogLevel = environment.getProperty("td-orm.log-level");
        return TdLogLevelEnum.match(tdLogLevel);
    }

}
