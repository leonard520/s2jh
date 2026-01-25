package lab.s2jh.sys.web.mvc.test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;

import lab.s2jh.core.test.SpringMvcTestCase;
import lab.s2jh.core.test.TestObjectUtils;
import lab.s2jh.sys.entity.DataDict;
import lab.s2jh.sys.service.DataDictService;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

/**
 * DataDictMvcController集成测试
 * 
 * 测试数据字典管理控制器的CRUD操作和分页查询功能
 */
public class DataDictMvcControllerTest extends SpringMvcTestCase {

    @Autowired
    private DataDictService dataDictService;

    /**
     * 测试分页查询接口
     * GET /admin/sys/data-dict/findByPage
     */
    @Test
    public void testFindByPage() throws Exception {
        // 准备测试数据
        DataDict dict = TestObjectUtils.buildMockObject(DataDict.class);
        dict.setPrimaryKey("TEST_DICT_001");
        dict.setPrimaryValue("Test Value");
        dataDictService.save(dict);
        flushAndClear();

        // 执行请求并验证
        mockMvc.perform(get("/admin/sys/data-dict/findByPage")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.content").isArray())
            .andExpect(jsonPath("$.totalElements").exists());
    }

    /**
     * 测试新增数据字典接口
     * POST /admin/sys/data-dict/doSave
     */
    @Test
    public void testDoSaveCreate() throws Exception {
        mockMvc.perform(post("/admin/sys/data-dict/doSave")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("primaryKey", "NEW_DICT_001")
                .param("primaryValue", "New Dictionary Value")
                .param("secondaryKey", "")
                .param("secondaryValue", ""))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.type").value("success"));
    }

    /**
     * 测试更新数据字典接口
     * POST /admin/sys/data-dict/doSave (with existing id)
     */
    @Test
    public void testDoSaveUpdate() throws Exception {
        // 准备测试数据
        DataDict dict = TestObjectUtils.buildMockObject(DataDict.class);
        dict.setPrimaryKey("UPDATE_DICT_001");
        dict.setPrimaryValue("Original Value");
        dataDictService.save(dict);
        flushAndClear();

        String dictId = dict.getId();

        // 更新数据字典
        mockMvc.perform(post("/admin/sys/data-dict/doSave")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("id", dictId)
                .param("primaryKey", "UPDATE_DICT_001")
                .param("primaryValue", "Updated Value"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.type").value("success"));
    }

    /**
     * 测试删除数据字典接口
     * POST /admin/sys/data-dict/doDelete?ids=1,2,3
     */
    @Test
    public void testDoDelete() throws Exception {
        // 准备测试数据
        DataDict dict = TestObjectUtils.buildMockObject(DataDict.class);
        dict.setPrimaryKey("DELETE_DICT_001");
        dict.setPrimaryValue("To Delete");
        dataDictService.save(dict);
        flushAndClear();

        String dictId = dict.getId();

        // 删除数据字典
        mockMvc.perform(post("/admin/sys/data-dict/doDelete")
                .param("ids", dictId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.type").value("success"));
    }

    /**
     * 测试按主键查询数据字典列表
     * 使用findByPage接口代替专用的list接口
     * 
     * TODO: 如需专用的list接口，需要在DataDictMvcController中添加
     */
    @Test
    public void testFindByPrimaryKey() throws Exception {
        // 准备测试数据
        DataDict dict1 = TestObjectUtils.buildMockObject(DataDict.class);
        dict1.setPrimaryKey("QUERY_DICT");
        dict1.setSecondaryKey("KEY1");
        dict1.setPrimaryValue("Value 1");
        dataDictService.save(dict1);

        DataDict dict2 = TestObjectUtils.buildMockObject(DataDict.class);
        dict2.setPrimaryKey("QUERY_DICT");
        dict2.setSecondaryKey("KEY2");
        dict2.setPrimaryValue("Value 2");
        dataDictService.save(dict2);
        flushAndClear();

        // 按主键查询 - 使用findByPage接口
        mockMvc.perform(get("/admin/sys/data-dict/findByPage")
                .param("search['EQ_primaryKey']", "QUERY_DICT")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray());
    }
}
