package parkingos.com.bolink.service.impl;

import com.alibaba.fastjson.JSONObject;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import parkingos.com.bolink.dao.spring.CommonDao;
import parkingos.com.bolink.models.CarTypeTb;
import parkingos.com.bolink.models.ComPassTb;
import parkingos.com.bolink.models.SyncInfoPoolTb;
import parkingos.com.bolink.service.EquipmentManageChannelService;
import parkingos.com.bolink.service.SupperSearchService;

import java.util.Map;

@Service
public class EquipmentManageChannelServiceImpl implements EquipmentManageChannelService {

    Logger logger = Logger.getLogger(EquipmentManageChannelServiceImpl.class);


    @Autowired
    private SupperSearchService<ComPassTb> supperSearchService;
    @Autowired
    private CommonDao commonDao;

    @Override
    public JSONObject selectResultByConditions(Map<String, String> reqmap) {
        Long comid = Long.valueOf(Integer.valueOf(reqmap.get("comid")));
        ComPassTb comPassTb = new ComPassTb();
        comPassTb.setState(0);
        comPassTb.setComid(comid);
        JSONObject result = supperSearchService.supperSearch(comPassTb,reqmap);

        return result;
    }

    @Override
    @Transactional
    public Integer insertResultByConditions(ComPassTb comPassTb) {
        Integer result = commonDao.insert(comPassTb);
        if(result==1){
            SyncInfoPoolTb syncInfoPoolTb = new SyncInfoPoolTb();
            syncInfoPoolTb.setOperate(0);
            syncInfoPoolTb.setComid(comPassTb.getComid());
            syncInfoPoolTb.setCreateTime(System.currentTimeMillis()/1000);
            syncInfoPoolTb.setTableId(comPassTb.getId());
            syncInfoPoolTb.setTableName("com_pass_tb");
            syncInfoPoolTb.setState(0);
            int ins = commonDao.insert(syncInfoPoolTb);
            logger.info("插入同步表:"+ins);
        }
        return result;
    }

    @Override
    @Transactional
    public Integer updateResultByConditions(ComPassTb comPassTb) {
        Integer result = commonDao.updateByPrimaryKey(comPassTb);
        if(result==1){
            ComPassTb con = new ComPassTb();
            con.setId(comPassTb.getId());
            comPassTb=(ComPassTb)commonDao.selectObjectByConditions(con);
            SyncInfoPoolTb syncInfoPoolTb = new SyncInfoPoolTb();
            syncInfoPoolTb.setOperate(1);
            syncInfoPoolTb.setComid(comPassTb.getComid());
            syncInfoPoolTb.setCreateTime(System.currentTimeMillis()/1000);
            syncInfoPoolTb.setTableId(comPassTb.getId());
            syncInfoPoolTb.setTableName("com_pass_tb");
            syncInfoPoolTb.setState(0);
            int ins = commonDao.insert(syncInfoPoolTb);
            logger.info("插入同步表:"+ins);
        }
        return result;
    }

    @Override
    @Transactional
    public Integer removeResultByConditions(ComPassTb comPassTb) {
        Integer result = commonDao.updateByPrimaryKey(comPassTb);
        if(result==1){
            ComPassTb con = new ComPassTb();
            con.setId(comPassTb.getId());
            comPassTb=(ComPassTb)commonDao.selectObjectByConditions(con);
            SyncInfoPoolTb syncInfoPoolTb = new SyncInfoPoolTb();
            syncInfoPoolTb.setOperate(2);
            syncInfoPoolTb.setComid(comPassTb.getComid());
            syncInfoPoolTb.setCreateTime(System.currentTimeMillis()/1000);
            syncInfoPoolTb.setTableId(comPassTb.getId());
            syncInfoPoolTb.setTableName("com_pass_tb");
            syncInfoPoolTb.setState(0);
            int ins = commonDao.insert(syncInfoPoolTb);
            logger.info("插入同步表:"+ins);
        }
        return result;
    }

    @Override
    public Long getId() {
        return commonDao.selectSequence(ComPassTb.class);
    }

}
