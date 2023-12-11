package com.baiyinliang.finance.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

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
@TableName("bond_info")
@ApiModel(value = "BondInfo对象", description = "")
public class BondInfo {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("可转债id或编码")
    @TableId("bond_id")
    private Integer bondId;

    @ApiModelProperty("名称")
    @TableField("bond_nm")
    private String bondNm;

    @ApiModelProperty("现价")
    @TableField("price")
    private BigDecimal price;

    @ApiModelProperty("强赎价和现价的差值，越大越适合买")
    @TableField("earnings_price")
    private BigDecimal earningsPrice;

    @ApiModelProperty("正股代码")
    @TableField("stock_id")
    private Integer stockId;

    @ApiModelProperty("正股名称")
    @TableField("stock_nm")
    private String stockNm;

    @ApiModelProperty("转债评级")
    @TableField("rating_cd")
    private String ratingCd;

    @ApiModelProperty("转股价值")
    @TableField("convert_value")
    private BigDecimal convertValue;

    @ApiModelProperty("转股溢价率")
    @TableField("premium_rt")
    private BigDecimal premiumRt;

    @ApiModelProperty("上市日期")
    @TableField("list_dt")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date listDt;

    @ApiModelProperty("到期日期")
    @TableField("maturity_dt")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date maturityDt;

    @ApiModelProperty("剩余规模（亿元）")
    @TableField("curr_iss_amt")
    private BigDecimal currIssAmt;

    @ApiModelProperty("到期税前收益率")
    @TableField("ytm_rt")
    private BigDecimal ytmRt;

    @ApiModelProperty("到期赎回价")
    @TableField("redeem_price")
    private BigDecimal redeemPrice;

    @ApiModelProperty("状态：1上市，2退市")
    @TableField("flag")
    private Integer flag;

    @ApiModelProperty("创建日期")
    @TableField("create_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    @ApiModelProperty("更新日期")
    @TableField("update_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;


}
