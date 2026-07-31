package com.smsc.management.app.report.utils;

import lombok.Getter;

@Getter
public enum FileExtension {

    CSV("csv"),
    XLSX("xlsx"),
    PDF("pdf");

    private final String value;

    FileExtension(String value) {
        this.value = value;
    }

    public String getName() {
        return this.name();
    }


    public static FileExtension fromValue(String value) {
        for (FileExtension extension : FileExtension.values()) {
            if (extension.getValue().equalsIgnoreCase(value)) {
                return extension;
            }
        }
        throw new IllegalArgumentException("No enum constant for value: " + value);
    }
}
