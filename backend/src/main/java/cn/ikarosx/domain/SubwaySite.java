package cn.ikarosx.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Data;

/**
 * 站点信息
 * @TableName subway_site
 */
@TableName(value ="subway_site")
@Data
public class SubwaySite implements Serializable {
    /**
     * 
     */
    @TableId
    private Integer id;

    /**
     * 站点图片
     */
    private String sitePic;

    /**
     * 站点说明
     */
    private String siteIntroduced;

    /**
     * 
     */
    private String siteCode;

    /**
     * 线路
     */
    private String lineName;

    /**
     * 
     */
    private String operCode;

    /**
     * 
     */
    private String lineCode;

    /**
     * 站点名称
     */
    private String siteName;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}