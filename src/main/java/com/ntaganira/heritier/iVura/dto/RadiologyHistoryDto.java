package com.ntaganira.heritier.iVura.dto;

import com.ntaganira.heritier.iVura.entity.RadiologyOrder;
import com.ntaganira.heritier.iVura.entity.RadiologyOrderItem;
import com.ntaganira.heritier.iVura.entity.RadiologyReport;
import lombok.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * <pre>
 * - Project   : iVura - Hospital Management System
 * - Package   : com.ntaganira.heritier.iVura.dto
 * - File      : RadiologyHistoryDto.java
 * - Desc      : Radiology history DTO grouping an imaging order with its
 *               items and their reports for the patient history view.
 * </pre>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RadiologyHistoryDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private RadiologyOrder order;

    private List<Item> items = new ArrayList<>();

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Item implements Serializable {

        private static final long serialVersionUID = 1L;

        private RadiologyOrderItem item;

        private RadiologyReport report;
    }
}
