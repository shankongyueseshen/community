package com.nowcoder.community.dao;

import com.nowcoder.community.entity.DiscussPost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Component;

import java.util.List;

//@Component
@Mapper
public interface DiscussPostMapper {
    //将来会有个人主页，个人主页里有我发布的帖子用到这个userid参数
    //第一页起始行行号，每一页最多有多少条数据
    List<DiscussPost> selectDiscussPosts(int userId, int offset, int limit,int orderMode);

    // @Param注解用于给参数取别名,
    // 如果只有一个参数,并且在<if>里使用,则必须加别名.
    //为了显示页码方便，查询帖子总数
    int selectDiscussPostRows(@Param("userId") int userId);

    int insertDiscussPost(DiscussPost discussPost );

    DiscussPost selectDiscussPostById(@Param("id")int id);

    int updateCommentCount(@Param("id")int id, @Param("commentCount")int commentCount);

    int updateType(@Param("id")int id,@Param("type") int type);

    int updateStatus(@Param("id")int id, @Param("status")int status);

    int updateScore(@Param("id")int id, @Param("score")double score);
}
