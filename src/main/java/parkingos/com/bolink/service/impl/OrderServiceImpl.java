package parkingos.com.bolink.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import parkingos.com.bolink.dao.spring.CommonDao;
import parkingos.com.bolink.models.CarpicTb;
import parkingos.com.bolink.models.ComPassTb;
import parkingos.com.bolink.models.OrderTb;
import parkingos.com.bolink.models.UserInfoTb;
import parkingos.com.bolink.service.OrderService;
import parkingos.com.bolink.service.SupperSearchService;
import parkingos.com.bolink.utils.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service("orderSpring")
public class OrderServiceImpl implements OrderService {

    Logger logger = Logger.getLogger(OrderServiceImpl.class);

    @Autowired
    private CommonDao commonDao;
    @Autowired
    private SupperSearchService<OrderTb> supperSearchService;

    @Override
    public int selectCountByConditions(OrderTb orderTb) {
        return 0;
    }

    @Override
    public JSONObject selectResultByConditions(Map<String, String> reqmap) {

        //查询三天的数据显示
        logger.error("=========..req"+reqmap);
        OrderTb orderTb = new OrderTb();
        orderTb.setComid(Long.parseLong(reqmap.get("comid")));
        String createTime = reqmap.get("create_time");
        String endTime = reqmap.get("end_time");
        //组装 一个月 参数
        if(endTime==null||"".equals(endTime)){
            if(createTime==null||"undefined".equals(createTime)||"".equals(createTime)){
                reqmap.put("create_time","1");
                reqmap.put("create_time_start",(TimeTools.getToDayBeginTime()-2*86400)+"");
                logger.error("=========..req"+reqmap.size());
            }
        }
        JSONObject result = supperSearchService.supperSearch(orderTb, reqmap);

        //时长重新处理  收款人和收费员重新处理
        List<OrderTb> orderList = JSON.parseArray(result.get("rows").toString(), OrderTb.class);
        List<Map<String, Object>> resList =new ArrayList<>();
        for(OrderTb order : orderList){
            OrmUtil<OrderTb> om = new OrmUtil<>();
            Map map = om.pojoToMap(order);
            Long start = (Long) map.get("create_time");
            Long end = (Long) map.get("end_time");
            if (start != null && end != null) {
                map.put("duration",StringUtils.getTimeString(start, end));
            } else {
                map.put("duration","");
            }
            resList.add(map);
        }
        result.remove("rows");
        result.put("rows",JSON.toJSON(resList));
        //车位数据
//        result.put("parktotal",total);
//        result.put("blank",blank);

        logger.error("============>>>>>返回数据"+result);
        return result;
    }

    @Override
    public JSONObject getPicResult(String orderid, Long comid) {

        String str = "{\"in\":[],\"out\":[]}";
        JSONObject result = JSONObject.parseObject(str);

        DB db = MongoClientFactory.getInstance().getMongoDBBuilder("zld");
        //根据订单编号查询出mongodb中存入的对应个表名
        //Map map = daService.getMap("select * from order_tb where order_id_local=? and comid=?", new Object[]{orderid,comid});
        OrderTb orderTb = new OrderTb();
        orderTb.setOrderIdLocal(orderid + "");
        orderTb.setComid(comid);
        orderTb = (OrderTb) commonDao.selectObjectByConditions(orderTb);

//        Calendar calendar=Calendar.getInstance();
//        //获得当前时间的月份，月份从0开始所以结果要加1
//        int month=calendar.get(Calendar.MONTH)+1;
//        logger.info("这是今年的"+month);
//        String monthStr = "";
//        if(month<10){
//            monthStr="0"+month;
//        }else{
//            monthStr = month+"";
//        }
//        String sql = "select * from order_tb_2018_"+monthStr+" where comid="+comid+" and ishd = 0"+" and order_id_local = '"+orderid+"'";
//        List<Map<String,Object>> list = commonDao.getObjectBySql(sql);
//        if(list==null||list.isEmpty()){
//            month=month-1;
//            if(month<10){
//                monthStr="0"+month;
//            }else{
//                monthStr = month+"";
//            }
//            sql = "select * from order_tb_2018_"+monthStr+" where comid="+comid+" and ishd = 0"+" and order_id_local = '"+orderid+"'";
//            logger.info("==============sql2"+sql);
//            list = commonDao.getObjectBySql(sql);
//        }

        String collectionName = "";
        if (orderTb != null && orderTb.getCarpicTableName() != null) {
            collectionName = orderTb.getCarpicTableName();
        }

//        if(list!=null&&list.size()>0){
//            if(list.get(0).get("carpic_table_name")!=null){
//                collectionName=list.get(0).get("carpic_table_name")+"";
//            }
//        }

        logger.error("====>>获得订单图片..collectionName" + collectionName);
        DBCollection collection = db.getCollection("collectionName");
//        DBCollection collection = db.getCollection(collectionName);
        logger.error("======>>>>.获取订单图片...collection" + collection);

        List<String> inlist = new ArrayList<>();
        List<String> outlist = new ArrayList<>();
        inlist.add("/order/carpicsup?comid=" + comid + "&typeNew=in&orderid=" + orderid);
        outlist.add("/order/carpicsup?comid=" + comid + "&typeNew=out&orderid=" + orderid);
        logger.error("=======>>>获取订单图片..inlist..outlist"+inlist.size()+"==>>"+outlist.size());
        if (collection != null) {
            BasicDBObject document = new BasicDBObject();
            document.put("parkid", String.valueOf(comid));
            document.put("orderid", orderid + "");
            document.put("gate", "in");
            Long insize = collection.count(document);
            document.put("gate", "out");
            Long outsize = collection.count(document);

            if (insize > 1) {
                for (int i = 0; i < insize; i++) {
                    inlist.add("/order/carpicsup?comid=" + comid + "&typeNew=in&currentnum=" + i + "&orderid=" + orderid);
                }
            }
            if (outsize > 1) {
                for (int i = 0; i < outsize; i++) {
                    outlist.add("/order/carpicsup?comid=" + comid + "&typeNew=out&currentnum=" + i + "&orderid=" + orderid);
                }
            }
        }

        result.put("in", JSON.toJSON(inlist));
        result.put("out", JSON.toJSON(outlist));
        return result;
    }

