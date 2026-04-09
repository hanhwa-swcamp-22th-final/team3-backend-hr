package com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.attachment;

public enum FileType {
    PDF, JPG, PNG, XLSX, MP4, WAV, MP3, GENERAL;

    public static FileType fromExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) return GENERAL;
        String ext = fileName.substring(fileName.lastIndexOf(".") + 1).toUpperCase();
        try {
            return FileType.valueOf(ext);
        } catch (IllegalArgumentException e) {
            return GENERAL;
        }
    }
}
