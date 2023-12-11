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
 * 可转债评级表
 * </p>
 *
 * @author root
 * @since 2023-11-26
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("bond_rating_cd")
@ApiModel(value = "BondRatingCd对象", description = "可转债评级表")
public class BondRatingCd implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId("id")
    private Long id;

    @ApiModelProperty("可转债i编码")
    @TableField("bond_code")
    private String bondCode;

    @ApiModelProperty("转债评级")
    @TableField("rating_cd")
    private String ratingCd;

    @ApiModelProperty("创建日期")
    @TableField("create_time")
    private Date createTime;


}