    @Override
    public byte[] getCarPics(String orderid, Long comid, String type, Integer currentnum) {
        logger.error("getcarPic from mongodb file:orderid=" + orderid + "type=" + type + ",comid:" + comid + ",currentnum=" + currentnum);
        if (orderid != null && type != null) {
            DB db = MongoClientFactory.getInstance().getMongoDBBuilder("zld");//
            //根据订单编号查询出mongodb中存入的对应个表名
            //Map map = daService.getMap("select * from carpic_tb where order_id=? and comid=?", new Object[]{orderidlocal,String.valueOf(comid)});
            CarpicTb carpicTb = new CarpicTb();
            carpicTb.setOrderId(orderid + "");
            carpicTb.setComid(comid + "");
            carpicTb = (CarpicTb) commonDao.selectObjectByConditions(carpicTb);

            logger.error(carpicTb);
            String collectionName = "";
            if (carpicTb != null && carpicTb.getCarpicTableName() != null) {
                collectionName = carpicTb.getCarpicTableName();
            }

            logger.error("table:" + collectionName);
            if (collectionName == null || "".equals(collectionName) || "null".equals(collectionName)) {
                logger.error(">>>>>>>>>>>>>查询图片错误........");
                return new byte[0];
            }

            DBCollection collection = db.getCollection(collectionName);
            if (collection != null) {
                BasicDBObject document = new BasicDBObject();
                document.put("parkid", String.valueOf(comid));
                document.put("orderid", orderid + "");
                document.put("gate", type);
                if (currentnum >= 0) {
                    document.put("currentnum", currentnum);
                }
                DBObject obj = collection.findOne(document);
                if (obj == null) {
                    logger.error("取图片错误.....");
                    return new byte[0];
                }
                byte[] content = (byte[]) obj.get("content");
                db.requestDone();
                logger.error("取图片成功.....大小:" + content.length);
                System.out.println("mongdb over.....");
                return content;

            } else {
                return new byte[0];
//                response.sendRedirect("http://sysimages.tq.cn/images/webchat_101001/common/kefu.png");
            }
        } else {
            return new byte[0];
//            response.sendRedirect("http://sysimages.tq.cn/images/webchat_101001/common/kefu.png");
        }
    }

