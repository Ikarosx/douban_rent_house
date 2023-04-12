package cn.ikarosx;

import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.config.OutputFile;
import com.baomidou.mybatisplus.generator.engine.FreemarkerTemplateEngine;

import java.util.Collections;

/**
 * @author 许培宇
 * @date 2022/12/14 15:09
 */
public class CreateCode {
    public static void main(String[] args) {
        //FastAutoGenerator.create("jdbc:sqlserver://10.168.1.203:1433;DatabaseName=TEMP", "csg", "ABCabc123!")
        //String jdbcUrl = "jdbc:mysql://10.168.1.200:3306/schoolmain?allowMultiQueries=true&useUnicode=true&characterEncoding=UTF-8&useSSL=false";
        //String username = "admin";
        //String password = "admin@2017!@#";
        String jdbcUrl = "jdbc:p6spy:mysql://127.0.0.1:3306/demo?allowMultiQueries=true&useUnicode=true&characterEncoding=UTF-8&useSSL=false";
        String username = "root";
        String password = "newLife2016";
        FastAutoGenerator.create(jdbcUrl, username, password)
                .globalConfig(builder -> {
                    builder.author("许培宇") // 设置作者
                            .enableSwagger() // 开启 swagger 模式
                            //.fileOverride() // 覆盖已生成文件
                            .outputDir("src\\main\\java"); // 指定输出目录
                })
                .packageConfig(builder -> {
                    builder.parent("cn.ikarosx") // 设置父包名
                            .moduleName("generate") // 设置父包模块名
                            .pathInfo(Collections.singletonMap(OutputFile.mapperXml, "src\\main\\java")); // 设置mapperXml生成路径
                })
                .strategyConfig(builder -> {
                    builder.addInclude("demo") // 设置需要生成的表名
                            .addTablePrefix("t_", "c_")
                            .entityBuilder()
                            .enableTableFieldAnnotation(); // 设置过滤表前缀
                })
                .templateEngine(new FreemarkerTemplateEngine()) // 使用Freemarker引擎模板，默认的是Velocity引擎模板
                .execute();
    }

}
