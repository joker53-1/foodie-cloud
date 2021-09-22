package com.zheng.item.mapper;

import com.zheng.my.mapper.MyMapper;
import com.zheng.item.pojo.ItemsComments;
import com.zheng.item.pojo.vo.MyCommentVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface ItemsCommentsMapperCustom extends MyMapper<ItemsComments> {

    public void saveComments(Map<String, Object> map);
    public List<MyCommentVO> queryMyComments(@Param("paramsMap") Map<String, Object> map);
}