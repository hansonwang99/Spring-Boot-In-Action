package cn.codesheep.springbt_vesta.controller;

import cn.codesheep.springbt_vesta.service.UidService;
import com.robert.vesta.service.bean.Id;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UidController {

    @Autowired
    private UidService uidService;

    @RequestMapping("/genid")
    public long genId() {
        return uidService.genId();
    }

    @RequestMapping("/expid")
    public Id explainId(@RequestParam(value = "id", defaultValue = "0") long id) {
        return uidService.explainId( id );
    }

    @RequestMapping("/transtime")
    public String transTime( @RequestParam(value = "time", defaultValue = "-1") long time ) {
        return uidService.transTime(time);
    }

    @RequestMapping("/makeid")
    public long makeId(
            @RequestParam(value = "version", defaultValue = "-1") long version,
            @RequestParam(value = "type", defaultValue = "-1") long type,
            @RequestParam(value = "genMethod", defaultValue = "-1") long genMethod,
            @RequestParam(value = "machine", defaultValue = "-1") long machine,
            @RequestParam(value = "time", defaultValue = "-1") long time,
            @RequestParam(value = "seq", defaultValue = "-1") long seq) {

        return uidService.makeId( version, type, genMethod, machine, time, seq );
    }
}