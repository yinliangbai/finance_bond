package com.baiyinliang.finance.vo;

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
@ApiModel(value = "BondInfo对象", description = "")
public class BondInfoListVO {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("可转债id或编码")
    private Integer bondId;

    @ApiModelProperty("名称")
    private String bondNm;

    @ApiModelProperty("现价")
    private BigDecimal price;

    @ApiModelProperty("强赎价和现价的差值，越大越适合买")
    private BigDecimal earningsPrice;

    @ApiModelProperty("正股代码")
    private Integer stockId;

    @ApiModelProperty("正股名称")
    private String stockNm;

    @ApiModelProperty("转债评级")
    private String ratingCd;

    @ApiModelProperty("转股价值")
    private BigDecimal convertValue;

    @ApiModelProperty("转股溢价率")
    private BigDecimal premiumRt;

    @ApiModelProperty("上市日期")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date listDt;

    @ApiModelProperty("到期日期")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date maturityDt;

    @ApiModelProperty("剩余规模（亿元）")
    private BigDecimal currIssAmt;

    @ApiModelProperty("到期税前收益率")
    private BigDecimal ytmRt;

    @ApiModelProperty("到期赎回价")
    private BigDecimal redeemPrice;

    @ApiModelProperty("创建日期")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    @ApiModelProperty("更新日期")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;


}
