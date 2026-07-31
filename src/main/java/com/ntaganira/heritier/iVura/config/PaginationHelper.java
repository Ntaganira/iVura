package com.ntaganira.heritier.iVura.config;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * <pre>
 * - Project   : iVura - Hospital Management System
 * - Package   : com.ntaganira.heritier.iVura.config
 * - File      : PaginationHelper.java
 * - Date      : 2026. 07. 31.
 * - User      : Hntaganira
 * - Desc      : Computes a window of page numbers for pagination controls
 * </pre>
 */
@Component("paginationHelper")
public class PaginationHelper {

    public List<Integer> window(int current, int total, int span) {
        List<Integer> pages = new ArrayList<>();
        if (total <= 0) {
            return pages;
        }
        int start = Math.max(0, current - span);
        int end = Math.min(total - 1, current + span);
        for (int i = start; i <= end; i++) {
            pages.add(i);
        }
        return pages;
    }
}
