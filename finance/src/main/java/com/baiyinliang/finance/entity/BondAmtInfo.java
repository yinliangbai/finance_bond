package com.baiyinliang.finance.entity;

import com.baomidou.mybatisplus.annotation.IdType;
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
 * 可转债剩余规模表
 * </p>
 *
 * @author root
 * @since 2023-11-26
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("bond_amt_info")
@ApiModel(value = "BondAmtInfo对象", description = "可转债剩余规模表")
public class BondAmtInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("可转债i编码")
    @TableField("bond_code")
    private String bondCode;

    @ApiModelProperty("剩余规模（亿元）")
    @TableField("curr_iss_amt")
    private Integer currIssAmt;

    @ApiModelProperty("创建日期")
    @TableField("create_time")
    private Date createTime;


}
