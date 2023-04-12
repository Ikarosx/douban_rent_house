package cn.ikarosx.job;

import cn.ikarosx.domain.Community;
import cn.ikarosx.domain.GroupTopic;
import cn.ikarosx.domain.SubwaySite;
import cn.ikarosx.mapper.CommunityMapper;
import cn.ikarosx.mapper.GroupTopicMapper;
import cn.ikarosx.mapper.SubwaySiteMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 豆瓣租房定时任务
 *
 * @author 许培宇
 * @since 2023/4/10 22:47
 */
@Component
@RequiredArgsConstructor
public class DoubanRentHouseJob {
    private final GroupTopicMapper groupTopicMapper;
    private final CommunityMapper communityMapper;
    private final SubwaySiteMapper subwaySiteMapper;
    private List<Community> communitySet;
    private List<SubwaySite> subwaySiteSet;


    @PostConstruct
    public void init() {
        communitySet = communityMapper.selectList(null);
        subwaySiteSet = subwaySiteMapper.selectList(null);
    }

    /**
     * 识别分类
     */
    @Scheduled(cron = "0 0/1 * * * ?")
    public void identify() {
        // 1. 获取所有未识别的房源
        List<GroupTopic> groupTopics = groupTopicMapper.selectList(Wrappers.lambdaQuery(GroupTopic.class).isNull(GroupTopic::getPrice).eq(GroupTopic::getTopicType, 0));
        for (GroupTopic groupTopic : groupTopics) {
            // 2. 识别价格
            groupTopic.setPrice(doIdentifyPrice(groupTopic));
            // 3. 识别房型
            groupTopic.setUnitType(doIdentifyUnitType(groupTopic));
            // 4. 关键词识别
            groupTopic.setKeys(doIdentifyKey(groupTopic));
            // 4. 合租
            groupTopic.setShare(doIdentifyIsShare(groupTopic));
            groupTopicMapper.updateById(groupTopic);
        }
    }

    /**
     * 识别是否合租
     *
     * @param groupTopic
     * @return
     */
    private boolean doIdentifyIsShare(GroupTopic groupTopic) {
        List<String> shareStrings = Arrays.asList("合租", "主卧", "次卧");
        for (String shareString : shareStrings) {
            if (groupTopic.titleAndContentContains(shareString)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 识别关键词
     *
     * @param groupTopic
     * @return
     */
    private String doIdentifyKey(GroupTopic groupTopic) {
        // 1. 识别小区
        Set<String> keys = new HashSet<>();
        for (Community community : communitySet) {
            if (groupTopic.isCommunity(community.getProjectName())) {
                // 小区
                keys.add(community.getProjectName());
                // 区域
                keys.add(community.getDistName());
                groupTopic.setCommunity(true);
            }
        }
        // 2. 识别地铁站
        for (SubwaySite subwaySite : subwaySiteSet) {
            if (groupTopic.isSubwaySite(subwaySite.getSiteName())) {
                // 地铁站
                keys.add(subwaySite.getSiteName());
                // 地铁线
                keys.add(subwaySite.getLineName() + "号线");
            }
        }
        if (keys.size() > 10) {
            // 超过10个关键词，设置为中介
            groupTopic.setIntermediary(1);
        }
        // 计算content里面img数量，超过10为中介
        Pattern pattern = Pattern.compile("<img");
        Matcher matcher = pattern.matcher(groupTopic.getContent());
        int count = 0;
        while (matcher.find()) {
            count++;
        }
        if (count > 10) {
            groupTopic.setIntermediary(1);
        }
        return "|" + String.join("|", keys) + "|";
    }

    /**
     * 识别房型
     *
     * @param groupTopic
     * @return
     */
    private String doIdentifyUnitType(GroupTopic groupTopic) {
        List<String> unitTypes = Arrays.asList("一室一厅", "单间", "一房一厅", "两房一厅", "三房一厅");
        String result = null;
        for (String unitType : unitTypes) {
            if (groupTopic.isUnitType(unitType)) {
                if (result != null) {
                    // 出现了2次，异常，不处理
                    return "";
                }
                result = unitType;
            }
        }
        return result;
    }


    /**
     * 识别价格
     *
     * @param groupTopic
     * @return
     */
    private BigDecimal doIdentifyPrice(GroupTopic groupTopic) {
        List<String> patterns = Arrays.asList("(\\d{3,5})元/月", "(\\d{3,5})/月", "租金(\\d{3,5})(?!\\d)", "租(\\d{3,5})(?!\\d)", "(\\d{3,5})每月", "(?<![\\d\\-])(\\d{4})(?![\\d\\-])");
        for (String pattern : patterns) {
            Pattern compile = Pattern.compile(pattern);
            Matcher matcher = compile.matcher(groupTopic.getTitle());
            if (matcher.find()) {
                return new BigDecimal(matcher.group(1));
            }
            matcher = compile.matcher(groupTopic.getContent());
            if (matcher.find()) {
                return new BigDecimal(matcher.group(1));
            }
        }
        return BigDecimal.ZERO;
    }

    /**
     * 识别是转租还是求租
     */
    @Scheduled(cron = "0 0/1 * * * ?")
    public void identifySellType() {
        // 识别是转租还是求租
        List<GroupTopic> groupTopics = groupTopicMapper.selectList(Wrappers.lambdaQuery(GroupTopic.class).isNull(GroupTopic::getTopicType));
        if (groupTopics == null) {
            return;
        }
        for (GroupTopic groupTopic : groupTopics) {
            Pattern compile = Pattern.compile("预算(\\d+)");
            Matcher matcher = compile.matcher(groupTopic.getContent());
            Matcher matcher2 = compile.matcher(groupTopic.getTitle());

            if (groupTopic.isBuy() || matcher.find() || matcher2.find()) {
                groupTopic.setTopicType(1);
            } else {
                groupTopic.setTopicType(0);
            }
            GroupTopic updateGroupTopic = new GroupTopic();
            updateGroupTopic.setTopicType(groupTopic.getTopicType());
            updateGroupTopic.setId(groupTopic.getId());
            groupTopicMapper.updateById(updateGroupTopic);
        }
    }
}
