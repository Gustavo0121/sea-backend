package com.sea.backend.config;

import org.hibernate.dialect.identity.IdentityColumnSupportImpl;

public class SQLiteIdentityColumnSupport extends IdentityColumnSupportImpl {

    @Override
    public boolean supportsIdentityColumns() {
        return true;
    }

    @Override
    public String getIdentitySelectString(String table, String column, int type) {
        return "select last_insert_rowid()";
    }

    @Override
    public boolean hasDataTypeInIdentityColumn() {
        // The column type must be the single word "integer" (not "integer integer") for
        // SQLite to treat the primary key as an alias for the rowid, which is what makes
        // last_insert_rowid() actually populate the id column on insert.
        return false;
    }

    @Override
    public String getIdentityColumnString(int type) {
        return "integer";
    }
}
