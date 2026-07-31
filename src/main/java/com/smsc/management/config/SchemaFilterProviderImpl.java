package com.smsc.management.config;

import org.hibernate.boot.model.relational.Namespace;
import org.hibernate.boot.model.relational.Sequence;
import org.hibernate.mapping.Table;
import org.hibernate.tool.schema.spi.SchemaFilter;
import org.hibernate.tool.schema.spi.SchemaFilterProvider;

import java.util.Set;

/**
 * Excludes specific tables from Hibernate's ddl-auto schema management.
 * Prevents Hibernate from running ALTER TABLE / ADD COLUMN / ADD CONSTRAINT DDL
 * on excluded tables at application startup, while all other entities remain managed.
 * To exclude additional tables in the future, add their names to EXCLUDED_TABLES.
 */
public class SchemaFilterProviderImpl implements SchemaFilterProvider {

    private static final Set<String> EXCLUDED_TABLES = Set.of(
            "broadcast_devices"
    );

    private static final SchemaFilter FILTER = new SchemaFilter() {
        @Override
        public boolean includeNamespace(Namespace namespace) {
            return true;
        }

        @Override
        public boolean includeTable(Table table) {
            return !EXCLUDED_TABLES.contains(table.getName());
        }

        @Override
        public boolean includeSequence(Sequence sequence) {
            return true;
        }
    };

    @Override
    public SchemaFilter getCreateFilter() {
        return FILTER;
    }

    @Override
    public SchemaFilter getDropFilter() {
        return FILTER;
    }

    @Override
    public SchemaFilter getTruncatorFilter() {
        return FILTER;
    }

    @Override
    public SchemaFilter getMigrateFilter() {
        return FILTER;
    }

    @Override
    public SchemaFilter getValidateFilter() {
        return FILTER;
    }
}
