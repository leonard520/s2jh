package lab.s2jh.sys.web.mvc;

import lab.s2jh.core.annotation.MetaData;
import lab.s2jh.core.service.BaseService;
import lab.s2jh.sys.entity.PubPostRead;
import lab.s2jh.sys.service.PubPostReadService;
import lab.s2jh.web.mvc.BaseMvcController;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Public post read record controller - Spring MVC version
 */
@Controller
@RequestMapping("/admin/sys/pub-post-read")
@MetaData(value = "公告阅读记录")
public class PubPostReadMvcController extends BaseMvcController<PubPostRead, String> {

    @Autowired
    private PubPostReadService pubPostReadService;

    @Override
    protected BaseService<PubPostRead, String> getEntityService() {
        return pubPostReadService;
    }

    @Override
    protected void checkEntityAclPermission(PubPostRead entity) {
        // TODO Add acl check code logic
    }

    @Override
    @MetaData(value = "查询")
    @RequestMapping(value = "findByPage", method = RequestMethod.GET)
    @ResponseBody
    public Page<PubPostRead> findByPage() {
        return super.findByPage();
    }
}
