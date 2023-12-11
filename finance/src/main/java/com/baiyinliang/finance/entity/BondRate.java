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

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * <p>
 *
 * </p>
 *
 * @author root
 * @since 2023-03-14
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("bond_rate")
@ApiModel(value = "BondRate对象", description = "")
public class BondRate implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @TableField("bond_id")
    private Integer bondId;

    @ApiModelProperty("付息日")
    @TableField("payment_date")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date paymentDate;

    @ApiModelProperty("当前利率")
    @TableField("rate")
    private BigDecimal rate;

    @ApiModelProperty("创建时间")
    @TableField("create_time")
    private Date createTime;


}
