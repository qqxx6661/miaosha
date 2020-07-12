package cn.monitor4all.miaoshaservice.service;

public interface UserService {

    /**
     * 获取用户验证Hash
     * @param sid
     * @param userId
     * @return
     * @throws Exception
     */
    public String getVerifyHash(Integer sid, Integer userId) throws Exception;

    /**
     * 添加用户访问次数
     * @param userId
     * @return
     * @throws Exception
     */
    public int addUserCount(Integer userId) throws Exception;

    /**
     * 检查用户是否被禁
     * @param userId
     * @return
     */
    public boolean getUserIsBanned(Integer userId);

}
