package cn.ikarosx.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 小组帖子内容
 *
 * @TableName group_topic
 */
@TableName(value = "group_topic")
@Data
public class GroupTopic implements Serializable {
    /**
     *
     */
    @TableId
    private Integer id;

    /**
     * 小组名称
     */
    private String groupName;

    /**
     * 帖子标题
     */
    private String title;

    /**
     * 作者
     */
    private String author;

    /**
     * 链接
     */
    private String url;

    /**
     * 回复数
     */
    private Integer reply;

    /**
     * 发帖时间
     */
    private Date createTopicTime;

    /**
     * 最后回复时间
     */
    private Date lastReplyTime;

    /**
     * 内容
     */
    private String content;

    /**
     * 是否中介，为空未判断，0不是1是
     */
    private Integer intermediary;

    /**
     * 价格
     */
    private BigDecimal price;

    /**
     * 1疑似小区 0不是小区
     */
    private Boolean community;

    /**
     * 关键词，地铁或者小区
     */
    @TableField("`keys`")
    private String keys;

    /**
     * 户型
     */
    private String unitType;

    /**
     * 合租
     */
    @TableField("`share`")
    private Boolean share;

    /**
     *
     */
    private Date createTime;

    /**
     *
     */
    private Date updateTime;

    /**
     * 0卖1买
     */
    private Integer topicType;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    /**
     * 是否是求租
     *
     * @return
     */
    public boolean isBuy() {
        return getTitle().contains("求租") || getContent().contains("求租");
    }

    /**
     * 是否是某一个户型
     *
     * @param unitType 户型
     * @return
     */
    public boolean isUnitType(String unitType) {
        return getTitle().contains(unitType) || getContent().contains(unitType);
    }

    /**
     * 是某一个小区
     *
     * @param projectName
     * @return
     */
    public boolean isCommunity(String projectName) {
        return getTitle().contains(projectName) || getContent().contains(projectName);
    }

    /**
     * 是某一个地铁站
     *
     * @param siteName
     * @return
     */
    public boolean isSubwaySite(String siteName) {
        return getTitle().contains(siteName) || getContent().contains(siteName);
    }

    public boolean titleAndContentContains(String key) {
        return title.contains(key) || content.contains(key);
    }
}