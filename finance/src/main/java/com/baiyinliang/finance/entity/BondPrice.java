package com.baiyinliang.finance.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.omg.CORBA.IDLType;

import java.math.BigDecimal;
import java.util.Date;

/**
 * <p>
 *
 * </p>
 *
 * @author root
 * @since 2023-03-06
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("bond_price")
@ApiModel(value = "BondPrice对象", description = "")
public class BondPrice {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @TableField("bond_id")
    private Integer bondId;

    @TableField("bond_nm")
    private String bondNm;

    @ApiModelProperty("收盘价即当前价格")
    @TableField("price")
    private BigDecimal price;

    @ApiModelProperty("当天最低价")
    @TableField("min_price")
    private BigDecimal minPrice;

    @ApiModelProperty("当天最高价")
    @TableField("max_price")
    private BigDecimal maxPrice;

    @ApiModelProperty("到期赎回价")
    @TableField("redeem_price")
    private BigDecimal redeemPrice;

    @ApiModelProperty("成交额（万元）")
    @TableField("volume")
    private BigDecimal volume;

    @ApiModelProperty("换手率")
    @TableField("turnover_rt")
    private BigDecimal turnoverRt;

    @ApiModelProperty("日期")
    @TableField("date")
    private Date date;

    @ApiModelProperty("周几")
    @TableField("week")
    private Integer week;

    @ApiModelProperty("创建时间")
    @TableField("create_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;


}
