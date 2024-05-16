package com.kalus.tdengineorm.entity;

import com.kalus.tdengineorm.annotation.PrimaryTs;
import lombok.Data;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * @author Klaus
 */
@Data
public class TdBaseEntity implements Serializable {
    @PrimaryTs
    private Timestamp ts;
}
