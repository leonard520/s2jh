package lab.s2jh.sys.web.mvc.test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;

import lab.s2jh.core.test.SpringMvcTestCase;
import lab.s2jh.core.test.TestObjectUtils;
import lab.s2jh.sys.entity.Menu;
import lab.s2jh.sys.service.MenuService;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

/**
 * MenuMvcController集成测试
 * 
 * 测试菜单管理控制器的CRUD操作和分页查询功能
 */
public class MenuMvcControllerTest extends SpringMvcTestCase {

    @Autowired
    private MenuService menuService;

    /**
     * 测试分页查询接口
     * GET /admin/sys/menu/findByPage
     */
    @Test
    public void testFindByPage() throws Exception {
        // 准备测试数据
        Menu menu = TestObjectUtils.buildMockObject(Menu.class);
        menu.setCode("TEST_MENU_001");
        menu.setTitle("Test Menu");
        menu.setUrl("/test/url");
        menuService.save(menu);
        flushAndClear();

        // 执行请求并验证
        mockMvc.perform(get("/admin/sys/menu/findByPage")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.content").isArray())
            .andExpect(jsonPath("$.totalElements").exists());
    }

    /**
     * 测试新增菜单接口
     * POST /admin/sys/menu/doSave
     */
    @Test
    public void testDoSaveCreate() throws Exception {
        mockMvc.perform(post("/admin/sys/menu/doSave")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("code", "NEW_MENU_001")
                .param("title", "New Test Menu")
                .param("url", "/admin/test/new")
                .param("orderRank", "100"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.type").value("success"));
    }

    /**
     * 测试更新菜单接口
     * POST /admin/sys/menu/doSave (with existing id)
     */
    @Test
    public void testDoSaveUpdate() throws Exception {
        // 准备测试数据
        Menu menu = TestObjectUtils.buildMockObject(Menu.class);
        menu.setCode("UPDATE_MENU_001");
        menu.setTitle("Original Title");
        menu.setUrl("/admin/original");
        menuService.save(menu);
        flushAndClear();

        String menuId = menu.getId();

        // 更新菜单
        mockMvc.perform(post("/admin/sys/menu/doSave")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("id", menuId)
                .param("code", "UPDATE_MENU_001")
                .param("title", "Updated Title")
                .param("url", "/admin/updated"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.type").value("success"));
    }

    /**
     * 测试删除菜单接口
     * POST /admin/sys/menu/doDelete?ids=1,2,3
     */
    @Test
    public void testDoDelete() throws Exception {
        // 准备测试数据
        Menu menu = TestObjectUtils.buildMockObject(Menu.class);
        menu.setCode("DELETE_MENU_001");
        menu.setTitle("To Delete");
        menu.setUrl("/admin/delete");
        menuService.save(menu);
        flushAndClear();

        String menuId = menu.getId();

        // 删除菜单
        mockMvc.perform(post("/admin/sys/menu/doDelete")
                .param("ids", menuId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.type").value("success"));
    }

    /**
     * 测试菜单树形数据接口 - 使用list接口代替
     * GET /admin/sys/menu/list
     */
    @Test
    public void testTree() throws Exception {
        // 准备测试数据 - 父菜单
        Menu parentMenu = TestObjectUtils.buildMockObject(Menu.class);
        parentMenu.setCode("TREE_PARENT");
        parentMenu.setTitle("Parent Menu");
        parentMenu.setUrl("/admin/parent");
        menuService.save(parentMenu);

        // 子菜单
        Menu childMenu = TestObjectUtils.buildMockObject(Menu.class);
        childMenu.setCode("TREE_CHILD");
        childMenu.setTitle("Child Menu");
        childMenu.setUrl("/admin/child");
        childMenu.setParent(parentMenu);
        menuService.save(childMenu);
        flushAndClear();

        // 获取菜单树 - 使用list端点
        mockMvc.perform(get("/admin/sys/menu/list")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray());
    }

    /**
     * 测试移动菜单接口 - 暂未实现
     * POST /admin/sys/menu/doMove
     * 
     * TODO: 实现菜单移动功能后启用此测试
     */
    // @Test
    public void testDoMove() throws Exception {
        // 准备测试数据
        Menu targetParent = TestObjectUtils.buildMockObject(Menu.class);
        targetParent.setCode("MOVE_TARGET");
        targetParent.setTitle("Target Parent");
        targetParent.setUrl("/admin/target");
        menuService.save(targetParent);

        Menu toMove = TestObjectUtils.buildMockObject(Menu.class);
        toMove.setCode("MOVE_CHILD");
        toMove.setTitle("To Move");
        toMove.setUrl("/admin/move");
        menuService.save(toMove);
        flushAndClear();

        // 移动菜单
        mockMvc.perform(post("/admin/sys/menu/doMove")
                .param("sourceId", toMove.getId())
                .param("targetId", targetParent.getId())
                .param("moveType", "inner"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.type").value("success"));
    }
}
