package cn.ikarosx.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import cn.ikarosx.domain.GroupTopic;
import cn.ikarosx.service.GroupTopicService;
import cn.ikarosx.mapper.GroupTopicMapper;
import org.springframework.stereotype.Service;

/**
* @author x5322
* @description 针对表【group_topic(小组帖子内容)】的数据库操作Service实现
* @createDate 2023-04-10 23:07:59
*/
@Service
public class GroupTopicServiceImpl extends ServiceImpl<GroupTopicMapper, GroupTopic>
    implements GroupTopicService{

}




