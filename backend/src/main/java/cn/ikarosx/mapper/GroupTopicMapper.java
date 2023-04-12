package cn.ikarosx.mapper;

import cn.ikarosx.domain.GroupTopic;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author x5322
 * @description 针对表【group_topic(小组帖子内容)】的数据库操作Mapper
 * @createDate 2023-04-10 23:07:59
 * @Entity cn.ikarosx.domain.GroupTopic
 */
@Mapper
public interface GroupTopicMapper extends BaseMapper<GroupTopic> {

}




