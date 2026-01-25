package lab.s2jh.sys.web.mvc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManagerFactory;

import lab.s2jh.core.annotation.MetaData;
import lab.s2jh.core.context.SpringContextHolder;
import lab.s2jh.core.service.Validation;
import lab.s2jh.core.web.mvc.BaseMvcController;
import lab.s2jh.core.web.view.OperationResult;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.h2.tools.Server;
import org.hibernate.SessionFactory;
import org.hibernate.ejb.HibernateEntityManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseFactoryBean;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;

import com.google.common.collect.Maps;

/**
 * Utility controller - Spring MVC version
 */
@Controller
@RequestMapping("/admin/sys/util")
@MetaData(value = "辅助功能")
public class UtilMvcController extends BaseMvcController {

    protected final static Logger logger = LoggerFactory.getLogger(UtilMvcController.class);

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @Autowired
    private CacheManager cacheManager;

    @MetaData(value = "辅助管理")
    @RequestMapping(value = "mgmt", method = RequestMethod.GET)
    public String mgmt() {
        return "admin/sys/util-mgmt";
    }

    public List<String> getCacheNames() {
        List<String> datas = new ArrayList<String>();
        for (String cacheName : cacheManager.getCacheNames()) {
            datas.add(cacheName);
        }
        Collections.sort(datas);
        return datas;
    }

    @MetaData(value = "刷新数据缓存")
    @RequestMapping(value = "dataEvictCache", method = RequestMethod.POST)
    @ResponseBody
    public OperationResult dataEvictCache(@RequestParam(value = "cacheNames", required = false) String[] cacheNames) {
        SessionFactory sessionFactory = ((HibernateEntityManagerFactory) entityManagerFactory).getSessionFactory();

        if (cacheNames != null && cacheNames.length > 0 && cacheNames[0].trim().length() > 0) {
            if (logger.isInfoEnabled()) {
                logger.info("Evicting Hibernate&Spring Cache, Scope: {}", StringUtils.join(cacheNames, ","));
            }
            for (String cacheName : cacheNames) {
                if (StringUtils.isNotBlank(cacheName)) {
                    sessionFactory.getCache().evictQueryRegion(cacheName);
                    sessionFactory.getCache().evictEntityRegion(cacheName);
                    cacheManager.getCache(cacheName).clear();
                }
            }
        } else {
            logger.info("Evicting Hibernate&Spring Cache, Scope: All");
            sessionFactory.getCache().evictEntityRegions();
            sessionFactory.getCache().evictCollectionRegions();
            sessionFactory.getCache().evictQueryRegions();
            for (String cacheName : cacheManager.getCacheNames()) {
                cacheManager.getCache(cacheName).clear();
            }
        }
        return OperationResult.buildSuccessResult("数据缓存刷新操作成功");
    }

    public Map<String, String> getLoggerList() {
        Map<String, String> dataMap = new LinkedHashMap<String, String>();
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        List<ch.qos.logback.classic.Logger> loggers = loggerContext.getLoggerList();
        for (Logger logger : loggers) {
            dataMap.put(logger.getName(), logger.getName());
        }
        return dataMap;
    }

    @MetaData(value = "日志级别更新")
    @RequestMapping(value = "loggerLevelUpdate", method = RequestMethod.POST)
    @ResponseBody
    public OperationResult loggerLevelUpdate(
            @RequestParam(value = "loggerName", required = false) String loggerName,
            @RequestParam(value = "loggerLevel", required = false) String loggerLevel) {
        Validation.notDemoMode();
        if (StringUtils.isBlank(loggerName)) {
            loggerName = Logger.ROOT_LOGGER_NAME;
        }
        Logger targetLogger = LoggerFactory.getLogger(loggerName);
        ch.qos.logback.classic.Logger logbackLogger = (ch.qos.logback.classic.Logger) targetLogger;
        if (StringUtils.isNotBlank(loggerLevel)) {
            logbackLogger.setLevel(Level.toLevel(loggerLevel));
        }
        return OperationResult.buildSuccessResult("日志参数刷新操作成功");
    }

    @MetaData(value = "开发调试")
    @RequestMapping(value = "dev", method = RequestMethod.GET)
    public String dev() {
        return "admin/sys/util-dev";
    }

    private static Server h2Server;

    @MetaData(value = "启动H2 Server")
    @RequestMapping(value = "startH2", method = RequestMethod.POST)
    @ResponseBody
    public OperationResult startH2() throws Exception {
        Validation.notDemoMode();
        Map<String, String> datas = Maps.newHashMap();
        EmbeddedDatabaseFactoryBean db = SpringContextHolder.getBean(EmbeddedDatabaseFactoryBean.class);
        String databaseName = (String) FieldUtils.readField(db, "databaseName", true);
        databaseName = "jdbc:h2:file:" + databaseName;
        logger.info("H2 JDBC URL: {}", databaseName);
        if (h2Server == null) {
            h2Server = Server.createWebServer();
        }
        try {
            logger.info("Starting H2 Server...");
            h2Server.start();
        } catch (Exception e) {
            logger.warn(e.getMessage());
        }
        datas.put("h2LoginUrl", "http://localhost:" + h2Server.getPort() + "/login.jsp");
        datas.put("h2JdbcUrl", databaseName);
        return OperationResult.buildSuccessResult("H2 Server已成功加载", datas);
    }

    @MetaData(value = "关闭H2 Server")
    @RequestMapping(value = "stopH2", method = RequestMethod.POST)
    @ResponseBody
    public OperationResult stopH2() throws Exception {
        Validation.notDemoMode();
        if (h2Server != null) {
            logger.info("Stopping H2 Server...");
            h2Server.stop();
        }
        return OperationResult.buildSuccessResult("H2 Server已关闭");
    }
}
