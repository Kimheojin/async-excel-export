package com.heojin.async_excel_export.status.service;

import com.heojin.async_excel_export.status.entity.Status;

final class DataSeedValues {

    private static final String[] PRODUCT_NAMES = {
            "스키 리프트권",
            "워터파크 종일권",
            "호텔 숙박권",
            "렌탈 패키지",
            "조식 이용권"
    };

    private static final String[] CATEGORIES = {
            "스키",
            "워터파크",
            "숙박",
            "기타"
    };

    private static final Status[] STATUSES = Status.values();

    private DataSeedValues() {
    }

    static String productName(long sequence) {
        return PRODUCT_NAMES[index(sequence, PRODUCT_NAMES.length)];
    }

    static String category(long sequence) {
        return CATEGORIES[index(sequence, CATEGORIES.length)];
    }

    static int amount(long sequence) {
        return 10_000 + (int) (sequence % 990) * 100;
    }

    static String statusName(long sequence) {
        return STATUSES[index(sequence, STATUSES.length)].name();
    }

    private static int index(long sequence, int size) {
        return (int) (sequence % size);
    }
}
