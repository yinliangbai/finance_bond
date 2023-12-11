package com.baiyinliang.finance.model;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class Bond {


//     /**
//         * 可转债id/代码
//         */
//        private Integer bond_id;
//        /**
//         * 名称
//         */
//        private String bond_nm;
//
//        private String bond_py;
//        /**
//         * 现价
//         */
//        private BigDecimal price;
//        /**
//         * 涨跌幅
//         */
//        private Double increase_rt;
//        /**
//         * 正股代码
//         */
//        private Integer stock_id;
//        /**
//         * 正股名称
//         */
//        private String stock_nm;
//        /**
//         * 正股简称
//         */
//        private String stock_py;
//        /**
//         * 正股价
//         */
//        private String sprice;
//        /**
//         * 正股涨跌
//         */
//        private String sincrease_rt;
//        /**
//         * 正股pb
//         */
//        private String pb;
//        /**
//         * 转股价
//         */
//        private String convert_price;
//        /**
//         * 转股价值
//         */
//        private String convert_value;
//        /**
//         * ？
//         */
//        private String convert_dt;
//        /**
//         * 转股溢价率
//         */
//        private String premium_rt;
//        /**
//         * 双低
//         */
//        private String dblow;
//        /**
//         * ？
//         */
//        private String sw_cd;
//        /**
//         * ？
//         */
//        private String market_cd;
//        /**
//         * ？
//         */
//        private String btype;
//        /**
//         * 上市日期
//         */
//        private String list_dt;
//        /**
//         * 涨跌幅
//         */
//        private String qflag2;
//        /**
//         * 涨跌幅
//         */
//        private String owned;
//        /**
//         * 涨跌幅
//         */
//        private String hold;
//        /**
//         * 涨跌幅
//         */
//        private String bond_value;
//        /**
//         * 债券评级
//         */
//        private String rating_cd;
//        /**
//         * 涨跌幅
//         */
//        private String option_value;
//        /**
//         * 涨跌幅
//         */
//        private String put_convert_price;
//        /**
//         * 涨跌幅
//         */
//        private String force_redeem_price;
//        /**
//         * 涨跌幅
//         */
//        private String convert_amt_ratio;
//        /**
//         * 涨跌幅
//         */
//        private String fund_rt;
//        /**
//         * 到期日期
//         */
//        private String maturity_dt;
//        /**
//         * 剩余年限
//         */
//        private BigDecimal year_left;
//        /**
//         * 剩余规模（亿元）
//         */
//        private String curr_iss_amt;
//        /**
//         * 成交额（万元）
//         */
//        private String volume;
//        /**
//         * 涨跌幅
//         */
//        private String svolume;
//        /**
//         * 换手率
//         */
//        private String turnover_rt;
//        /**
//         * 到期税前收益率
//         */
//        private String ytm_rt;
//        /**
//         * 涨跌幅
//         */
//        private String put_ytm_rt;
//        /**
//         * 涨跌幅
//         */
//        private String noted;
//        /**
//         * 涨跌幅
//         */
//        private String bond_nm_tip;
//        /**
//         * 涨跌幅
//         */
//        private String redeem_icon;
//        /**
//         * 涨跌幅
//         */
//        private String last_time;
//        /**
//         * 涨跌幅
//         */
//        private String qstatus;
//        /**
//         * 涨跌幅
//         */
//        private String margin_flg;
//        /**
//         * 涨跌幅
//         */
//        private String sqflag;
//        /**
//         * 涨跌幅
//         */
//        private String pb_flag;
//        /**
//         * 涨跌幅
//         */
//        private String adj_cnt;
//        /**
//         * 涨跌幅
//         */
//        private String adj_scnt;
//        /**
//         * 涨跌幅
//         */
//        private String convert_price_valid;
//        /**
//         * 涨跌幅
//         */
//        private String convert_price_tips;
//        /**
//         * 涨跌幅
//         */
//        private String convert_cd_tip;
//        /**
//         * 涨跌幅
//         */
//        private String ref_yield_info;
//        /**
//         * 涨跌幅
//         */
//        private String adjusted;
//        /**
//         * 涨跌幅
//         */
//        private String orig_iss_amt;
//        /**
//         * 涨跌幅
//         */
//        private String price_tips;
//        /**
//         * 涨跌幅
//         */
//        private String redeem_dt;
//        /**
//         * 涨跌幅
//         */
//        private String real_force_redeem_price;
//        /**
//         * 涨跌幅
//         */
//        private String option_tip;
//        /**
//         * 涨跌幅
//         */
//        private String notes;
//        /**
//         * 涨跌幅
//         */
//        private String volatility_rate;/**
//     * 可转债id/代码
//     */
//    private Integer bond_id;
//    /**
//     * 名称
//     */
//    private String bond_nm;
//    /**
//     * 现价
//     */
//    private BigDecimal price;
//    /**
//     * 涨跌幅
//     */
//    private Double increase_rt;
//    /**
//     * 转股价值
//     */
//    private BigDecimal convert_value;
//    /**
//     * 转股溢价率
//     */
//    private BigDecimal premium_rt;
//    /**
//     * 上市日期
//     */
//    private Date list_dt;
//    /**
//     * 债券评级
//     */
//    private String rating_cd;
//    /**
//     * 到期日期
//     */
//    private Date maturity_dt;
//    /**
//     * 剩余年限
//     */
//    private String year_left;
//    /**
//     * 剩余规模（亿元）
//     */
//    private BigDecimal curr_iss_amt;
//    /**
//     * 成交额（万元）
//     */
//    private String volume;
//    /**
//     * 换手率
//     */
//    private String turnover_rt;
//    /**
//     * 到期税前收益率
//     */
//    private BigDecimal ytm_rt;
//    /**
//     * 到期赎回价
//     */
//    private BigDecimal redeem_price;
//    /**
//     * 强赎价-价格
//     */
    private BigDecimal earnings_price;
//    /**
//     * 正股代码
//     */
//    private Integer stock_id;
//    /**
//     * 正股名称
//     */
//    private String stock_nm;


    /**
     * 可转债id/代码
     */
    private Integer bond_id;
    /**
     * 名称
     */
    private String bond_nm;

    private String bond_py;
    /**
     * 现价
     */
    private BigDecimal price;
    /**
     * 涨跌幅
     */
    private Double increase_rt;
    /**
     * 正股代码
     */
    private Integer stock_id;
    /**
     * 正股名称
     */
    private String stock_nm;
    /**
     * 正股简称
     */
    private String stock_py;
    /**
     * 正股价
     */
    private String sprice;
    /**
     * 正股涨跌
     */
    private String sincrease_rt;
    /**
     * 正股pb
     */
    private String pb;
    /**
     * 转股价
     */
    private String convert_price;
    /**
     * 转股价值
     */
    private BigDecimal convert_value;
    /**
     * ？
     */
    private String convert_dt;
    /**
     * 转股溢价率
     */
    private BigDecimal premium_rt;
    /**
     * 双低
     */
    private String dblow;
    /**
     * ？
     */
    private String sw_cd;
    /**
     * ？
     */
    private String market_cd;
    /**
     * ？
     */
    private String btype;
    /**
     * 上市日期
     */
    private Date list_dt;
    /**
     * 涨跌幅
     */
    private String qflag2;
    /**
     * 涨跌幅
     */
    private String owned;
    /**
     * 涨跌幅
     */
    private String hold;
    /**
     * 涨跌幅
     */
    private String bond_value;
    /**
     * 债券评级
     */
    private String rating_cd;
    /**
     * 涨跌幅
     */
    private String option_value;
    /**
     * 涨跌幅
     */
    private String put_convert_price;
    /**
     * 涨跌幅
     */
    private String force_redeem_price;
    /**
     * 涨跌幅
     */
    private String convert_amt_ratio;
    /**
     * 涨跌幅
     */
    private String fund_rt;
    /**
     * 到期日期
     */
    private Date maturity_dt;
    /**
     * 剩余年限
     */
    private BigDecimal year_left;
    /**
     * 剩余规模（亿元）
     */
    private BigDecimal curr_iss_amt;
    /**
     * 成交额（万元）
     */
    private BigDecimal volume;
    /**
     * 涨跌幅
     */
    private BigDecimal svolume;
    /**
     * 换手率
     */
    private BigDecimal turnover_rt;
    /**
     * 到期税前收益率
     */
    private BigDecimal ytm_rt;
    /**
     * 涨跌幅
     */
    private String put_ytm_rt;
    /**
     * 涨跌幅
     */
    private String noted;
    /**
     * 涨跌幅
     */
    private String bond_nm_tip;
    /**
     * 涨跌幅
     */
    private String redeem_icon;
    /**
     * 涨跌幅
     */
    private String last_time;
    /**
     * 涨跌幅
     */
    private String qstatus;
    /**
     * 涨跌幅
     */
    private String margin_flg;
    /**
     * 涨跌幅
     */
    private String sqflag;
    /**
     * 涨跌幅
     */
    private String pb_flag;
    /**
     * 涨跌幅
     */
    private String adj_cnt;
    /**
     * 涨跌幅
     */
    private String adj_scnt;
    /**
     * 涨跌幅
     */
    private String convert_price_valid;
    /**
     * 涨跌幅
     */
    private String convert_price_tips;
    /**
     * 涨跌幅
     */
    private String convert_cd_tip;
    /**
     * 涨跌幅
     */
    private String ref_yield_info;
    /**
     * 涨跌幅
     */
    private String adjusted;
    /**
     * 涨跌幅
     */
    private String orig_iss_amt;
    /**
     * 涨跌幅
     */
    private String price_tips;
    /**
     * 涨跌幅
     */
    private String redeem_dt;
    /**
     * 涨跌幅
     */
    private String real_force_redeem_price;
    /**
     * 涨跌幅
     */
    private String option_tip;
    /**
     * 涨跌幅
     */
    private String notes;
    /**
     * 涨跌幅
     */
    private String volatility_rate;
    /**
     * 到期赎回价
     */
    private BigDecimal redeem_price;
}