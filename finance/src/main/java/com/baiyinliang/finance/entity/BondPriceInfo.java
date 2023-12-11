package com.baiyinliang.finance.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 * 可转债价格表
 * </p>
 *
 * @author root
 * @since 2023-11-26
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("bond_price_info")
@ApiModel(value = "BondPriceInfo对象", description = "可转债价格表")
public class BondPriceInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("可转债i编码")
    @TableField("bond_code")
    private String bondCode;

    @ApiModelProperty("开盘价")
    @TableField("opening_price")
    private Integer openingPrice;

    @ApiModelProperty("税后收益率")
    @TableField("income")
    private Integer income;

    @ApiModelProperty("当前价 即当天收盘价")
    @TableField("current_price")
    private Integer currentPrice;

    @ApiModelProperty("当天日期")
    @TableField("date")
    private Date date;

    @ApiModelProperty("当天最高价")
    @TableField("max_price")
    private Integer maxPrice;

    @ApiModelProperty("当天最低价")
    @TableField("min_price")
    private Integer minPrice;

    @ApiModelProperty("创建时间")
    @TableField("create_time")
    private Date createTime;


}
