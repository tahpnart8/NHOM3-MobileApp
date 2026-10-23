package com.nhom3.pubgapp;

import com.nhom3.pubgapp.model.Category;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CategoryTest {

    @Test
    public void testIsRoot() {
        Category rootCategory = new Category();
        rootCategory.setName("Điện tử");
        rootCategory.setParentId(null);
        assertTrue(rootCategory.isRoot());

        rootCategory.setParentId("");
        assertTrue(rootCategory.isRoot());

        Category subCategory = new Category();
        subCategory.setName("Điện thoại");
        subCategory.setParentId("root_123");
        assertFalse(subCategory.isRoot());
    }

    @Test
    public void testHasConditionField() {
        Category category = new Category();
        assertFalse(category.hasConditionField("Dung lượng pin"));

        List<String> fields = new ArrayList<String>();
        fields.add("Dung lượng pin");
        fields.add("Bảo hành");
        category.setConditionFields(fields);

        assertTrue(category.hasConditionField("Dung lượng pin"));
        assertTrue(category.hasConditionField("Bảo hành"));
        assertFalse(category.hasConditionField("Màu sắc"));
    }
}
