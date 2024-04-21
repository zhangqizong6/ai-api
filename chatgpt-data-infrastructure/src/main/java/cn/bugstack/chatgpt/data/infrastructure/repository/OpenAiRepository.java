package cn.bugstack.chatgpt.data.infrastructure.repository;

import cn.bugstack.chatgpt.data.domain.openai.model.entity.UserAccountQuotaEntity;
import cn.bugstack.chatgpt.data.domain.openai.model.valobj.UserAccountStatusVO;
import cn.bugstack.chatgpt.data.domain.openai.repository.IOpenAiRepository;
import cn.bugstack.chatgpt.data.infrastructure.dao.IUserAccountDao;
import cn.bugstack.chatgpt.data.infrastructure.po.UserAccountPO;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;

/**
 * @ClassName: OpenAiRepository
 * @author: zqz
 * @date: 2024/4/19 18:20
 */
@Repository
public class OpenAiRepository implements IOpenAiRepository {

    @Resource
    private IUserAccountDao userAccountDao;

    @Override
    public int subAccountQuota(String openid) {
        return userAccountDao.subAccountQuota(openid);
    }

    @Override
    public UserAccountQuotaEntity queryUserAccount(String openid) {
        UserAccountPO userAccountPO = userAccountDao.queryUserAccount(openid);
        if (null == userAccountPO) return null;
        UserAccountQuotaEntity userAccountQuotaEntity = new UserAccountQuotaEntity();
        userAccountQuotaEntity.setOpenid(userAccountPO.getOpenid());
        userAccountQuotaEntity.setTotalQuota(userAccountPO.getTotalQuota());
        userAccountQuotaEntity.setSurplusQuota(userAccountPO.getSurplusQuota());
        userAccountQuotaEntity.setUserAccountStatusVO(UserAccountStatusVO.get(userAccountPO.getStatus()));
        userAccountQuotaEntity.genModelTypes(userAccountPO.getModelTypes());
        return userAccountQuotaEntity;
    }
}
