package com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.missiontemplate;

public enum UpgradeToTier {

    B("B"),       // C 티어 → B 티어 달성 목표
    A("A"),       // B 티어 → A 티어 달성 목표
    S("S"),       // A 티어 → S 티어 달성 목표
    S_PLUS("S+"); // S 티어 → S+ 달성 목표

    private final String dbValue;

    UpgradeToTier(String dbValue) {
        this.dbValue = dbValue;
    }

    public String getDbValue() {
        return dbValue;
    }

    public static UpgradeToTier fromDbValue(String value) {
        for (UpgradeToTier t : values()) {
            if (t.dbValue.equals(value)) return t;
        }
        throw new IllegalArgumentException("알 수 없는 UpgradeToTier 값: " + value);
    }
}
