package cn.bugstack.chatgpt.data.infrastructure.dao;


import cn.bugstack.chatgpt.data.infrastructure.po.UserAccountPO;
import org.apache.ibatis.annotations.Mapper;

/**
 * @ClassName: IUserAccountDao
 * @author: zqz
 * @date: 2024/4/19 18:21
 */
@Mapper
public interface IUserAccountDao {

    int subAccountQuota(String openid);

    UserAccountPO queryUserAccount(String openid);
}
