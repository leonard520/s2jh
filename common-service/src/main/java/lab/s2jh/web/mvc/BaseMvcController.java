package lab.s2jh.web.mvc;

import java.io.File;
import java.io.Serializable;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;

import lab.s2jh.core.annotation.MetaData;
import lab.s2jh.core.context.SpringContextHolder;
import lab.s2jh.core.entity.AttachmentableEntity;
import lab.s2jh.core.entity.BaseEntity;
import lab.s2jh.core.web.mvc.PersistableMvcController;
import lab.s2jh.core.web.util.ServletUtils;
import lab.s2jh.core.web.view.OperationResult;
import lab.s2jh.ctx.DynamicConfigService;
import lab.s2jh.sys.entity.AttachmentFile;
import lab.s2jh.sys.service.AttachmentFileService;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

/**
 * Spring MVC Base Controller for business entities - replacement for Struts BaseController
 * 
 * Extends PersistableMvcController with:
 * - Attachment file handling
 * - Clone operation support
 * - Image upload configuration
 */
public abstract class BaseMvcController<T extends BaseEntity<ID>, ID extends Serializable> extends
        PersistableMvcController<T, ID> {

    @Value("${struts.image.upload.maxSize:3145728}")
    protected long imageUploadMaxSize;

    public Long getImageUploadMaxSize() {
        return imageUploadMaxSize;
    }

    /**
     * Hook for clone initialization - override in subclass
     */
    protected void prepareClone() {
        // Subclass can add clone object initialization
    }

    /**
     * Edit with clone support
     */
    @Override
    @RequestMapping(value = "edit", method = RequestMethod.GET)
    public String edit(Model model) {
        T entity = getBindingEntity();
        String clone = getParameter("clone");
        if (BooleanUtils.toBoolean(clone) && entity != null) {
            entity.setId(null);
            entity.setVersion(0);
            prepareClone();
        }
        if (entity == null) {
            newBindingEntity();
            entity = getBindingEntity();
        }
        model.addAttribute("entity", entity);
        return resolveView("inputBasic");
    }

    /**
     * Save with attachment handling - override parent's doSave()
     */
    @Override
    @MetaData(value = "保存")
    @RequestMapping(value = "doSave", method = RequestMethod.POST)
    @ResponseBody
    public OperationResult doSave() {
        T entity = getBindingEntity();
        ID id = getId();
        if (id == null) {
            String msg = isDisallowCreate();
            if (msg != null) {
                return OperationResult.buildFailureResult(msg);
            }
        } else {
            String msg = isDisallowUpdate();
            if (msg != null) {
                return OperationResult.buildFailureResult(msg);
            }
        }
        
        getEntityService().save(entity);

        // Handle attachment binding with _attachment_ prefix parameters
        if (entity instanceof AttachmentableEntity) {
            Enumeration<?> names = getRequest().getParameterNames();
            Set<String> attachmentNames = Sets.newHashSet();
            while (names.hasMoreElements()) {
                String name = (String) names.nextElement();
                if (name.startsWith("_attachment_")) {
                    attachmentNames.add(name);
                }
            }
            if (attachmentNames.size() > 0) {
                AttachmentFileService attachmentFileService = SpringContextHolder.getBean(AttachmentFileService.class);
                for (String attachmentName : attachmentNames) {
                    String[] attachments = getParameterIds(attachmentName);
                    attachmentFileService.attachmentBind(attachments, entity,
                            StringUtils.substringAfter(attachmentName, "_attachment_"));
                }
            }
        }
        
        return OperationResult.buildSuccessResult("数据保存成功", entity);
    }

    /**
     * List attachments for entity
     */
    @RequestMapping(value = "attachmentList", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, List<Map<String, Object>>> attachmentList(@RequestParam(required = false) String category) {
        T entity = getBindingEntity();
        return attachmentListInternal(entity, category != null ? category : "default");
    }

    protected Map<String, List<Map<String, Object>>> attachmentListInternal(T entity, String category) {
        String url = getRequest().getServletPath();
        AttachmentFileService attachmentFileService = SpringContextHolder.getBean(AttachmentFileService.class);
        List<AttachmentFile> attachmentFiles = attachmentFileService.findBy(entity.getClass().getName(),
                String.valueOf(entity.getId()), category);
        
        List<Map<String, Object>> filesResponse = Lists.newArrayList();
        for (AttachmentFile attachmentFile : attachmentFiles) {
            Map<String, Object> dataMap = Maps.newHashMap();
            dataMap.put("id", attachmentFile.getId());
            dataMap.put("attachmentName", "_attachment_" + attachmentFile.getEntityFileCategory());
            dataMap.put("name", attachmentFile.getFileRealName());
            dataMap.put("size", FileUtils.byteCountToDisplaySize(attachmentFile.getFileLength()));
            dataMap.put("url", getRequest().getContextPath() + StringUtils.substringBefore(url, "/attachmentList")
                    + "/attachmentDownload?id=" + entity.getId() + "&attachmentId=" + attachmentFile.getId());
            filesResponse.add(dataMap);
        }
        
        Map<String, List<Map<String, Object>>> response = Maps.newHashMap();
        response.put("files", filesResponse);
        return response;
    }

    /**
     * Download attachment
     */
    @RequestMapping(value = "attachmentDownload", method = RequestMethod.GET)
    public void attachmentDownload(@RequestParam String attachmentId) {
        T entity = getBindingEntity();
        attachmentDownloadInternal(entity, attachmentId);
    }

    protected void attachmentDownloadInternal(T entity, String attachmentId) {
        try {
            AttachmentFileService attachmentFileService = SpringContextHolder.getBean(AttachmentFileService.class);
            AttachmentFile attachmentFile = attachmentFileService.findOne(attachmentId);
            if (attachmentFile != null && entity.getId().toString().equals(attachmentFile.getEntityId())
                    && entity.getClass().getName().equals(attachmentFile.getEntityClassName())) {
                HttpServletResponse response = getResponse();
                ServletUtils.setFileDownloadHeader(response, attachmentFile.getFileRealName());
                response.setContentType(attachmentFile.getFileType());

                DynamicConfigService dynamicConfigService = SpringContextHolder.getBean(DynamicConfigService.class);
                String rootPath = dynamicConfigService.getFileUploadRootDir();
                File diskFile = new File(rootPath + attachmentFile.getFileRelativePath() + File.separator
                        + attachmentFile.getDiskFileName());
                logger.debug("Downloading attachment file from disk: {}", diskFile.getAbsolutePath());
                ServletUtils.renderFileDownload(response, FileUtils.readFileToByteArray(diskFile));
            }
        } catch (Exception e) {
            logger.error("Download file error", e);
        }
    }
}
