package com.baiyinliang.finance.common.mybatisplus;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.generator.config.OutputFile;
import com.baomidou.mybatisplus.generator.config.StrategyConfig;
import com.baomidou.mybatisplus.generator.config.rules.DateType;
import com.baomidou.mybatisplus.generator.engine.FreemarkerTemplateEngine;

import java.util.Collections;

public class FastAutoGenerator {

    public static void main(String[] args) {
        System.out.println("---------------------------------");
        com.baomidou.mybatisplus.generator.FastAutoGenerator.create("jdbc:mysql://127.0.0.1:3306/bond_sys", "root", "root")
                .globalConfig(builder -> {
                    builder.author("root") // 设置作者
                            .enableSwagger() // 开启 swagger 模式
                            .fileOverride() // 覆盖已生成文件
                            .dateType(DateType.ONLY_DATE)
                            .commentDate("yyyy-MM-dd")
                            // .outputDir("D:\\Projects\\account-book\\account-user\\src\\main\\java"); // 指定输出目录
                            // .outputDir("D:\\Projects\\account-book\\account-books\\src\\main\\java"); // 指定输出目录
                            .outputDir("D:\\Projects\\finance\\src\\main\\java"); // 指定输出目录
                })
                .packageConfig(builder -> {
                    builder.parent("com.baiyinliang.finance") // 设置父包名
                            // .moduleName("user") // 设置父包模块名
                            .pathInfo(Collections.singletonMap(OutputFile.mapperXml, "D:\\Projects\\finance\\src\\main\\resources\\mapper")); // 设置mapperXml生成路径
                })
                .strategyConfig(builder -> {
                    builder.addInclude("bond_amt_info","bond_base_info","bond_median_info","bond_price_info","bond_rate_info","bond_rating_cd") // 设置需要生成的表名
                    // builder.addInclude("t_account_bill","t_account_base_info","t_account_book","t_book_budget","t_book_category","t_book_consumer","t_book_merchant","t_book_theme") // 设置需要生成的表名
                    // builder.addInclude("t_account_user") // 设置需要生成的表名
                            .addTablePrefix("t_", "c_"); // 设置过滤表前缀
                    supportEntityStrategyConfig(builder);
                    supportMapperStrategyConfig(builder);
                    supportServiceStrategyConfig(builder);
                    supportControllerStrategyConfig(builder);
                })
                .templateEngine(new FreemarkerTemplateEngine())
                /*.templateConfig(builder -> {
                    builder.entity("/templates/entity.java");
                })*/
                .execute();
    }

    private static void supportMapperStrategyConfig(StrategyConfig.Builder builder) {
        builder.mapperBuilder()
                .superClass(BaseMapper.class)
                .enableMapperAnnotation()
                .enableBaseResultMap()
                .enableBaseColumnList()
                // .cache(MyMapperCache.class)
                .formatMapperFileName("%sDao")
                .formatXmlFileName("%sXml")
                .build();
    }

    private static void supportServiceStrategyConfig(StrategyConfig.Builder builder) {
        builder.serviceBuilder()
                // .superServiceClass(BaseService.class)
                // .superServiceImplClass(BaseServiceImpl.class)
                .formatServiceFileName("%sService")
                .formatServiceImplFileName("%sServiceImpl")
                .build();
    }

    private static void supportControllerStrategyConfig(StrategyConfig.Builder builder) {
        builder.controllerBuilder()
                // .superClass(BaseController.class)
                .enableHyphenStyle() //开启驼峰转连字符
                .enableRestStyle()
                // .formatFileName("%sAction")
                .build();
    }

    private static void supportEntityStrategyConfig(StrategyConfig.Builder builder) {
        builder.entityBuilder()
//                .superClass("com.baiyinliang.finance.entity.BaseEntity")
                // .disableSerialVersionUID()
                .enableChainModel()
                .enableLombok()
                .enableRemoveIsPrefix()
                .enableTableFieldAnnotation()
                // .enableActiveRecord()
                .versionColumnName("version")
                .versionPropertyName("version")
                .logicDeleteColumnName("delete")
                .logicDeletePropertyName("delete")
                // .naming(NamingStrategy.no_change)
                // .columnNaming(NamingStrategy.no_change)
//                .addSuperEntityColumns("id", "created_by", "created_time", "updated_by", "updated_time")
        // .addIgnoreColumns("age")
        // .addTableFills(new Column("create_time", FieldFill.INSERT))
        // .addTableFills(new Property("updateTime", FieldFill.UPDATE))
        // .idType(IdType.AUTO)
        // .formatFileName("%sEntity")
        ;
    }


}
