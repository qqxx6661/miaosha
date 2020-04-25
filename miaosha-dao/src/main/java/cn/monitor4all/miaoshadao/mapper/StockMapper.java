package cn.monitor4all.miaoshadao.mapper;

import cn.monitor4all.miaoshadao.dao.Stock;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface StockMapper {

    int deleteByPrimaryKey(Integer id);

    int insert(Stock record);

    int insertSelective(Stock record);

    Stock selectByPrimaryKey(Integer id);

    Stock selectByPrimaryKeyForUpdate(Integer id);

    int updateByPrimaryKeySelective(Stock record);

    int updateByPrimaryKey(Stock record);

    int updateByOptimistic(Stock record);
}