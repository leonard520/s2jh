package lab.s2jh.auth.web.mvc.test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;

import lab.s2jh.auth.entity.User;
import lab.s2jh.auth.service.UserService;
import lab.s2jh.core.test.SpringMvcTestCase;
import lab.s2jh.core.test.TestObjectUtils;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

/**
 * UserMvcController集成测试
 * 
 * 测试用户管理控制器的CRUD操作和分页查询功能
 */
public class UserMvcControllerTest extends SpringMvcTestCase {

    @Autowired
    private UserService userService;

    /**
     * 测试分页查询接口
     * GET /admin/auth/user/findByPage
     * 
     * 期望返回Spring Data Page格式的JSON:
     * { "content": [], "totalElements": n, "totalPages": n, ... }
     */
    @Test
    public void testFindByPage() throws Exception {
        // 准备测试数据
        User user = TestObjectUtils.buildMockObject(User.class);
        user.setSigninid("testuser001");
        user.setEmail("test001@example.com");
        userService.save(user);
        flushAndClear();

        // 执行请求并验证
        mockMvc.perform(get("/admin/auth/user/findByPage")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.content").isArray())
            .andExpect(jsonPath("$.totalElements").exists());
    }

    /**
     * 测试分页查询带过滤条件
     * GET /admin/auth/user/findByPage?search['CN_signinid']=test
     */
    @Test
    public void testFindByPageWithFilter() throws Exception {
        // 准备测试数据
        User user1 = TestObjectUtils.buildMockObject(User.class);
        user1.setSigninid("findtest_user1");
        user1.setEmail("findtest1@example.com");
        userService.save(user1);

        User user2 = TestObjectUtils.buildMockObject(User.class);
        user2.setSigninid("other_user2");
        user2.setEmail("other2@example.com");
        userService.save(user2);
        flushAndClear();

        // 使用过滤条件查询
        mockMvc.perform(get("/admin/auth/user/findByPage")
                .param("search['CN_signinid']", "findtest")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray());
    }

    /**
     * 测试新增用户接口
     * POST /admin/auth/user/doSave
     * 
     * 期望返回OperationResult格式的JSON:
     * { "type": "success", "message": "...", "data": {...} }
     */
    @Test
    public void testDoSaveCreate() throws Exception {
        mockMvc.perform(post("/admin/auth/user/doSave")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("signinid", "newuser123")
                .param("email", "newuser@example.com")
                .param("nick", "New User")
                .param("newpassword", "password123"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.type").value("success"));
    }

    /**
     * 测试更新用户接口
     * POST /admin/auth/user/doSave (with existing id)
     */
    @Test
    public void testDoSaveUpdate() throws Exception {
        // 准备测试数据
        User user = TestObjectUtils.buildMockObject(User.class);
        user.setSigninid("updateuser001");
        user.setEmail("update001@example.com");
        user.setNick("Original Nick");
        userService.save(user);
        flushAndClear();

        Long userId = user.getId();

        // 更新用户
        mockMvc.perform(post("/admin/auth/user/doSave")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("id", userId.toString())
                .param("signinid", "updateuser001")
                .param("email", "update001@example.com")
                .param("nick", "Updated Nick"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.type").value("success"));
    }

    /**
     * 测试删除用户接口
     * POST /admin/auth/user/doDelete?ids=1,2,3
     */
    @Test
    public void testDoDelete() throws Exception {
        // 准备测试数据
        User user = TestObjectUtils.buildMockObject(User.class);
        user.setSigninid("deleteuser001");
        user.setEmail("delete001@example.com");
        userService.save(user);
        flushAndClear();

        Long userId = user.getId();

        // 删除用户
        mockMvc.perform(post("/admin/auth/user/doDelete")
                .param("ids", userId.toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.type").value("success"));
    }

    /**
     * 测试查看用户详情页面
     * GET /admin/auth/user/edit?id=xxx
     */
    @Test
    public void testEditView() throws Exception {
        // 准备测试数据
        User user = TestObjectUtils.buildMockObject(User.class);
        user.setSigninid("viewuser001");
        user.setEmail("view001@example.com");
        userService.save(user);
        flushAndClear();

        // 请求编辑页面
        mockMvc.perform(get("/admin/auth/user/edit")
                .param("id", user.getId().toString()))
            .andExpect(status().isOk())
            .andExpect(view().name("admin/auth/user-inputBasic"));
    }
}