    @Override
    public List<List<Object>> exportExcel(Map<String, String> reqParameterMap) {

        //删除分页条件  查询该条件下所有  不然为一页数据
        reqParameterMap.remove("orderfield");
        reqParameterMap.remove("orderby");

        //获得要导出的结果
        JSONObject result = selectResultByConditions(reqParameterMap);

        Long comid = Long.parseLong(reqParameterMap.get("comid"));

        List<OrderTb> orderlist = JSON.parseArray(result.get("rows").toString(), OrderTb.class);

        logger.error("=========>>>>>>.导出订单" + orderlist.size());
        List<List<Object>> bodyList = new ArrayList<List<Object>>();
//        List<List<String>> bodyList = new ArrayList<List<String>>();
        if (orderlist != null && orderlist.size() > 0) {
            String[] f = new String[]{"c_type", "car_number","car_type", "create_time", "end_time", "duration", "pay_type", "freereasons","amount_receivable", "total", "electronic_prepay", "cash_prepay", "electronic_pay", "cash_pay", "reduce_amount", "uid", "out_uid", "state", "in_passid", "out_passid","order_id_local"};
            Map<Long, String> uinNameMap = new HashMap<Long, String>();
            Map<Integer, String> passNameMap = new HashMap<Integer, String>();
            for (OrderTb orderTb : orderlist) {
//                List<String> values = new ArrayList<String>();
                List<Object> values = new ArrayList<Object>();
                OrmUtil<OrderTb> otm = new OrmUtil<>();
                Map map = otm.pojoToMap(orderTb);
                for (String field : f) {
                    Object v = map.get(field);
                    if (v == null)
                        v = "";
                    if ("uid".equals(field) || "out_uid".equals(field)) {
                        Long uid = -1L;
                        if (Check.isLong(v + ""))
                            uid = Long.valueOf(v + "");
                        if (uinNameMap.containsKey(uid))
                            values.add(uinNameMap.get(uid));
                        else {
                            String name = getUinName(Long.valueOf(map.get(field) + ""));
                            values.add(name);
                            uinNameMap.put(uid, name);
                        }
                    } else if ("c_type".equals(field)) {
                        if (Check.isLong(v + "")) {
                            switch (Integer.valueOf(v + "")) {//0:NFC,1:IBeacon,2:照牌   3通道照牌 4直付 5月卡用户
                                case 0:
                                    values.add("NFC刷卡");
                                    break;
                                case 1:
                                    values.add("Ibeacon");
                                    break;
                                case 2:
                                    values.add("手机扫牌");
                                    break;
                                case 3:
                                    values.add("通道扫牌");
                                    break;
                                case 4:
                                    values.add("直付");
                                    break;
                                case 5:
                                    values.add("月卡");
                                    break;
                                default:
                                    values.add("");
                            }
                        } else {
                            values.add(v + "");
                        }
                    } else if ("duration".equals(field)) {
                        Long start = (Long) map.get("create_time");
                        Long end = (Long) map.get("end_time");
                        if (start != null && end != null) {
                            values.add(StringUtils.getTimeString(start, end));
                        } else {
                            values.add("");
                        }
                    } else if ("pay_type".equals(field)) {
                        switch (Integer.valueOf(v + "")) {//0:NFC,1:IBeacon,2:照牌   3通道照牌 4直付 5月卡用户
                            case 1:
                                values.add("现金支付");
                                break;
                            case 2:
                                values.add("手机支付");
                                break;
                            case 3:
                                values.add("包月");
                                break;
                            case 8:
                                values.add("免费");
                                break;
                            default:
                                values.add("");
                        }
                    } else if ("state".equals(field)) {
                        switch (Integer.valueOf(v + "")) {//0:NFC,1:IBeacon,2:照牌   3通道照牌 4直付 5月卡用户
                            case 0:
                                values.add("未结算 ");
                                break;
                            case 1:
                                values.add("已结算 ");
                                break;
                            default:
                                values.add("");
                        }
                    } else if ("isclick".equals(field)) {
                        switch (Integer.valueOf(map.get(field) + "")) {//0:NFC,1:IBeacon,2:照牌   3通道照牌 4直付 5月卡用户
                            case 0:
                                values.add("系统结算");
                                break;
                            case 1:
                                values.add("手动结算");
                                break;
                            default:
                                values.add("");
                        }
                    } else if ("in_passid".equals(field) || "out_passid".equals(field)) {
                        if (!"".equals(v.toString()) && Check.isNumber(v.toString())) {
                            Integer passId = Integer.valueOf(v.toString());
                            if (passNameMap.containsKey(passId))
                                values.add(passNameMap.get(passId));
                            else {
                                String passName = getPassName(comid, passId);
                                values.add(passName);
                                passNameMap.put(passId, passName);
                            }
                        } else {
                            values.add(v + "");
                        }
                    } else {
                        if ("create_time".equals(field) || "end_time".equals(field)) {
                            if (!"".equals(v.toString())) {
                                values.add(TimeTools.getTime_yyyyMMdd_HHmmss(Long.valueOf((v + "")) * 1000));
                            } else {
                                values.add("null");
                            }
                        } else {
                            values.add(v + "");
                        }
                    }
                }
                bodyList.add(values);
            }
        }
        return bodyList;
    }

    @Override
    public Long getComidByOrder(Long id) {
        OrderTb orderTb = new OrderTb();
        orderTb.setId(id);
        orderTb = (OrderTb)commonDao.selectObjectByConditions(orderTb);
        if(orderTb!=null&&orderTb.getComid()!=null){
            return orderTb.getComid();
        }
        return -1L;
    }

    private String getPassName(Long comId,Integer passId) {
        ComPassTb comPassTb = new ComPassTb();
        comPassTb.setComid(comId);
        comPassTb.setId(passId.longValue());
        comPassTb = (ComPassTb)commonDao.selectObjectByConditions(comPassTb);
        if(comPassTb!=null&&comPassTb.getPassname()!=null){
            return comPassTb.getPassname();
        }
        return "";
    }

    private String getUinName(Long uin) {
        UserInfoTb userInfoTb = new UserInfoTb();
        userInfoTb.setId(uin);
        userInfoTb = (UserInfoTb)commonDao.selectObjectByConditions(userInfoTb);

        String uinName = "";
        if(userInfoTb!=null&&userInfoTb.getNickname()!=null){
            return userInfoTb.getNickname();
        }
        return uinName;
    }
}
