package lab.s2jh.sys.web.mvc;

import java.io.File;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import lab.s2jh.core.annotation.MetaData;
import lab.s2jh.core.service.BaseService;
import lab.s2jh.core.web.util.ServletUtils;
import lab.s2jh.core.web.view.OperationResult;
import lab.s2jh.ctx.DynamicConfigService;
import lab.s2jh.sys.entity.AttachmentFile;
import lab.s2jh.sys.service.AttachmentFileService;
import lab.s2jh.web.mvc.BaseMvcController;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * Attachment file controller - Spring MVC version
 */
@Controller
@RequestMapping("/admin/sys/attachment-file")
@MetaData(value = "附件处理")
public class AttachmentFileMvcController extends BaseMvcController<AttachmentFile, String> {

    private final Logger logger = LoggerFactory.getLogger(AttachmentFileMvcController.class);

    @Autowired
    private AttachmentFileService attachmentFileService;

    @Autowired
    private DynamicConfigService dynamicConfigService;

    @Override
    protected BaseService<AttachmentFile, String> getEntityService() {
        return attachmentFileService;
    }

    @Override
    protected void checkEntityAclPermission(AttachmentFile entity) {
        // Do nothing check
    }

    @Override
    @MetaData(value = "删除")
    @RequestMapping(value = "doDelete", method = RequestMethod.POST)
    @ResponseBody
    public OperationResult doDelete() {
        return super.doDelete();
    }

    private AttachmentFile saveAttachmentFile(MultipartFile file) throws Exception {
        AttachmentFile entity = AttachmentFile.buildInstance(file);
        entity.setFileRealName(file.getOriginalFilename());
        entity.setFileType(file.getContentType());
        entity.setFileExtension(StringUtils.substringAfterLast(entity.getFileRealName(), "."));

        String rootPath = dynamicConfigService.getFileUploadRootDir();
        File diskFileDir = new File(rootPath + entity.getFileRelativePath());
        if (!diskFileDir.exists()) {
            diskFileDir.mkdirs();
        }
        File diskFile = new File(rootPath + entity.getFileRelativePath() + File.separator + entity.getDiskFileName());
        logger.debug("Saving attachment file to disk: {}", diskFile.getAbsolutePath());
        file.transferTo(diskFile);
        logger.debug("Saving attachment record to database: {}", entity.getFileRealName());
        attachmentFileService.save(entity);
        return entity;
    }

    @MetaData(value = "多文件上传")
    @RequestMapping(value = "uploadMulti", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, List<Map<String, Object>>> uploadMulti(
            @RequestParam("files") MultipartFile[] files,
            @RequestParam("attachmentName") String attachmentName) {
        List<Map<String, Object>> filesResponse = Lists.newArrayList();
        for (MultipartFile file : files) {
            Map<String, Object> dataMap = Maps.newHashMap();
            try {
                AttachmentFile entity = saveAttachmentFile(file);
                dataMap.put("id", entity.getId());
                dataMap.put("attachmentName", attachmentName);
                dataMap.put("name", file.getOriginalFilename());
                dataMap.put("size", FileUtils.byteCountToDisplaySize(entity.getFileLength()));
                dataMap.put("url", getRequest().getContextPath() + "/admin/sys/attachment-file/download?id=" + entity.getId());
            } catch (Exception e) {
                logger.warn("Attachment file upload failure", e);
                dataMap.put("name", file.getOriginalFilename());
                dataMap.put("error", e.getMessage());
            }
            filesResponse.add(dataMap);
        }
        Map<String, List<Map<String, Object>>> response = Maps.newHashMap();
        response.put("files", filesResponse);
        return response;
    }

    @MetaData(value = "文件下载")
    @RequestMapping(value = "download", method = RequestMethod.GET)
    public void download(@RequestParam("id") String id) {
        try {
            AttachmentFile entity = attachmentFileService.findOne(id);
            // 附件访问控制：未关联附件才允许通用访问下载，已关联附件只能通过各业务对象入口访问
            Assert.isTrue(StringUtils.isBlank(entity.getEntityId()));
            HttpServletResponse response = getResponse();
            ServletUtils.setFileDownloadHeader(response, entity.getFileRealName());
            response.setContentType(entity.getFileType());
            String rootPath = dynamicConfigService.getFileUploadRootDir();
            File diskFile = new File(rootPath + entity.getFileRelativePath() + File.separator
                    + entity.getDiskFileName());

            ServletUtils.renderFileDownload(response, FileUtils.readFileToByteArray(diskFile));
        } catch (Exception e) {
            logger.error("Download file error", e);
        }
    }

    @MetaData(value = "文件删除")
    @RequestMapping(value = "delete", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, List<Map<String, Object>>> delete(@RequestParam("id") String id) {
        AttachmentFile entity = attachmentFileService.findOne(id);
        List<Map<String, Object>> filesResponse = Lists.newArrayList();
        if (entity != null && entity.getEntityId() == null) {
            Map<String, Object> dataMap = Maps.newHashMap();
            dataMap.put(entity.getFileRealName(), true);
            attachmentFileService.delete(entity);
            filesResponse.add(dataMap);
        }
        Map<String, List<Map<String, Object>>> response = Maps.newHashMap();
        response.put("files", filesResponse);
        return response;
    }
}
