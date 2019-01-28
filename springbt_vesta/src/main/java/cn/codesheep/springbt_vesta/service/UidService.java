package cn.codesheep.springbt_vesta.service;

import com.robert.vesta.service.bean.Id;
import com.robert.vesta.service.intf.IdService;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;

@Service
public class UidService {

    @Resource
    private IdService idService;

    // 若没有建立UidConfig类来专门引入ext/vesta/vesta-rest-main.xml文件，则用下面这种方式来手工获取bean也是可以的！！！
//    public UidService() {
//        ApplicationContext ac = new ClassPathXmlApplicationContext(
//                "ext/vesta/vesta-rest-main.xml");
//
//        idService = (IdService) ac.getBean("idService");
//    }

    public long genId() {
        return idService.genId();
    }

    public Id explainId( long id ) {
        return idService.expId(id);
    }

    public String transTime( long time ) {
        return idService.transTime(time).toString();
    }

    public long makeId( long version, long type, long genMethod, long machine, long time, long seq ) {

        long madeId = -1;
        if (time == -1 || seq == -1)
            throw new IllegalArgumentException( "Both time and seq are required." );
        else if (version == -1) {
            if (type == -1) {
                if (genMethod == -1) {
                    if (machine == -1) {
                        madeId = idService.makeId(time, seq);
                    } else {
                        madeId = idService.makeId(machine, time, seq);
                    }
                } else {
                    madeId = idService.makeId(genMethod, machine, time, seq);
                }
            } else {
                madeId = idService.makeId(type, genMethod, machine, time, seq);
            }
        } else {
            madeId = idService.makeId(version, type, genMethod, time,
                    seq, machine);
        }

        return madeId;
    }

}
