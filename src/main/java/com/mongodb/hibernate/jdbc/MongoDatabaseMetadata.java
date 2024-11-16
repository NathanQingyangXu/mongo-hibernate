/*
 * Copyright 2024-present MongoDB, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mongodb.hibernate.jdbc;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;

final class MongoDatabaseMetadata extends AbstractDatabaseMetadata {

    private static final String MONGO_DATABASE_PRODUCT_NAME = "MongoDB";

    private final Connection connection;

    private final String databaseVersionText;
    private final int databaseMajorVersion;
    private final int databaseMinorVersion;

    MongoDatabaseMetadata(
            Connection connection, String databaseVersionText, int databaseMajorVersion, int databaseMinorVersion) {
        this.connection = connection;
        this.databaseVersionText = databaseVersionText;
        this.databaseMajorVersion = databaseMajorVersion;
        this.databaseMinorVersion = databaseMinorVersion;
    }

    // ----------------------------------------------------------------------
    // First, a variety of minor information about the target database.

    @Override
    public String getDatabaseProductName() {
        return MONGO_DATABASE_PRODUCT_NAME;
    }

    @Override
    public String getDatabaseProductVersion() {
        return this.databaseVersionText;
    }

    @Override
    public String getDriverName() {
        return "Embedded JDBC Driver for MongoDB Hibernate Dialect";
    }

    @Override
    public String getDriverVersion() {
        return "1.0";
    }

    @Override
    public int getDriverMajorVersion() {
        return 1;
    }

    @Override
    public int getDriverMinorVersion() {
        return 0;
    }

    @Override
    public boolean storesUpperCaseIdentifiers() {
        return false;
    }

    @Override
    public boolean storesLowerCaseIdentifiers() {
        return false;
    }

    @Override
    public boolean storesMixedCaseIdentifiers() {
        return true;
    }

    @Override
    public boolean supportsMixedCaseQuotedIdentifiers() {
        return true;
    }

    @Override
    public boolean storesUpperCaseQuotedIdentifiers() {
        return false;
    }

    @Override
    public boolean storesLowerCaseQuotedIdentifiers() {
        return false;
    }

    @Override
    public boolean storesMixedCaseQuotedIdentifiers() {
        return true;
    }

    @Override
    public String getSQLKeywords() {
        return "";
    }

    @Override
    public boolean isCatalogAtStart() {
        return true;
    }

    @Override
    public String getCatalogSeparator() {
        return ".";
    }

    @Override
    public boolean supportsSchemasInTableDefinitions() {
        return false;
    }

    @Override
    public boolean supportsCatalogsInTableDefinitions() {
        return false;
    }

    // ----------------------------------------------------------------------
    // The following group of methods exposes various limitations
    // based on the target database with the current driver.
    // Unless otherwise specified, a result of zero means there is no
    // limit, or the limit is not known.

    // ----------------------------------------------------------------------

    @Override
    public boolean dataDefinitionCausesTransactionCommit() {
        return false;
    }

    @Override
    public boolean dataDefinitionIgnoredInTransactions() {
        return false;
    }

    @Override
    public ResultSet getTables(String catalog, String schemaPattern, String tableNamePattern, String types[]) {
        return DummyResultSet.INSTANCE;
    }

    @Override
    public ResultSet getCatalogs() {
        return DummyResultSet.INSTANCE;
    }

    @Override
    public ResultSet getColumns(
            String catalog, String schemaPattern, String tableNamePattern, String columnNamePattern) {
        return DummyResultSet.INSTANCE;
    }

    @Override
    public ResultSet getPrimaryKeys(String catalog, String schema, String table) {
        return DummyResultSet.INSTANCE;
    }

    @Override
    public ResultSet getImportedKeys(String catalog, String schema, String table) {
        return DummyResultSet.INSTANCE;
    }

    @Override
    public ResultSet getCrossReference(
            String parentCatalog,
            String parentSchema,
            String parentTable,
            String foreignCatalog,
            String foreignSchema,
            String foreignTable) {
        return DummyResultSet.INSTANCE;
    }

    @Override
    public ResultSet getTypeInfo() {
        return DummyResultSet.INSTANCE;
    }

    @Override
    public ResultSet getIndexInfo(String catalog, String schema, String table, boolean unique, boolean approximate) {
        return DummyResultSet.INSTANCE;
    }

    @Override
    public boolean supportsResultSetType(int type) {
        return false;
    }

    @Override
    public boolean supportsBatchUpdates() {
        return true;
    }

    @Override
    public Connection getConnection() {
        return this.connection;
    }

    // ------------------- JDBC 3.0 -------------------------

    @Override
    public boolean supportsNamedParameters() {
        return false;
    }

    @Override
    public boolean supportsGetGeneratedKeys() {
        return false;
    }

    @Override
    public int getDatabaseMajorVersion() {
        return this.databaseMajorVersion;
    }

    @Override
    public int getDatabaseMinorVersion() {
        return this.databaseMinorVersion;
    }

    @Override
    public int getJDBCMajorVersion() {
        return 4;
    }

    @Override
    public int getJDBCMinorVersion() {
        return 3;
    }

    @Override
    public int getSQLStateType() {
        return DatabaseMetaData.sqlStateSQL;
    }

    // ------------------------- JDBC 4.0 -----------------------------------

    @Override
    public ResultSet getSchemas(String catalog, String schemaPattern) {
        return DummyResultSet.INSTANCE;
    }

    // --------------------------JDBC 4.1 -----------------------------

    // --------------------------JDBC 4.2 -----------------------------

    @Override
    public boolean supportsRefCursors() {
        return false;
    }

    // JDBC 4.3
}
