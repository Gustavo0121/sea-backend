package com.sea.backend.config;

import org.hibernate.boot.Metadata;
import org.hibernate.boot.model.relational.SqlStringGenerationContext;
import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.unique.DefaultUniqueDelegate;
import org.hibernate.mapping.Table;
import org.hibernate.mapping.UniqueKey;

import java.util.Iterator;

/**
 * SQLite has no ALTER TABLE ... ADD CONSTRAINT support, so unique keys must be
 * declared inline in the CREATE TABLE statement instead of via a separate
 * alter-table command (which is what Hibernate's DefaultUniqueDelegate emits).
 */
public class SQLiteUniqueDelegate extends DefaultUniqueDelegate {

    public SQLiteUniqueDelegate(Dialect dialect) {
        super(dialect);
    }

    @Override
    public String getTableCreationUniqueConstraintsFragment(Table table, SqlStringGenerationContext context) {
        StringBuilder fragment = new StringBuilder();
        Iterator<UniqueKey> uniqueKeys = table.getUniqueKeyIterator();
        while (uniqueKeys.hasNext()) {
            fragment.append(", ").append(uniqueConstraintSql(uniqueKeys.next()));
        }
        return fragment.toString();
    }

    @Override
    public String getAlterTableToAddUniqueKeyCommand(UniqueKey uniqueKey, Metadata metadata,
                                                       SqlStringGenerationContext context) {
        return "";
    }

    @Override
    public String getAlterTableToDropUniqueKeyCommand(UniqueKey uniqueKey, Metadata metadata,
                                                        SqlStringGenerationContext context) {
        return "";
    }
}
