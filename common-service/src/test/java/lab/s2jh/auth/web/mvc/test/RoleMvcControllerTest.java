package lab.s2jh.auth.web.mvc.test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;

import lab.s2jh.auth.entity.Role;
import lab.s2jh.auth.service.RoleService;
import lab.s2jh.core.test.SpringMvcTestCase;
import lab.s2jh.core.test.TestObjectUtils;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

/**
 * RoleMvcController集成测试
 * 
 * 测试角色管理控制器的CRUD操作和分页查询功能
 */
public class RoleMvcControllerTest extends SpringMvcTestCase {

    @Autowired
    private RoleService roleService;

    /**
     * 测试分页查询接口
     * GET /admin/auth/role/findByPage
     */
    @Test
    public void testFindByPage() throws Exception {
        // 准备测试数据
        Role role = TestObjectUtils.buildMockObject(Role.class);
        role.setCode("ROLE_TEST_001");
        role.setTitle("Test Role");
        roleService.save(role);
        flushAndClear();

        // 执行请求并验证
        mockMvc.perform(get("/admin/auth/role/findByPage")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.content").isArray())
            .andExpect(jsonPath("$.totalElements").exists());
    }

    /**
     * 测试新增角色接口
     * POST /admin/auth/role/doSave
     */
    @Test
    public void testDoSaveCreate() throws Exception {
        mockMvc.perform(post("/admin/auth/role/doSave")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("code", "ROLE_NEW_001")
                .param("title", "New Test Role")
                .param("description", "This is a test role"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.type").value("success"));
    }

    /**
     * 测试更新角色接口
     * POST /admin/auth/role/doSave (with existing id)
     */
    @Test
    public void testDoSaveUpdate() throws Exception {
        // 准备测试数据
        Role role = TestObjectUtils.buildMockObject(Role.class);
        role.setCode("ROLE_UPDATE_001");
        role.setTitle("Original Title");
        roleService.save(role);
        flushAndClear();

        String roleId = role.getId();

        // 更新角色
        mockMvc.perform(post("/admin/auth/role/doSave")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("id", roleId)
                .param("code", "ROLE_UPDATE_001")
                .param("title", "Updated Title")
                .param("description", "Updated description"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.type").value("success"));
    }

    /**
     * 测试删除角色接口
     * POST /admin/auth/role/doDelete?ids=1,2,3
     */
    @Test
    public void testDoDelete() throws Exception {
        // 准备测试数据
        Role role = TestObjectUtils.buildMockObject(Role.class);
        role.setCode("ROLE_DELETE_001");
        role.setTitle("To Delete");
        roleService.save(role);
        flushAndClear();

        String roleId = role.getId();

        // 删除角色
        mockMvc.perform(post("/admin/auth/role/doDelete")
                .param("ids", roleId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.type").value("success"));
    }

    /**
     * 测试角色列表数据接口（用于下拉选择等）
     * 使用findByPage接口代替专用的list接口
     * 
     * TODO: 如需专用的list接口，需要在RoleMvcController中添加
     */
    @Test
    public void testList() throws Exception {
        // 准备测试数据
        Role role = TestObjectUtils.buildMockObject(Role.class);
        role.setCode("ROLE_LIST_001");
        role.setTitle("List Test Role");
        roleService.save(role);
        flushAndClear();

        // 获取角色列表 - 使用findByPage端点
        mockMvc.perform(get("/admin/auth/role/findByPage")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray());
    }
}
