package parkingos.com.bolink.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import parkingos.com.bolink.dao.spring.CommonDao;
import parkingos.com.bolink.models.*;
import parkingos.com.bolink.service.SupperSearchService;
import parkingos.com.bolink.service.UserProgramService;
import parkingos.com.bolink.utils.OrmUtil;
import parkingos.com.bolink.utils.TimeTools;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;


@Service
public class UserProgramServiceImpl implements UserProgramService {

    Logger logger = LoggerFactory.getLogger(UserProgramServiceImpl.class);

    @Autowired
    CommonDao commonDao;
    @Autowired
    SupperSearchService supperSearchService;


    @Override
    public JSONObject buyUserProgram(Integer buyMonth, Double money, String tradeNo, Long park_id, Long userId) {
        JSONObject result = new JSONObject();
        result.put("state",0);

        Long union_id =-1L;
        ComInfoTb comInfoTb = new ComInfoTb();
        comInfoTb.setId(park_id);
        comInfoTb = (ComInfoTb)commonDao.selectObjectByConditions(comInfoTb);
        if(comInfoTb==null){
            return result;
        }

        String parkName = comInfoTb.getCompanyName()+"("+comInfoTb.getBolinkId()+")";
        if(comInfoTb.getCityid()>0){
            union_id = comInfoTb.getCityid();
        }else{
            OrgGroupTb orgGroupTb= new OrgGroupTb();
            orgGroupTb.setId(comInfoTb.getGroupid());
            orgGroupTb=(OrgGroupTb)commonDao.selectObjectByConditions(orgGroupTb);
            if(orgGroupTb!=null){
                union_id = orgGroupTb.getCityid();
            }
        }

        String unionName = "";
        if(union_id>0){
            OrgCityMerchants orgCityMerchants = new OrgCityMerchants();
            orgCityMerchants.setId(union_id);
            orgCityMerchants=(OrgCityMerchants)commonDao.selectObjectByConditions(orgCityMerchants);
            if(orgCityMerchants!=null){
                unionName=orgCityMerchants.getName();
            }
        }

        UserInfoTb user = new UserInfoTb();
        user.setId(userId);
        user = (UserInfoTb)commonDao.selectObjectByConditions(user);
        String userName = "";
        if(user!=null){
            userName=user.getNickname()+"("+userId+")";
        }

        UserProgramTrade trade = new UserProgramTrade();
        trade.setTradeNo(tradeNo);
        trade.setBuyMonth(buyMonth);
        trade.setMoney(new BigDecimal(money+""));
        trade.setParkId(park_id);
        trade.setParkName(parkName);
        trade.setState(0);
        trade.setUnionId(union_id);
        trade.setUnionName(unionName);
        trade.setUserId(userId);
        trade.setUserName(userName);

        trade.setCtime(System.currentTimeMillis()/1000);
        //如果这个收费员支付过，那么结束时间取这个人的结束时间
        Long beginTime = System.currentTimeMillis()/1000;
        UserProgramTb account = new UserProgramTb();
        account.setUserId(userId);
        account =(UserProgramTb)commonDao.selectObjectByConditions(account);
        if(account!=null&&account.getEndTime()!=null){
            //如果大于当前时间 证明没过期  那么取结束时间
            if(account.getEndTime()>beginTime) {
                beginTime = account.getEndTime();
            }
        }

        trade.setBeginTime(beginTime);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(beginTime * 1000);
        calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH) + buyMonth);
        Long etime = calendar.getTimeInMillis() / 1000 - 1;

        trade.setEndTime(etime);

        int insert = commonDao.insert(trade);
        if(insert==1) {
            result.put("state", 1);
            result.put("trade_no",tradeNo);
        }
        return result;
    }

    @Override
    public JSONObject getBuyTrade(Map<String, String> reqParameterMap) {
        Long comid = Long.parseLong(reqParameterMap.get("comid"));
        UserProgramTrade trade = new UserProgramTrade();
        trade.setParkId(comid);
        trade.setState(1);
        JSONObject result = supperSearchService.supperSearch(trade, reqParameterMap);
        return result;
    }

    @Override
    public List<List<Object>> exportBuyTrade(Map<String, String> reqParameterMap) {
        reqParameterMap.remove("orderby");
        JSONObject result =getBuyTrade(reqParameterMap);
        List<UserProgramTrade> buyList = JSON.parseArray(result.get("rows").toString(), UserProgramTrade.class);
        List<List<Object>> bodyList = new ArrayList<List<Object>>();
        if(buyList!=null&&buyList.size()>0){
            String [] f = new String[]{"user_id","user_name","buy_month","pay_time","end_time","money","trade_no"};
            for(UserProgramTrade trade : buyList){
                List<Object> values = new ArrayList<Object>();
                OrmUtil<UserProgramTrade> otm = new OrmUtil<UserProgramTrade>();
                Map map = otm.pojoToMap(trade);
                //判断各种字段 组装导出数据
                for(String field : f){
                    Object value = map.get(field);
                    if("money".equals(field)){
                        if(value!=null){
                            values.add(map.get(field)+"元");
                        }
                    }else{
                        if("pay_time".equals(field)||"end_time".equals(field)||"begin_time".equals(field)){
                            if(map.get(field)!=null){
                                values.add(TimeTools.getTime_yyyyMMdd_HHmmss(Long.valueOf((map.get(field)+""))*1000));
                            }else{
                                values.add("");
                            }
                        }else{
                            values.add(map.get(field)+"");
                        }
                    }
                }
                bodyList.add(values);
            }
        }
        return bodyList;
    }

}