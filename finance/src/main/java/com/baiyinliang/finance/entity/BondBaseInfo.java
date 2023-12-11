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
 * 可转债基本信息表，除了状态，其它数据不变
 * </p>
 *
 * @author root
 * @since 2023-11-26
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("bond_base_info")
@ApiModel(value = "BondBaseInfo对象", description = "可转债基本信息表，除了状态，其它数据不变")
public class BondBaseInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("可转债i编码")
    @TableField("bond_code")
    private String bondCode;

    @ApiModelProperty("名称")
    @TableField("bond_nm")
    private String bondNm;

    @ApiModelProperty("票面价格")
    @TableField("price")
    private Integer price;

    @ApiModelProperty("上市日期（从详情页面获取）")
    @TableField("list_dt")
    private Date listDt;

    @ApiModelProperty("到期日期")
    @TableField("maturity_dt")
    private Date maturityDt;

    @ApiModelProperty("发行规模")
    @TableField("orig_iss_amt")
    private Long origIssAmt;

    @ApiModelProperty("到期赎回价（从详情页面获取）")
    @TableField("redeem_price")
    private Integer redeemPrice;

    @ApiModelProperty("创建日期")
    @TableField("create_time")
    private Date createTime;

    @ApiModelProperty("更新日期")
    @TableField("update_time")
    private Date updateTime;

    @ApiModelProperty("状态：0未上市 ，1上市，2退市")
    @TableField("flag")
    private Integer flag;


}
