package cn.monitor4all.miaoshaservice.service;

public interface UserService {

    public String getVerifyHash(Integer sid, Integer userId) throws Exception;

    public int addUserCount(Integer userId) throws Exception;

    public boolean getUserIsBanned(Integer userId);

}
