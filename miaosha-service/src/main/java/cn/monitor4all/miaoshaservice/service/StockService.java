package cn.monitor4all.miaoshaservice.service;

import cn.monitor4all.miaoshadao.dao.Stock;

public interface StockService {

    /**
     * 获取剩余库存
     * @param id
     * @return
     */
    int getStockCount(int id);

    /**
     * 根据库存 ID 查询库存信息
     * @param id
     * @return
     */
    Stock getStockById(int id);

    /**
     * 根据库存 ID 查询库存信息 ForUpdate
     * @param id
     * @return
     */
    Stock getStockByIdForUpdate(int id);

    /**
     * 更新库存信息
     * @param stock
     * return
     */
    int updateStockById(Stock stock);

    /**
     * 更新库存信息（乐观锁）
     * @param stock
     * @return
     */
    public int updateStockByOptimistic(Stock stock);

}
