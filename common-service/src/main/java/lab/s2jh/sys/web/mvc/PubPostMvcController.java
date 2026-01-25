package lab.s2jh.sys.web.mvc;

import java.util.List;
import java.util.Map;

import lab.s2jh.core.annotation.MetaData;
import lab.s2jh.core.service.BaseService;
import lab.s2jh.core.web.view.OperationResult;
import lab.s2jh.sys.entity.PubPost;
import lab.s2jh.sys.service.AttachmentFileService;
import lab.s2jh.sys.service.PubPostService;
import lab.s2jh.web.mvc.BaseMvcController;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Public post management controller - Spring MVC version
 */
@Controller
@RequestMapping("/admin/sys/pub-post")
@MetaData(value = "公告管理")
public class PubPostMvcController extends BaseMvcController<PubPost, String> {

    @Autowired
    private PubPostService pubPostService;

    @Autowired
    private AttachmentFileService attachmentFileService;

    @Override
    protected BaseService<PubPost, String> getEntityService() {
        return pubPostService;
    }

    @Override
    protected void checkEntityAclPermission(PubPost entity) {
        // Nothing
    }

    @Override
    @MetaData(value = "保存")
    @RequestMapping(value = "doSave", method = RequestMethod.POST)
    @ResponseBody
    public OperationResult doSave() {
        return super.doSave();
    }

    @Override
    @MetaData(value = "删除")
    @RequestMapping(value = "doDelete", method = RequestMethod.POST)
    @ResponseBody
    public OperationResult doDelete() {
        return super.doDelete();
    }

    @Override
    @MetaData(value = "查询")
    @RequestMapping(value = "findByPage", method = RequestMethod.GET)
    @ResponseBody
    public Page<PubPost> findByPage() {
        return super.findByPage();
    }

    @MetaData(value = "关联附件列表")
    @RequestMapping(value = "attachmentList", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, List<Map<String, Object>>> attachmentList(@RequestParam(required = false) String category) {
        return attachmentListInternal(bindingEntity, category != null ? category : "default");
    }

    @MetaData(value = "关联附件下载")
    @RequestMapping(value = "attachmentDownload", method = RequestMethod.GET)
    public void attachmentDownload(@RequestParam String attachmentId) {
        attachmentDownloadInternal(bindingEntity, attachmentId);
    }
}
