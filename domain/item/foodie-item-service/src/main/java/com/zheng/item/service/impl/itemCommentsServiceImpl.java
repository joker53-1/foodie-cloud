package com.zheng.item.service.impl;

import com.github.pagehelper.PageHelper;
import com.zheng.item.mapper.ItemsCommentsMapperCustom;
import com.zheng.item.pojo.vo.MyCommentVO;
import com.zheng.item.service.ItemCommentsService;
import com.zheng.pojo.PagedGridResult;
import com.zheng.service.BaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class itemCommentsServiceImpl extends BaseService implements ItemCommentsService {

    @Autowired
    private ItemsCommentsMapperCustom itemsCommentsMapperCustom;

    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public PagedGridResult queryMyComments(@RequestParam("userId") String userId, @RequestParam(value = "page",required = false) Integer page,
                                           @RequestParam(value = "pageSize",required = false) Integer pageSize) {
        Map<String,Object> map = new HashMap<>();
        map.put("userId",userId);

        PageHelper.startPage(page,pageSize);
        List<MyCommentVO> list = itemsCommentsMapperCustom.queryMyComments(map);

        return setterPagedGrid(list,page);
    }

    @Override
    public void saveComments(Map<String, Object> map) {
        itemsCommentsMapperCustom.saveComments(map);
    }
}
