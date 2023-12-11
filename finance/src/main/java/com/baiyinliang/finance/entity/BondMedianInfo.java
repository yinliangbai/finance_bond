package com.baiyinliang.finance.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * <p>
 * 可转债中位数表
 * </p>
 *
 * @author root
 * @since 2023-11-26
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("bond_median_info")
@ApiModel(value = "BondMedianInfo对象", description = "可转债中位数表")
public class BondMedianInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId("id")
    private Long id;

    @ApiModelProperty("日期")
    @TableField("date")
    private Date date;

    @ApiModelProperty("转债中位数")
    @TableField("median")
    private Integer median;

    @ApiModelProperty("创建日期")
    @TableField("create_time")
    private Date createTime;


}
