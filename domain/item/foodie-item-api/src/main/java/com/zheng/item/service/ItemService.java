package com.zheng.item.service;

import com.zheng.item.pojo.Items;
import com.zheng.item.pojo.ItemsImg;
import com.zheng.item.pojo.ItemsParam;
import com.zheng.item.pojo.ItemsSpec;
import com.zheng.pojo.PagedGridResult;
import com.zheng.item.pojo.vo.CommentLevelCountsVO;
import com.zheng.item.pojo.vo.ShopcartVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient("foodie-item-service")
@RequestMapping("item-api")
public interface ItemService {

    /**
     * 根据商品id查询详情
     * @param itemId
     * @return
     */
    @GetMapping("item")
    public Items queryItemById(@RequestParam("itemId") String itemId);

    /**
     * 根据商品id查询商品图片列表
     * @param itemId
     * @return
     */
    @GetMapping("itemImages")
    public List<ItemsImg> queryItemImgList(@RequestParam("itemId")String itemId);

    /**
     * 根据商品id查询商品规格
     * @param itemId
     * @return
     */
    @GetMapping("itemSpecs")
    public List<ItemsSpec> queryItemSpecList(@RequestParam("itemId")String itemId);

    /**
     * 根据商品id查询商品参数
     * @param itemId
     * @return
     */
    @GetMapping("itemParam")
    public ItemsParam queryItemParam(@RequestParam("itemId")String itemId);

    /**
     * 根据商品id查询商品的评价等级数量
     * @param itemId
     * @return
     */
    @GetMapping("countComment")
    public CommentLevelCountsVO queryCommentCounts(@RequestParam("itemId")String itemId);

    /**
     * 这是根据商品id查询商品的评价（分页）
     * @param itemId
     * @param level
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping("pagedComments")
    public PagedGridResult queryPagedComments(@RequestParam("itemId") String itemId, @RequestParam(value = "level",required = false) Integer level,
                                              @RequestParam(value = "page",required = false)Integer page, @RequestParam(value = "pageSize",required = false)Integer pageSize);

//    /**
//     * 这是根据商品id查询商品的评价（分页）
//     * @param keywords
//     * @param sort
//     * @param page
//     * @param pageSize
//     * @return
//     */
//    public PagedGridResult searchItems(String keywords, String sort,
//                                       Integer page, Integer pageSize);
//
//    /**
//     * 这是根据分类id查询商品的评价（分页）
//     * @param catId
//     * @param sort
//     * @param page
//     * @param pageSize
//     * @return
//     */
//    public PagedGridResult searchItems(Integer catId, String sort,
//                                       Integer page, Integer pageSize);

    /**
     * 根据规格ids查询最新的购物车中商品数据（用于刷新渲染购物车中的商品数据）
     * @param specIds
     * @return
     */
    @GetMapping("getCartBySpecIds")
    public List<ShopcartVO> queryItemsBySpecIds(@RequestParam("specIds")String specIds);

    /**
     * 根据商品规格id获取商品信息
     * @param specId
     * @return
     */
    @GetMapping("singleItemSpec")
    public ItemsSpec queryItemSpecById(@RequestParam("specId") String specId);

    /**
     * 根据商品id获得商品图片主图url
     * @param itemId
     * @return
     */
    @GetMapping("primaryImage")
    public String queryItemMainImgById(@RequestParam("itemId") String itemId);

    /**
     * 减少库存
     * @param specId
     * @param buyCounts
     */
    @PostMapping("decreaseStock")
    public void decreaseItemSpecStock(@RequestParam("specId") String specId, @RequestParam("buyCounts") int buyCounts);
}
