/**
 * Copyright (c) 2012-2026
 */
package lab.s2jh.core.test;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.sql.DataSource;

import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

/**
 * Spring MVC Controller集成测试基类
 * 
 * 使用Spring MVC Test框架进行Controller层测试，支持：
 * - MockMvc模拟HTTP请求
 * - 事务自动回滚
 * - WebApplicationContext注入
 * 
 * 子类示例:
 * <pre>
 * public class UserMvcControllerTest extends SpringMvcTestCase {
 *     @Test
 *     public void testFindByPage() throws Exception {
 *         mockMvc.perform(get("/admin/auth/user/findByPage"))
 *             .andExpect(status().isOk())
 *             .andExpect(jsonPath("$.content").isArray());
 *     }
 * }
 * </pre>
 */
@ActiveProfiles("test")
@WebAppConfiguration
@ContextConfiguration(locations = { 
    "classpath:/context/context-profiles.xml", 
    "classpath*:/context/spring*.xml",
    "classpath:/context/mvc-test-context.xml"
})
public abstract class SpringMvcTestCase extends AbstractTransactionalJUnit4SpringContextTests {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    protected WebApplicationContext webApplicationContext;

    protected MockMvc mockMvc;

    protected DataSource dataSource;

    @PersistenceContext
    protected EntityManager entityManager;

    /**
     * JSON媒体类型常量
     */
    protected static final MediaType JSON = MediaType.APPLICATION_JSON;

    /**
     * 表单媒体类型常量
     */
    protected static final MediaType FORM = MediaType.APPLICATION_FORM_URLENCODED;

    @Override
    @Autowired
    public void setDataSource(DataSource dataSource) {
        super.setDataSource(dataSource);
        this.dataSource = dataSource;
    }

    /**
     * 初始化MockMvc
     */
    @Before
    public void setupMockMvc() {
        this.mockMvc = MockMvcBuilders
            .webAppContextSetup(webApplicationContext)
            .build();
    }

    /**
     * 刷新EntityManager，确保数据已经持久化
     */
    protected void flush() {
        entityManager.flush();
    }

    /**
     * 清理EntityManager一级缓存
     */
    protected void clear() {
        entityManager.clear();
    }

    /**
     * 刷新并清理缓存
     */
    protected void flushAndClear() {
        flush();
        clear();
    }
}
