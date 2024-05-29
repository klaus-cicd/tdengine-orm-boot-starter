package com.kalus.tdengineorm.entity;

import com.kalus.tdengineorm.annotation.PrimaryTs;
import lombok.Data;

import java.sql.Timestamp;

/**
 * @author Klaus
 */
@Data
public class BaseTdEntity {

    @PrimaryTs
    private Timestamp ts;
}
