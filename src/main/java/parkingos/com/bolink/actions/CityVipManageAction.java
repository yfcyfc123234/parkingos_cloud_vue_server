package parkingos.com.bolink.actions;


import com.alibaba.fastjson.JSONObject;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import parkingos.com.bolink.dao.spring.CommonDao;
import parkingos.com.bolink.service.CityVipService;
import parkingos.com.bolink.utils.RequestUtil;
import parkingos.com.bolink.utils.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

@Controller
@RequestMapping("/cityvip")
public class CityVipManageAction {

    Logger logger = Logger.getLogger(CarRenewAction.class);


    @Autowired
    private CityVipService cityVipService;

    @Autowired
    private CommonDao commonDao;

    @RequestMapping(value = "/query")
    public String query(HttpServletRequest request, HttpServletResponse response) {

        Map<String, String> reqParameterMap = RequestUtil.readBodyFormRequset(request);

        JSONObject result = cityVipService.selectResultByConditions(reqParameterMap);

        StringUtils.ajaxOutput(response,result.toJSONString());
        return null;
    }



//    @RequestMapping(value = "add")
//    public String add(HttpServletRequest req, HttpServletResponse resp){
//
//        JSONObject result = cityVipService.createVip(req);
//
//        StringUtils.ajaxOutput(resp,result.toJSONString());
//
//        return null;
//    }



//    @RequestMapping(value = "/exportExcel")
//    public String exportExcel(HttpServletRequest request, HttpServletResponse response) {
//        Map<String, String> reqParameterMap = RequestUtil.readBodyFormRequset(request);
//
//        List<List<String>> bodyList = cityVipService.exportExcel(reqParameterMap);
////        String[] heards = new String[]{"编号","包月产品名称","车主手机"/*,"车主账户"*/,"名字","车牌号码","购买时间","开始时间","结束时间","金额","车型类型","单双日限行","备注"};
//        String[][] heards = new String[][]{{"编号","STR"},{"包月产品名称","STR"},{"车主手机","STR"}/*,"车主账户"*/,{"名字","STR"},{"车牌号码","STR"},{"购买时间","STR"},{"开始时间","STR"},{"结束时间","STR"},{"金额","STR"},{"车型类型","STR"},{"单双日限行","STR"},{"备注","STR"}};
//
//        ExportDataExcel excel = new ExportDataExcel("会员数据", heards, "sheet1");
//        String fname = "会员数据";
//        fname = StringUtils.encodingFileName(fname);
//        try {
//            OutputStream os = response.getOutputStream();
//            response.reset();
//            response.setHeader("Content-disposition", "attachment; filename="+fname+".xls");
//            excel.PoiWriteExcel_To2007(bodyList, os);
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (ParseException e) {
//            e.printStackTrace();
//        }
//
//        return null;
//    }

    @RequestMapping(value = "/importExcel")
    public String importExcel(HttpServletRequest request, HttpServletResponse resp,@RequestParam("file")MultipartFile file) throws Exception{


        System.out.println("===进入上传方法");
        Long groupid = RequestUtil.getLong(request,"groupid",-1L);
        Long cityid = RequestUtil.getLong(request,"cityid",-1L);
        System.out.println("===进入上传方法groupid:"+groupid+"===cityid:"+cityid);
        JSONObject result = cityVipService.importExcel(file,groupid,cityid);

        StringUtils.ajaxOutput(resp,result.toJSONString());

        return null;
    }


}