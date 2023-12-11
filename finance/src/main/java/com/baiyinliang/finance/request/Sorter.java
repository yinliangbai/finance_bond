package com.baiyinliang.finance.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

/**
 * 1. 基础排序对象，包含排序字段和排序方式
 */
@Data
public class Sorter {

    @ApiModelProperty(value = "排序字段", example = "userName")
    private String sort;

    @ApiModelProperty(value = "排序方式", example = "asc/desc")
    private String order;

    /**
     * 根据查询条件拼接得到order by语句
     *
     * @param sorter 分页查询条件
     * @return String
     */
    public static String getStatement(Sorter sorter) {
        String sort;
        String[] sortArray = {};
        String[] orderArray = {};
        String order = sorter.getOrder();
        String sortColumn = sorter.getSort();
        StringBuilder statement = new StringBuilder();

        // 多字段排序
        if (StringUtils.isNotEmpty(sortColumn)) {
            // 驼峰命名转为下划线
            sort = com.baiyinliang.finance.tools.StringUtils.toUnderScoreCase(sortColumn);

            if (sort.contains(",")) {
                sortArray = sort.split(",");
            }
        } else {
            return "";
        }
        if (StringUtils.isNotEmpty(order)) {
            if (order.contains(",")) {
                orderArray = order.split(",");
            }
        } else {
            return "";
        }

        if (sortArray.length > 0 && orderArray.length > 0) {
            int length = sortArray.length;

            for (int i = 0; i < length; i++) {
                statement.append(sortArray[i]);
                statement.append(" ");
                statement.append(orderArray[i]);

                if (i < length - 1) {
                    statement.append(", ");
                }
            }
        } else {
            // " #{sort} #{order}“
            statement.append(sort);
            statement.append(" ");
            statement.append(order);
        }
        return statement.toString();
    }

    /**
     * 根据查询条件拼接得到order by语句
     *
     * @param sorter 分页查询条件
     * @return String
     */
    public static String getOrderByStatement(Sorter sorter) {
        String statement = getStatement(sorter);

        if (StringUtils.isNotEmpty(statement)) {
            return " order by " + statement;
        } else {
            return statement;
        }
    }
}
