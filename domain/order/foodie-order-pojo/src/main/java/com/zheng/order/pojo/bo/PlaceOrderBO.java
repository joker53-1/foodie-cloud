package com.zheng.order.pojo.bo;

import com.zheng.pojo.ShopcartBO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlaceOrderBO {
    private SubmitOrderBO order;
    private List<ShopcartBO> items;
}
