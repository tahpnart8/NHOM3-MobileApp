package com.nhom3.pubgapp.ui.admin;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.nhom3.pubgapp.model.Category;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class AdminCategoryRulesTest {

    @Test
    public void isValidName_allowsValidNames() {
        assertTrue(AdminCategoryRules.isValidName("Điện thoại"));
        assertTrue(AdminCategoryRules.isValidName("  Laptop  "));
        StringBuilder sb100 = new StringBuilder();
        for (int i = 0; i < 100; i++) {
            sb100.append('a');
        }
        assertTrue(AdminCategoryRules.isValidName(sb100.toString()));
    }

    @Test
    public void isValidName_rejectsNullOrBlankOrTooLong() {
        assertFalse(AdminCategoryRules.isValidName(null));
        assertFalse(AdminCategoryRules.isValidName(""));
        assertFalse(AdminCategoryRules.isValidName("   "));

        StringBuilder sb101 = new StringBuilder();
        for (int i = 0; i < 101; i++) {
            sb101.append('a');
        }
        assertFalse(AdminCategoryRules.isValidName(sb101.toString()));
    }

    @Test
    public void isValidParent_allowsEmptyParentForRoot() {
        Category cat = new Category();
        cat.setParentId(null);
        assertTrue(AdminCategoryRules.isValidParent(cat, new ArrayList<Category>()));

        cat.setParentId("");
        assertTrue(AdminCategoryRules.isValidParent(cat, new ArrayList<Category>()));

        cat.setParentId("   ");
        assertTrue(AdminCategoryRules.isValidParent(cat, new ArrayList<Category>()));
    }

    @Test
    public void isValidParent_allowsOnlyRootCategoryAsParent() {
        List<Category> all = new ArrayList<Category>();

        Category root = new Category();
        root.setId("root1");
        root.setName("Điện tử");
        root.setParentId("");
        all.add(root);

        Category child = new Category();
        child.setId("child1");
        child.setName("Điện thoại");
        child.setParentId("root1");
        all.add(child);

        Category newSub = new Category();
        newSub.setName("iPhone");
        newSub.setParentId("root1");
        assertTrue(AdminCategoryRules.isValidParent(newSub, all));

        // Sub of child must be rejected (max 2 levels)
        Category grandChild = new Category();
        grandChild.setName("Phụ kiện");
        grandChild.setParentId("child1");
        assertFalse(AdminCategoryRules.isValidParent(grandChild, all));

        // Unknown parent rejected
        Category unknown = new Category();
        unknown.setName("Lạ");
        unknown.setParentId("unknown_id");
        assertFalse(AdminCategoryRules.isValidParent(unknown, all));

        // Self as parent rejected
        Category selfParent = new Category();
        selfParent.setId("self1");
        selfParent.setParentId("self1");
        assertFalse(AdminCategoryRules.isValidParent(selfParent, all));
    }

    @Test
    public void isValidParent_rejectsCategoryWithChildrenFromBecomingChild() {
        List<Category> all = new ArrayList<Category>();

        Category root1 = new Category();
        root1.setId("root1");
        root1.setName("Điện tử");
        root1.setParentId("");
        all.add(root1);

        Category root2 = new Category();
        root2.setId("root2");
        root2.setName("Gia dụng");
        root2.setParentId("");
        all.add(root2);

        Category child = new Category();
        child.setId("child1");
        child.setName("Tivi");
        child.setParentId("root1");
        all.add(child);

        assertTrue(AdminCategoryRules.hasChildren(root1, all));
        assertFalse(AdminCategoryRules.hasChildren(root2, all));

        // root1 already has child1, so it cannot be made a child of root2
        root1.setParentId("root2");
        assertFalse(AdminCategoryRules.isValidParent(root1, all));
    }

    @Test
    public void buildTreeList_ordersRootsAndChildren() {
        List<Category> all = new ArrayList<Category>();

        Category c1 = new Category();
        c1.setId("c1");
        c1.setName("iPhone");
        c1.setParentId("r1");
        all.add(c1);

        Category r2 = new Category();
        r2.setId("r2");
        r2.setName("Gia dụng");
        r2.setParentId("");
        all.add(r2);

        Category r1 = new Category();
        r1.setId("r1");
        r1.setName("Điện tử");
        r1.setParentId("");
        all.add(r1);

        Category c2 = new Category();
        c2.setId("c2");
        c2.setName("Android");
        c2.setParentId("r1");
        all.add(c2);

        List<Category> tree = AdminCategoryRules.buildTreeList(all);
        assertEquals(4, tree.size());
        // Gia dụng (G) before Điện tử (Đ) in alphabetical order
        assertEquals("r2", tree.get(0).getId());
        assertEquals("r1", tree.get(1).getId());
        // Android before iPhone under Điện tử
        assertEquals("c2", tree.get(2).getId());
        assertEquals("c1", tree.get(3).getId());
    }
}
