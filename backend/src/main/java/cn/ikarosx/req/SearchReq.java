package cn.ikarosx.req;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * 搜索
 *
 * @author 许培宇
 * @since 2023/4/11 23:43
 */
@Data
public class SearchReq {
    private String lineName;
    private String subwayStation;
    private String community;
    // 排除中介
    private Boolean agentFilter;
    private int page = 1;
    private int size = 10;
    private BigDecimal priceLow;
    private BigDecimal priceHigh;
    // 租房类型，全部，整租，合租
    private String rentType;

    public Integer getRentType() {
        if (Objects.equals("全部", rentType)) {
            return null;
        }
        if (Objects.equals("合租", rentType)) {
            return 1;
        }
        return 0;
    }
}
