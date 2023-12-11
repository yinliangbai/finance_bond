package com.baiyinliang.finance.vo;

import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
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
public class BondInfoVO {

    private static final long serialVersionUID = 1L;


    @ApiModelProperty("可转债编码")
    private String bondCode;

    @ApiModelProperty("可转债id或编码")
    private Integer bondId;

    @ApiModelProperty("名称")
    private String bondNm;

    @ApiModelProperty("现价")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal price;

    @ApiModelProperty("强赎价和现价的差值，越大越适合买")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal earningsPrice;

    @ApiModelProperty("正股代码")
    private Integer stockId;

    @ApiModelProperty("正股名称")
    private String stockNm;

    @ApiModelProperty("转债评级")
    private String ratingCd;

    @ApiModelProperty("转股价值")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal convertValue;

    @ApiModelProperty("转股溢价率")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal premiumRt;

    @ApiModelProperty("上市日期")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date listDt;

    @ApiModelProperty("到期日期")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date maturityDt;

    @ApiModelProperty("剩余规模（亿元）")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal currIssAmt;

    @ApiModelProperty("到期税前收益率")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal ytmRt;

    @ApiModelProperty("到期税后收益率")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal incomeRt;

    @ApiModelProperty("到期赎回价")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal redeemPrice;

    @ApiModelProperty("前一天最低价:最高价")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BondPriceRange priceRange1;

    @ApiModelProperty("前二天最低价:最高价")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BondPriceRange priceRange2;

    @ApiModelProperty("前三天最低价:最高价")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BondPriceRange priceRange3;

    @ApiModelProperty("前四天最低价:最高价")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BondPriceRange priceRange4;

    @ApiModelProperty("前五天最低价:最高价")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BondPriceRange priceRange5;

    @ApiModelProperty("付息信息")
    private BondPayInterestInfo bondPayInterestInfo;

    /**
     * 本期付息日
     */
    @ApiModelProperty("本期付息日")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    @JSONField(format = "yyyy-MM-dd")
    private Date currentPeriodPayInterestDate;
    /**
     * 本期利率
     */
    @ApiModelProperty("本期利率")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal currentPeriodRate;
    /**
     * 是否已支付
     */
    @ApiModelProperty("是否已支付")
    private Integer paidFlag;

    @Data
    public static class BondPriceRange {
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        private BigDecimal minPrice;
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        private BigDecimal maxPrice;
        private boolean minFlag;
        private boolean maxFlag;
    }

    @Data
    public static class BondPayInterestInfo {
        /**
         * 本期付息日
         */
        @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
        @JSONField(format = "yyyy-MM-dd")
        private Date currentPeriodPayInterestDate;
        /**
         * 本期利率
         */
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        private BigDecimal currentPeriodRate;
        /**
         * 是否已支付
         */
        private Integer paidFlag;
    }
}
