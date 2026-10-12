package com.beefarm.trace.entity;
import lombok.Data;
/** Aggregated device counts; contains no individual visitor data. */
@Data
public class ScanDeviceStat {
    private String name;
    private Long value;
}
