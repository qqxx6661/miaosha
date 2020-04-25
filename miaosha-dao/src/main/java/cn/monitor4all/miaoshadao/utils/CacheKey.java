package cn.monitor4all.miaoshadao.utils;

public enum CacheKey {

    HASH_KEY("miaosha_v1_hash"),
    LIMIT_KEY("miaosha_v1_limit"),
    
    STOCK_COUNT("miaosha_v1_stock_count");

    private String key;

    private CacheKey(String key) {
        this.key = key;
    }
    public String getKey() {
        return key;
    }
}
