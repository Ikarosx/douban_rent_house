package cn.ikarosx.controller;

import cn.ikarosx.domain.GroupTopic;
import cn.ikarosx.domain.SubwaySite;
import cn.ikarosx.req.SearchReq;
import cn.ikarosx.service.GroupTopicService;
import cn.ikarosx.service.SubwaySiteService;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author 许培宇
 * @since 2023/4/11 17:16
 */
@RestController
@RequestMapping("/rent")
@RequiredArgsConstructor
public class RentController {
    private final SubwaySiteService subwaySiteService;
    private final GroupTopicService groupTopicService;

    /**
     * 获取线路信息
     */
    @RequestMapping("/getLine")
    public ResponseEntity<Map<String, List<String>>> getLine() {
        List<SubwaySite> list = subwaySiteService.list();
        //Map<String, List<String>> subWaySiteMap = list.stream().collect(Collectors.groupingBy(SubwaySite::getLineName));
        Map<String, List<String>> subWaySiteMap = list.stream().collect(Collectors.groupingBy(SubwaySite::getLineName, Collectors.mapping(SubwaySite::getSiteName, Collectors.toList())));
        return ResponseEntity.ok(subWaySiteMap);
    }

    /**
     * 根据条件搜索房源
     */
    @GetMapping("/search")
    public ResponseEntity<Page<GroupTopic>> search(SearchReq searchReq) {
        Page<GroupTopic> pageParam = Page.of(searchReq.getPage(), searchReq.getSize());
        Page<GroupTopic> page = groupTopicService.page(pageParam,
                Wrappers.lambdaQuery(GroupTopic.class)
                        // 最后回复时间在1个月以内
                        // 豆瓣不登陆只能打开1个月以内的
                        .ge(GroupTopic::getLastReplyTime, LocalDate.now().minusMonths(1))
                        .like(StringUtils.hasText(searchReq.getCommunity()), GroupTopic::getKeys, searchReq.getCommunity())
                        .like(StringUtils.hasText(searchReq.getLineName()), GroupTopic::getKeys, searchReq.getLineName() + "号线")
                        .like(StringUtils.hasText(searchReq.getSubwayStation()), GroupTopic::getKeys, searchReq.getSubwayStation())
                        .ge(GroupTopic::getPrice, searchReq.getPriceLow())
                        .le(GroupTopic::getPrice, searchReq.getPriceHigh())
                        .isNull(searchReq.getAgentFilter(), GroupTopic::getIntermediary)
                        .eq(searchReq.getRentType() != null, GroupTopic::getShare, searchReq.getRentType())
                        .orderByDesc(GroupTopic::getLastReplyTime)
        );
        return ResponseEntity.ok(page);
    }
}
