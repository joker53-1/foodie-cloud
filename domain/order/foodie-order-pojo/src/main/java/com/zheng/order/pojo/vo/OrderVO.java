package com.zheng.order.pojo.vo;


import com.zheng.pojo.ShopcartBO;

import java.util.List;

public class OrderVO {

    private String orderId;
    private MerchantOrdersVO merchantOrdersVO;

    private List<ShopcartBO> toBeRemoveShopcartList;

    public List<ShopcartBO> getToBeRemoveShopcartList() {
        return toBeRemoveShopcartList;
    }

    public void setToBeRemoveShopcartList(List<ShopcartBO> toBeRemoveShopcartList) {
        this.toBeRemoveShopcartList = toBeRemoveShopcartList;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public MerchantOrdersVO getMerchantOrdersVO() {
        return merchantOrdersVO;
    }

    public void setMerchantOrdersVO(MerchantOrdersVO merchantOrdersVO) {
        this.merchantOrdersVO = merchantOrdersVO;
    }
}
