package parkingos.com.bolink.service.impl;

import com.alibaba.fastjson.JSONObject;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import parkingos.com.bolink.dao.spring.CommonDao;
import parkingos.com.bolink.models.OrgCityMerchants;
import parkingos.com.bolink.models.UserInfoTb;
import parkingos.com.bolink.service.CityUinService;
import parkingos.com.bolink.service.SupperSearchService;

import java.util.HashMap;
import java.util.Map;

@Service
public class CityUinServiceImpl implements CityUinService {

    Logger logger = Logger.getLogger(CityUinServiceImpl.class);

    @Autowired
    private CommonDao commonDao;
    @Autowired
    private SupperSearchService<OrgCityMerchants> supperSearchService;


    @Override
    public JSONObject createCity(String name, String union_id, String ukey) {
        JSONObject result = new JSONObject();

        if("".equals(name)||"".equals(ukey)||"".equals(union_id)){
            result.put("state",0);
            result.put("msg","添加失败");
            return result;
        }

        OrgCityMerchants orgCityMerchants = new OrgCityMerchants();
        Long id = commonDao.selectSequence(OrgCityMerchants.class);
        orgCityMerchants.setId(id);
        orgCityMerchants.setName(name);
        orgCityMerchants.setUnionId(union_id);
        orgCityMerchants.setUkey(ukey);
        orgCityMerchants.setCtime(System.currentTimeMillis()/1000);
        int res = commonDao.insert(orgCityMerchants);
        if(res==1){

            UserInfoTb userInfotb = new UserInfoTb();
            userInfotb.setCityid(id);
            userInfotb.setStrid("admin");
            userInfotb.setNickname(name+"管理员");
            userInfotb.setRoleId(29L);
            userInfotb.setRegTime(System.currentTimeMillis()/1000);
            userInfotb.setPassword("123456");
            int insertUser = commonDao.insert(userInfotb);
            System.out.println("插入user:"+insertUser);

            result.put("state",1);
            result.put("msg","添加成功");
        }
        return result;
    }

    @Override
    public JSONObject editSetting(Long cityid, Integer state) {
        JSONObject result = new JSONObject();
        OrgCityMerchants orgCityMerchants = new OrgCityMerchants();
        orgCityMerchants.setId(cityid);
        orgCityMerchants.setSelfRefillSetting(state);
        int count = commonDao.updateByPrimaryKey(orgCityMerchants);
        if(count ==1){
            result.put("state",1);
            return result;
        }
        result.put("state",0);
        result.put("errmsg","修改失败");
        return result;
    }

    @Override
    public JSONObject querySetting(Long cityid) {
        JSONObject result = new JSONObject();
        OrgCityMerchants orgCityMerchants = new OrgCityMerchants();
        orgCityMerchants.setId(cityid);
        orgCityMerchants.setState(0);
        orgCityMerchants =(OrgCityMerchants)commonDao.selectObjectByConditions(orgCityMerchants);
        Map<String,Object> map = new HashMap<>();
        if(orgCityMerchants!=null){
            map.put("self_setting",orgCityMerchants.getSelfRefillSetting());
            result.put("self_setting",map);
            return result;
        }
        return null;
    }


}
