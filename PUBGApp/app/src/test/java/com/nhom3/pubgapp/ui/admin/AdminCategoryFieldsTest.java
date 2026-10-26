package com.nhom3.pubgapp.ui.admin;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import com.nhom3.pubgapp.viewmodel.AdminContentViewModel;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

// Tests for condition fields validation: non-empty, case-insensitive uniqueness, 30 max items, 50 chars max.
public class AdminCategoryFieldsTest {

    @Test
    public void validateConditionFields_allowsValidList() {
        List<String> fields = new ArrayList<String>();
        fields.add("Dung lượng pin");
        fields.add("Bảo hành");
        fields.add("Màu sắc");

        String error = AdminContentViewModel.validateConditionFields(fields);
        assertNull(error);
    }

    @Test
    public void validateConditionFields_allowsNullOrEmptyList() {
        assertNull(AdminContentViewModel.validateConditionFields(null));
        assertNull(AdminContentViewModel.validateConditionFields(new ArrayList<String>()));
    }

    @Test
    public void validateConditionFields_rejectsEmptyOrBlankName() {
        List<String> withNull = new ArrayList<String>();
        withNull.add(null);
        assertNotNull(AdminContentViewModel.validateConditionFields(withNull));

        List<String> withEmpty = new ArrayList<String>();
        withEmpty.add("");
        assertNotNull(AdminContentViewModel.validateConditionFields(withEmpty));

        List<String> withWhitespace = new ArrayList<String>();
        withWhitespace.add("   ");
        assertNotNull(AdminContentViewModel.validateConditionFields(withWhitespace));
    }

    @Test
    public void validateConditionFields_rejectsNameLongerThan50Chars() {
        List<String> list = new ArrayList<String>();
        StringBuilder sb50 = new StringBuilder();
        for (int i = 0; i < 50; i++) {
            sb50.append('a');
        }
        list.add(sb50.toString());
        assertNull(AdminContentViewModel.validateConditionFields(list));

        list.clear();
        StringBuilder sb51 = new StringBuilder();
        for (int i = 0; i < 51; i++) {
            sb51.append('a');
        }
        list.add(sb51.toString());
        assertEquals("Tên thuộc tính tối đa 50 ký tự.", AdminContentViewModel.validateConditionFields(list));
    }

    @Test
    public void validateConditionFields_rejectsDuplicateNamesCaseInsensitive() {
        List<String> list = new ArrayList<String>();
        list.add("Bảo hành");
        list.add("bảo hành");
        assertEquals("Tên thuộc tính không được trùng lặp.",
                AdminContentViewModel.validateConditionFields(list));

        list.clear();
        list.add("Dung lượng pin");
        list.add("  DUNG LƯỢNG PIN  ");
        assertEquals("Tên thuộc tính không được trùng lặp.",
                AdminContentViewModel.validateConditionFields(list));
    }

    @Test
    public void validateConditionFields_rejectsMoreThan30Items() {
        List<String> list = new ArrayList<String>();
        for (int i = 1; i <= 30; i++) {
            list.add("Thuộc tính " + i);
        }
        assertNull(AdminContentViewModel.validateConditionFields(list));

        list.add("Thuộc tính 31");
        assertEquals("Danh mục tối đa 30 thuộc tính tình trạng.",
                AdminContentViewModel.validateConditionFields(list));
    }
}
