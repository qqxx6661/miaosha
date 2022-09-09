package cn.monitor4all.miaoshaweb.controller;

import cn.monitor4all.miaoshaservice.service.OrderService;
import cn.monitor4all.miaoshaservice.service.StockService;
import cn.monitor4all.miaoshaservice.service.UserService;
import com.alibaba.fastjson.JSONObject;
import com.google.common.util.concurrent.RateLimiter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.*;

@Controller
public class OrderController {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderController.class);

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserService userService;

    @Autowired
    private StockService stockService;

    @Autowired
    private AmqpTemplate rabbitTemplate;

    // Guava令牌桶：每秒放行10个请求
    RateLimiter rateLimiter = RateLimiter.create(10);

    // 延时时间：预估读数据库数据业务逻辑的耗时，用来做缓存再删除
    private static final int DELAY_MILLSECONDS = 1000;

    // 延时双删线程池
    private static ExecutorService cachedThreadPool = new ThreadPoolExecutor(0, Integer.MAX_VALUE, 60L, TimeUnit.SECONDS,new SynchronousQueue<Runnable>());

    /**
     * 下单接口：导致超卖的错误示范
     * @param sid
     * @return
     */
    @RequestMapping("/createWrongOrder/{sid}")
    @ResponseBody
    public String createWrongOrder(@PathVariable int sid) {
        int id = 0;
        try {
            id = orderService.createWrongOrder(sid);
            LOGGER.info("创建订单id: [{}]", id);
        } catch (Exception e) {
            LOGGER.error("Exception", e);
        }
        return String.valueOf(id);
    }

    /**
     * 下单接口：乐观锁更新库存 + 令牌桶限流
     * @param sid
     * @return
     */
    @RequestMapping("/createOptimisticOrder/{sid}")
    @ResponseBody
    public String createOptimisticOrder(@PathVariable int sid) {
        // 1. 阻塞式获取令牌
        LOGGER.info("等待时间" + rateLimiter.acquire());
        // 2. 非阻塞式获取令牌
//        if (!rateLimiter.tryAcquire(1000, TimeUnit.MILLISECONDS)) {
//            LOGGER.warn("你被限流了，真不幸，直接返回失败");
//            return "你被限流了，真不幸，直接返回失败";
//        }
        int id;
        try {
            id = orderService.createOptimisticOrder(sid);
            LOGGER.info("购买成功，剩余库存为: [{}]", id);
        } catch (Exception e) {
            LOGGER.error("购买失败：[{}]", e.getMessage());
            return "购买失败，库存不足";
        }
        return String.format("购买成功，剩余库存为：%d", id);
    }

    /**
     * 下单接口：悲观锁更新库存 事务for update更新库存
     * @param sid
     * @return
     */
    @RequestMapping("/createPessimisticOrder/{sid}")
    @ResponseBody
    public String createPessimisticOrder(@PathVariable int sid) {
        int id;
        try {
            id = orderService.createPessimisticOrder(sid);
            LOGGER.info("购买成功，剩余库存为: [{}]", id);
        } catch (Exception e) {
            LOGGER.error("购买失败：[{}]", e.getMessage());
            return "购买失败，库存不足";
        }
        return String.format("购买成功，剩余库存为：%d", id);
    }

    /**
     * 验证接口：下单前用户获取验证值
     * @return
     */
    @RequestMapping(value = "/getVerifyHash", method = {RequestMethod.GET})
    @ResponseBody
    public String getVerifyHash(@RequestParam(value = "sid") Integer sid,
                                @RequestParam(value = "userId") Integer userId) {
        String hash;
        try {
            hash = userService.getVerifyHash(sid, userId);
        } catch (Exception e) {
            LOGGER.error("获取验证hash失败，原因：[{}]", e.getMessage());
            return "获取验证hash失败";
        }
        return String.format("请求抢购验证hash值为：%s", hash);
    }

    /**
     * 下单接口：要求用户验证的抢购接口
     * @param sid
     * @return
     */
    @RequestMapping(value = "/createOrderWithVerifiedUrl", method = {RequestMethod.GET})
    @ResponseBody
    public String createOrderWithVerifiedUrl(@RequestParam(value = "sid") Integer sid,
                                             @RequestParam(value = "userId") Integer userId,
                                             @RequestParam(value = "verifyHash") String verifyHash) {
        int stockLeft;
        try {
            stockLeft = orderService.createVerifiedOrder(sid, userId, verifyHash);
            LOGGER.info("购买成功，剩余库存为: [{}]", stockLeft);
        } catch (Exception e) {
            LOGGER.error("购买失败：[{}]", e.getMessage());
            return e.getMessage();
        }
        return String.format("购买成功，剩余库存为：%d", stockLeft);
    }

    /**
     * 下单接口：要求验证的抢购接口 + 单用户限制访问频率
     * @param sid
     * @return
     */
    @RequestMapping(value = "/createOrderWithVerifiedUrlAndLimit", method = {RequestMethod.GET})
    @ResponseBody
    public String createOrderWithVerifiedUrlAndLimit(@RequestParam(value = "sid") Integer sid,
                                                     @RequestParam(value = "userId") Integer userId,
                                                     @RequestParam(value = "verifyHash") String verifyHash) {
        int stockLeft;
        try {
            int count = userService.addUserCount(userId);
            LOGGER.info("用户截至该次的访问次数为: [{}]", count);
            boolean isBanned = userService.getUserIsBanned(userId);
            if (isBanned) {
                return "购买失败，超过频率限制";
            }
            stockLeft = orderService.createVerifiedOrder(sid, userId, verifyHash);
            LOGGER.info("购买成功，剩余库存为: [{}]", stockLeft);
        } catch (Exception e) {
            LOGGER.error("购买失败：[{}]", e.getMessage());
            return e.getMessage();
        }
        return String.format("购买成功，剩余库存为：%d", stockLeft);
    }

    /**
     * 下单接口：先删除缓存，再更新数据库
     * @param sid
     * @return
     */
    @RequestMapping("/createOrderWithCacheV1/{sid}")
    @ResponseBody
    public String createOrderWithCacheV1(@PathVariable int sid) {
        int count = 0;
        try {
            // 删除库存缓存
            stockService.delStockCountCache(sid);
            // 完成扣库存下单事务
            count = orderService.createPessimisticOrder(sid);
        } catch (Exception e) {
            LOGGER.error("购买失败：[{}]", e.getMessage());
            return "购买失败，库存不足";
        }
        LOGGER.info("购买成功，剩余库存为: [{}]", count);
        return String.format("购买成功，剩余库存为：%d", count);
    }

    /**
     * 下单接口：先更新数据库，再删缓存
     * @param sid
     * @return
     */
    @RequestMapping("/createOrderWithCacheV2/{sid}")
    @ResponseBody
    public String createOrderWithCacheV2(@PathVariable int sid) {
        int count = 0;
        try {
            // 完成扣库存下单事务
            count = orderService.createPessimisticOrder(sid);
            // 删除库存缓存
            stockService.delStockCountCache(sid);
        } catch (Exception e) {
            LOGGER.error("购买失败：[{}]", e.getMessage());
            return "购买失败，库存不足";
        }
        LOGGER.info("购买成功，剩余库存为: [{}]", count);
        return String.format("购买成功，剩余库存为：%d", count);
    }

    /**
     * 下单接口：先删除缓存，再更新数据库，缓存延时双删
     * @param sid
     * @return
     */
    @RequestMapping("/createOrderWithCacheV3/{sid}")
    @ResponseBody
    public String createOrderWithCacheV3(@PathVariable int sid) {
        int count;
        try {
            // 删除库存缓存
            stockService.delStockCountCache(sid);
            // 完成扣库存下单事务
            count = orderService.createPessimisticOrder(sid);
            LOGGER.info("完成下单事务");
            // 延时指定时间后再次删除缓存
            cachedThreadPool.execute(new delCacheByThread(sid));
        } catch (Exception e) {
            LOGGER.error("购买失败：[{}]", e.getMessage());
            return "购买失败，库存不足";
        }
        LOGGER.info("购买成功，剩余库存为: [{}]", count);
        return String.format("购买成功，剩余库存为：%d", count);
    }

    /**
     * 下单接口：先更新数据库，再删缓存，删除缓存失败重试，通知消息队列
     * @param sid
     * @return
     */
    @RequestMapping("/createOrderWithCacheV4/{sid}")
    @ResponseBody
    public String createOrderWithCacheV4(@PathVariable int sid) {
        int count;
        try {
            // 完成扣库存下单事务
            count = orderService.createPessimisticOrder(sid);
            LOGGER.info("完成下单事务");
            // 删除库存缓存
            stockService.delStockCountCache(sid);
            // 延时指定时间后再次删除缓存
            // cachedThreadPool.execute(new delCacheByThread(sid));
            // 假设上述再次删除缓存没成功，通知消息队列进行删除缓存
            sendToDelCache(String.valueOf(sid));

        } catch (Exception e) {
            LOGGER.error("购买失败：[{}]", e.getMessage());
            return "购买失败，库存不足";
        }
        LOGGER.info("购买成功，剩余库存为: [{}]", count);
        return "购买成功";
    }

    /**
     * 下单接口：异步处理订单
     * @param sid
     * @return
     */
    @RequestMapping(value = "/createOrderWithMq", method = {RequestMethod.GET})
    @ResponseBody
    public String createOrderWithMq(@RequestParam(value = "sid") Integer sid,
                                  @RequestParam(value = "userId") Integer userId) {
        try {
            // 检查缓存中商品是否还有库存
            Integer count = stockService.getStockCount(sid);
            if (count == 0) {
                return "秒杀请求失败，库存不足.....";
            }

            // 有库存，则将用户id和商品id封装为消息体传给消息队列处理
            // 注意这里的有库存和已经下单都是缓存中的结论，存在不可靠性，在消息队列中会查表再次验证
            LOGGER.info("有库存：[{}]", count);
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("sid", sid);
            jsonObject.put("userId", userId);
            sendToOrderQueue(jsonObject.toJSONString());
            return "秒杀请求提交成功";
        } catch (Exception e) {
            LOGGER.error("下单接口：异步处理订单异常：", e);
            return "秒杀请求失败，服务器正忙.....";
        }
    }

    /**
     * 下单接口：异步处理订单
     * @param sid
     * @return
     */
    @RequestMapping(value = "/createUserOrderWithMq", method = {RequestMethod.GET})
    @ResponseBody
    public String createUserOrderWithMq(@RequestParam(value = "sid") Integer sid,
                                  @RequestParam(value = "userId") Integer userId) {
        try {
            // 检查缓存中该用户是否已经下单过
            Boolean hasOrder = orderService.checkUserOrderInfoInCache(sid, userId);
            if (hasOrder != null && hasOrder) {
                LOGGER.info("该用户已经抢购过");
                return "你已经抢购过了，不要太贪心.....";
            }
            // 没有下单过，检查缓存中商品是否还有库存
            LOGGER.info("没有抢购过，检查缓存中商品是否还有库存");
            Integer count = stockService.getStockCount(sid);
            if (count == 0) {
                return "秒杀请求失败，库存不足.....";
            }

            // 有库存，则将用户id和商品id封装为消息体传给消息队列处理
            // 注意这里的有库存和已经下单都是缓存中的结论，存在不可靠性，在消息队列中会查表再次验证
            LOGGER.info("有库存：[{}]", count);
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("sid", sid);
            jsonObject.put("userId", userId);
            sendToOrderQueue(jsonObject.toJSONString());
            return "秒杀请求提交成功";
        } catch (Exception e) {
            LOGGER.error("下单接口：异步处理订单异常：", e);
            return "秒杀请求失败，服务器正忙.....";
        }
    }

    /**
     * 检查缓存中用户是否已经生成订单
     * @param sid
     * @return
     */
    @RequestMapping(value = "/checkOrderByUserIdInCache", method = {RequestMethod.GET})
    @ResponseBody
    public String checkOrderByUserIdInCache(@RequestParam(value = "sid") Integer sid,
                                  @RequestParam(value = "userId") Integer userId) {
        // 检查缓存中该用户是否已经下单过
        try {
            Boolean hasOrder = orderService.checkUserOrderInfoInCache(sid, userId);
            if (hasOrder != null && hasOrder) {
                return "恭喜您，已经抢购成功！";
            }
        } catch (Exception e) {
            LOGGER.error("检查订单异常：", e);
        }
        return "很抱歉，你的订单尚未生成，继续排队。";
    }


    /**
     * 缓存再删除线程
     */
    private class delCacheByThread implements Runnable {
        private int sid;
        public delCacheByThread(int sid) {
            this.sid = sid;
        }
        public void run() {
            try {
                LOGGER.info("异步执行缓存再删除，商品id：[{}]， 首先休眠：[{}] 毫秒", sid, DELAY_MILLSECONDS);
                Thread.sleep(DELAY_MILLSECONDS);
                stockService.delStockCountCache(sid);
                LOGGER.info("再次删除商品id：[{}] 缓存", sid);
            } catch (Exception e) {
                LOGGER.error("delCacheByThread执行出错", e);
            }
        }
    }


    /**
     * 向消息队列delCache发送消息
     * @param message
     */
    private void sendToDelCache(String message) {
        LOGGER.info("这就去通知消息队列开始重试删除缓存：[{}]", message);
        this.rabbitTemplate.convertAndSend("delCache", message);
    }

    /**
     * 向消息队列orderQueue发送消息
     * @param message
     */
    private void sendToOrderQueue(String message) {
        LOGGER.info("这就去通知消息队列开始下单：[{}]", message);
        this.rabbitTemplate.convertAndSend("orderQueue", message);
    }

}
