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
    private static final long serialVersionUID = 1L;

    @PrimaryTs
    private Timestamp ts;
}
