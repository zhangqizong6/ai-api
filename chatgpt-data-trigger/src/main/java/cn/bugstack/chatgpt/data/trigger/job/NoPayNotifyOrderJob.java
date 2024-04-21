package cn.bugstack.chatgpt.data.trigger.job;

import cn.bugstack.chatgpt.data.domain.order.service.IOrderService;
import com.google.common.eventbus.EventBus;
import com.wechat.pay.java.service.payments.model.Transaction;
import com.wechat.pay.java.service.payments.nativepay.NativePayService;
import com.wechat.pay.java.service.payments.nativepay.model.QueryOrderByOutTradeNoRequest;
import io.micrometer.core.annotation.Timed;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * @ClassName: NoPayNotifyOrderJob
 * @author: zqz
 * <p>
 * 检测未接收到或未正确处理的支付回调通知
 * @date: 2024/4/21 20:25
 */
@Slf4j
@Component()
public class NoPayNotifyOrderJob {

    @Resource
    private EventBus eventBus;
    @Resource
    private IOrderService orderService;
    @Autowired(required = false)
    private NativePayService payService;

    //商户号
    @Value("${wxpay.config.mchid}")
    private String mchid;

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");

    // Java中使用Micrometer来实现监控数据的输出
    @Timed(value = "no_pay_notify_order_job", description = "定时任务，订单支付状态更新")
    @Scheduled(cron = "0/3 * * * * ?")
    public void exec() {
        try {
            if (null == payService) {
                log.info("定时任务，订单支付状态更新。应用未配置支付渠道，任务不执行。");
                return;
            }

            List<String> orderIds = orderService.queryNoPayNotifyOrder();
            if (orderIds.isEmpty()) {
                log.info("定时任务，订单支付状态更新，暂无未更新订单 orderId is null");
                return;
            }
            for (String orderId : orderIds) {
                QueryOrderByOutTradeNoRequest request = new QueryOrderByOutTradeNoRequest();
                request.setMchid(mchid);
                request.setOutTradeNo(orderId);
                Transaction transaction = payService.queryOrderByOutTradeNo(request);
                if (!Transaction.TradeStateEnum.SUCCESS.equals(transaction.getTradeState())) {
                    log.info("定时任务，订单支付状态更新，当前订单未支付 orderId is {}", orderId);
                    continue;
                }
                // 支付单号
                String transactionId = transaction.getTransactionId();
                Integer total = transaction.getAmount().getTotal();
                BigDecimal totalAmount = new BigDecimal(total).divide(new BigDecimal(100), 2, RoundingMode.HALF_UP);
                String successTime = transaction.getSuccessTime();
                // 更新订单
                boolean isSuccess = orderService.changeOrderPaySuccess(orderId, transactionId, totalAmount, dateFormat.parse(successTime));
                if (isSuccess) {
                    // 发布消息
                    eventBus.post(orderId);
                }
            }
        } catch (Exception e) {
            log.error("定时任务，订单支付状态更新失败", e);
        }
    }
}
