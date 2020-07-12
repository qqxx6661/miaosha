package cn.monitor4all.miaoshaservice.service;

public interface OrderService {

    /**
     * 创建错误订单
     * @param sid
     *  库存ID
     * @return
     *  订单ID
     */
    public int createWrongOrder(int sid);


    /**
     * 创建正确订单：下单乐观锁
     * @param sid
     * @return
     * @throws Exception
     */
    public int createOptimisticOrder(int sid);

    /**
     * 创建正确订单：下单悲观锁 for update
     * @param sid
     * @return
     * @throws Exception
     */
    public int createPessimisticOrder(int sid);

    /**
     * 创建正确订单：验证库存 + 用户 + 时间 合法性 + 下单乐观锁
     * @param sid
     * @param userId
     * @param verifyHash
     * @return
     * @throws Exception
     */
    public int createVerifiedOrder(Integer sid, Integer userId, String verifyHash) throws Exception;

    /**
     * 创建正确订单：验证库存 + 下单乐观锁 + 更新订单信息到缓存
     * @param sid
     * @param userId
     * @throws Exception
     */
    public void createOrderByMq(Integer sid, Integer userId) throws Exception;

    /**
     * 检查缓存中用户是否已经有订单
     * @param sid
     * @param userId
     * @return
     * @throws Exception
     */
    public Boolean checkUserOrderInfoInCache(Integer sid, Integer userId) throws Exception;



}
