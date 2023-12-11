package com.baiyinliang.finance.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * <p>
 * 
 * </p>
 *
 * @author root
 * @since 2023-11-09
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("bond_duration_price")
@ApiModel(value = "BondDurationPrice对象", description = "")
public class BondDurationPrice implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @TableField("bond_id")
    private Integer bondId;

    @ApiModelProperty("最低价")
    @TableField("min_price")
    private BigDecimal minPrice;

    @ApiModelProperty("最高价")
    @TableField("max_price")
    private BigDecimal maxPrice;

    @ApiModelProperty("多少天内最低或最高")
    @TableField("duration")
    private Integer duration;

    @ApiModelProperty("创建时间")
    @TableField("create_time")
    private Date createTime;

    @ApiModelProperty("逻辑删除 0正常 1删除")
    @TableField("del_flag")
    private Integer delFlag;


}
