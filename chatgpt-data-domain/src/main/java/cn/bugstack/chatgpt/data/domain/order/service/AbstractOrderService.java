package cn.bugstack.chatgpt.data.domain.order.service;

import cn.bugstack.chatgpt.data.domain.order.model.entity.*;
import cn.bugstack.chatgpt.data.domain.order.model.valobj.PayStatusVO;
import cn.bugstack.chatgpt.data.domain.order.repository.IOrderRepository;
import cn.bugstack.chatgpt.data.types.common.Constants;
import cn.bugstack.chatgpt.data.types.exception.ChatGPTException;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;
import java.math.BigDecimal;

/**
 * @ClassName: AbstractOrderService
 * @author: zqz
 * @date: 2024/4/21 19:28
 * <p>
 * 抽象订单服务
 */
@Slf4j
public abstract class AbstractOrderService implements IOrderService {

    @Resource
    protected IOrderRepository orderRepository;


    @Override
    public PayOrderEntity createOrder(ShopCartEntity shopCartEntity) {
        try {
            //1.获取请求基础参数
            String openid = shopCartEntity.getOpenid();
            Integer productId = shopCartEntity.getProductId();

            //2.查询有效的未支付订单，如果存在直接返回微信支付
            UnpaidOrderEntity unpaidOrderEntity = orderRepository.queryUnpaidOrder(shopCartEntity);
            if (null != unpaidOrderEntity && PayStatusVO.WAIT.equals(unpaidOrderEntity.getPayStatus()) && null != unpaidOrderEntity.getPayUrl()) {
                log.info("创建订单-存在，已生成微信支付，返回 openid: {} orderId: {} payUrl: {}", openid, unpaidOrderEntity.getOrderId(), unpaidOrderEntity.getPayUrl());
                return PayOrderEntity.builder()
                        .openid(openid)
                        .orderId(unpaidOrderEntity.getOrderId())
                        .payUrl(unpaidOrderEntity.getPayUrl())
                        .payStatus(unpaidOrderEntity.getPayStatus())
                        .build();
            } else if (null != unpaidOrderEntity && null == unpaidOrderEntity.getPayUrl()) {
                log.info("创建订单-存在，未生成微信支付，返回 openid: {} orderId: {}", openid, unpaidOrderEntity.getOrderId());
                PayOrderEntity payOrderEntity = this.doPrepayOrder(openid, unpaidOrderEntity.getOrderId(), unpaidOrderEntity.getProductName(), unpaidOrderEntity.getTotalAmount());
                log.info("创建订单-完成，生成支付单。openid: {} orderId: {} payUrl: {}", openid, payOrderEntity.getOrderId(), payOrderEntity.getPayUrl());
                return payOrderEntity;
            }

            //3.商品查询
            ProductEntity productEntity = orderRepository.queryProduct(productId);
            if (!productEntity.isAvailable()) {
                throw new ChatGPTException(Constants.ResponseCode.ORDER_PRODUCT_ERR.getCode(), Constants.ResponseCode.ORDER_PRODUCT_ERR.getInfo());
            }

            // 4. 保存订单
            OrderEntity orderEntity = this.doSaveOrder(openid, productEntity);

            //5.创建支付
            PayOrderEntity payOrderEntity = this.doPrepayOrder(openid, orderEntity.getOrderId(), productEntity.getProductName(), orderEntity.getTotalAmount());
            log.info("创建订单-完成，生成支付单。openid: {} orderId: {} payUrl: {}", openid, orderEntity.getOrderId(), payOrderEntity.getPayUrl());
            return payOrderEntity;

        } catch (Exception e) {
            log.error("创建订单，已生成微信支付，返回 openid: {} productId: {}", shopCartEntity.getOpenid(), shopCartEntity.getProductId());
            throw new ChatGPTException(Constants.ResponseCode.UN_ERROR.getCode(), Constants.ResponseCode.UN_ERROR.getInfo());
        }
    }

    protected abstract OrderEntity doSaveOrder(String openid, ProductEntity productEntity);

    protected abstract PayOrderEntity doPrepayOrder(String openid, String orderId, String productName, BigDecimal amountTotal);

}
