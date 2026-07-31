package com.smsc.management.app.dnd.utils;

import com.paicbd.smsc.utils.DndType;
import com.smsc.management.app.dnd.model.entity.DndEntryList;

public class TestFileGenerator {

    public static String buildDndJson(String name, DndType type, String value) {
        return String.format("""
            {
              "name": "%s",
              "dnd_type": "%s",
              "dnd_value": "%s"
            }
        """, name, type.name(), value);
    }

    public static DndEntryList createDndName(DndType type, String value, String name) {
        DndEntryList dndEntryList = new DndEntryList();
        dndEntryList.setId(1);
        dndEntryList.setDndType(type);
        dndEntryList.setDndValue(value);
        dndEntryList.setName(name);
        return dndEntryList;
    }
}