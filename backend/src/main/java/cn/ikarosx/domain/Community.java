package cn.ikarosx.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Data;

/**
 * 
 * @TableName community
 */
@TableName(value ="community")
@Data
public class Community implements Serializable {
    /**
     * 
     */
    private String projectType;

    /**
     * 
     */
    private String devName;

    /**
     * 
     */
    private String distName;

    /**
     * 
     */
    private String id;

    /**
     * 
     */
    private String cspName;

    /**
     * 
     */
    private String staffName;

    /**
     * 
     */
    private String projectName;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}