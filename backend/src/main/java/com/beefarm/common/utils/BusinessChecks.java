package com.beefarm.common.utils;
import com.beefarm.common.exception.BusinessException;
import java.math.BigDecimal;
public final class BusinessChecks {
    private BusinessChecks() {}
    public static void require(boolean valid, String message) {
        if (!valid) throw new BusinessException(message);
    }
    public static BigDecimal money(BigDecimal value) {
        require(value != null && value.signum() >= 0 && value.scale() <= 2
                && value.compareTo(new BigDecimal("9999999999.99")) <= 0, "金额须为非负数，最多两位小数");
        return value;
    }
    public static void quantity(BigDecimal value) {
        require(value != null && value.signum() > 0 && value.scale() <= 2
                && value.compareTo(new BigDecimal("9999999999.99")) <= 0, "数量须大于0，最多两位小数");
    }
}
