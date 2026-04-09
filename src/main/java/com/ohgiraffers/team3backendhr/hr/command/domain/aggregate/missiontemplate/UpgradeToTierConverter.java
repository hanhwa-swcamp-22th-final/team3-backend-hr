package com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.missiontemplate;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class UpgradeToTierConverter implements AttributeConverter<UpgradeToTier, String> {

    @Override
    public String convertToDatabaseColumn(UpgradeToTier attribute) {
        return attribute == null ? null : attribute.getDbValue();
    }

    @Override
    public UpgradeToTier convertToEntityAttribute(String dbData) {
        return dbData == null ? null : UpgradeToTier.fromDbValue(dbData);
    }
}
