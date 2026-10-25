package com.nhom3.pubgapp.ui.admin;

import com.nhom3.pubgapp.model.Category;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

// Rules for Category name and hierarchy validation, and tree ordering.
public final class AdminCategoryRules {

    public static final int MAX_NAME_LENGTH = 100;

    private AdminCategoryRules() {
    }

    public static boolean isValidName(String name) {
        if (name == null) {
            return false;
        }
        String trimmed = name.trim();
        return !trimmed.isEmpty() && trimmed.length() <= MAX_NAME_LENGTH;
    }

    public static boolean hasChildren(Category category, List<Category> allCategories) {
        if (category == null || category.getId() == null || allCategories == null) {
            return false;
        }
        String id = category.getId();
        for (Category candidate : allCategories) {
            if (id.equals(candidate.getParentId())) {
                return true;
            }
        }
        return false;
    }

    public static boolean isValidParent(Category category, List<Category> allCategories) {
        if (category == null) {
            return false;
        }
        String parentId = category.getParentId();
        if (parentId == null || parentId.trim().isEmpty()) {
            return true;
        }
        if (category.getId() != null && category.getId().equals(parentId)) {
            return false;
        }
        if (allCategories == null) {
            return false;
        }
        // If this category already has child categories, it cannot become a child of another.
        if (hasChildren(category, allCategories)) {
            return false;
        }
        for (Category candidate : allCategories) {
            if (candidate.getId() != null && candidate.getId().equals(parentId)) {
                return candidate.isRoot();
            }
        }
        return false;
    }

    public static List<Category> buildTreeList(List<Category> allCategories) {
        if (allCategories == null || allCategories.isEmpty()) {
            return new ArrayList<Category>();
        }
        List<Category> roots = new ArrayList<Category>();
        List<Category> children = new ArrayList<Category>();

        for (Category cat : allCategories) {
            if (cat.isRoot()) {
                roots.add(cat);
            } else {
                children.add(cat);
            }
        }

        Collections.sort(roots, new Comparator<Category>() {
            @Override
            public int compare(Category o1, Category o2) {
                String n1 = o1.getName() != null ? o1.getName() : "";
                String n2 = o2.getName() != null ? o2.getName() : "";
                return n1.compareToIgnoreCase(n2);
            }
        });

        Collections.sort(children, new Comparator<Category>() {
            @Override
            public int compare(Category o1, Category o2) {
                String n1 = o1.getName() != null ? o1.getName() : "";
                String n2 = o2.getName() != null ? o2.getName() : "";
                return n1.compareToIgnoreCase(n2);
            }
        });

        List<Category> tree = new ArrayList<Category>();
        for (Category root : roots) {
            tree.add(root);
            String rootId = root.getId();
            if (rootId != null) {
                for (Category child : children) {
                    if (rootId.equals(child.getParentId())) {
                        tree.add(child);
                    }
                }
            }
        }

        for (Category child : children) {
            if (!tree.contains(child)) {
                tree.add(child);
            }
        }

        return tree;
    }
}
