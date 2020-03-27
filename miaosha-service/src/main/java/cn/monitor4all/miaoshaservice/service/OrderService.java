package cn.monitor4all.miaoshaservice.service;

public interface OrderService {

    /**
     * 创建订单
     * @param sid
     *  库存ID
     * @return
     *  订单ID
     */
    public int createWrongOrder(int sid);


    /**
     * 创建订单 乐观锁
     * @param sid
     * @return
     * @throws Exception
     */
    public int createOptimisticOrder(int sid);

    /**
     * 创建订单 悲观锁 for update
     * @param sid
     * @return
     * @throws Exception
     */
    public int createPessimisticOrder(int sid);

    public int createVerifiedOrder(Integer sid, Integer userId, String verifyHash) throws Exception;
}
