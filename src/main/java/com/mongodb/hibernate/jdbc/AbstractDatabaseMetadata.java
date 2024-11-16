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

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.RowIdLifetime;

abstract class AbstractDatabaseMetadata implements DatabaseMetaData {

    @Override
    public <T> T unwrap(Class<T> unwrapType) {
        throw new IllegalStateException("Hibernate won't use this method");
    }

    @Override
    public boolean isWrapperFor(Class<?> unwrapType) {
        throw new IllegalStateException("Hibernate won't use this method");
    }

    // ----------------------------------------------------------------------
    // First, a variety of minor information about the target database.

    @Override
    public boolean allProceduresAreCallable() {
        throw new IllegalStateException("Hibernate won't use this method");
    }

    @Override
    public boolean allTablesAreSelectable() {
        throw new IllegalStateException("Hibernate won't use this method");
    }

    @Override
    public String getURL() {
        throw new IllegalStateException("Hibernate won't use this method");
    }

    @Override
    public String getUserName() {
        throw new IllegalStateException("Hibernate won't use this method");
    }

    @Override
    public boolean isReadOnly() {
        throw new IllegalStateException("Hibernate won't use this method");
    }

    @Override
    public boolean nullsAreSortedHigh() {
        throw new IllegalStateException("Hibernate won't use this method");
    }

    @Override
    public boolean nullsAreSortedLow() {
        throw new IllegalStateException("Hibernate won't use this method");
    }

    @Override
    public boolean nullsAreSortedAtStart() {
        throw new IllegalStateException("Hibernate won't use this method");
    }

    @Override
    public boolean nullsAreSortedAtEnd() {
        throw new IllegalStateException("Hibernate won't use this method");
    }

    @Override
    public boolean usesLocalFiles() {
        throw new IllegalStateException("Hibernate won't use this method");
    }

    @Override
    public boolean usesLocalFilePerTable() {
        throw new IllegalStateException("Hibernate won't use this method");
    }

    @Override
    public boolean supportsMixedCaseIdentifiers() {
        throw new IllegalStateException("Hibernate won't use this method");
    }

    @Override
    public String getIdentifierQuoteString() {
        throw new IllegalStateException("Hibernate won't use this method");
    }

    @Override
    public String getNumericFunctions() {
        throw new IllegalStateException("Hibernate won't use this method");
    }

    @Override
    public String getStringFunctions() {
        throw new IllegalStateException("Hibernate won't use this method");
    }

    @Override
    public String getSystemFunctions() {
        throw new IllegalStateException("Hibernate won't use this method");
    }

    @Override
    public String getTimeDateFunctions() {
        throw new IllegalStateException("Hibernate won't use this method");
    }

    @Override
    public String getSearchStringEscape() {
        throw new IllegalStateException("Hibernate won't use this method");
    }

    @Override
    public String getExtraNameCharacters() {
        throw new IllegalStateException("Hibernate won't use this method");
    }

    // --------------------------------------------------------------------
    // Functions describing which features are supported.

    @Override
    public boolean supportsAlterTableWithAddColumn() {
        throw new IllegalStateException("Hibernate won't use this method");
    }

    @Override
    public boolean supportsAlterTableWithDropColumn() {
        throw new IllegalStateException("Hibernate won't use this method");
    }

    @Override
    public boolean supportsColumnAliasing() {
        throw new IllegalStateException("Hibernate won't use this method");
    }

    @Override
    public boolean nullPlusNonNullIsNull() {
        throw new IllegalStateException("Hibernate won't use this method");
    }

    @Override
    public boolean supportsConvert() {
        throw new IllegalStateException("Hibernate won't use this method");
    }

    @Override
    public boolean supportsConvert(int fromType, int toType) {
        throw new IllegalStateException("Hibernate won't use this method");
    }

    @Override
    public boolean supportsTableCorrelationNames() {
        throw new IllegalStateException("Hibernate won't use this method");
    }

    @Override
    public boolean supportsDifferentTableCorrelationNames() {
        throw new IllegalStateException("Hibernate won't use this method");
    }

    @Override
    public boolean supportsExpressionsInOrderBy() {
        throw new IllegalStateException("Hibernate won't use this method");
    }

    @Override
    public boolean supportsOrderByUnrelated() {
        throw new IllegalStateException("Hibernate won't use this method");
    }

    @Override
    public boolean supportsGroupBy() {
        throw new IllegalStateException("Hibernate won't use this method");
    }

    @Override
    public boolean supportsGroupByUnrelated() {
        throw new IllegalStateException("Hibernate won't use this method");
    }

    @Override
    public boolean supportsGroupByBeyondSelect() {
        throw new IllegalStateException("Hibernate won't use this method");
    }

    @Override
    public boolean supportsLikeEscapeClause() {
        throw new IllegalStateException("Hibernate won't use this method");
    }

    @Override
    public boolean supportsMultipleResultSets() {
        throw new IllegalStateException("Hibernate won't use this method");
    }

    @Override
    public boolean supportsMultipleTransactions() {
        throw new IllegalStateException("Hibernate won't use this method");
    }

    @Override
    public boolean supportsNonNullableColumns() {
        throw new IllegalStateException("Hibernate won't use this method");
    }

    @Override
    public boolean supportsMinimumSQLGrammar() {
        throw new IllegalStateException("Hibernate won't use this method");
    }

    @Override
    public boolean supportsCoreSQLGrammar() {
        throw new IllegalStateException("Hibernate won't use this method");
    }

    @Override
    public boolean supportsExtendedSQLGrammar() {
        throw new IllegalStateException("Hibernate won't use this method");
    }

    @Override
    public boolean supportsANSI92EntryLevelSQL() {
        throw new IllegalStateException("Hibernate won't use this method");
    }

    @Override
    public boolean supportsANSI92IntermediateSQL() {
        throw new IllegalStateException("Hibernate won't use this method");
    }

    @Override
    public boolean supportsANSI92FullSQL() {
        throw new IllegalStateException("Hibernate won't use this method");
    }

    @Override
    public boolean supportsIntegrityEnhancementFacility() {
        throw new IllegalStateException("Hibernate won't use this method");
    }

    @Override
    public boolean supportsOuterJoins() {
        throw new IllegalStateException("Hibernate won't use this method");
    }

    @Override
    public boolean supportsFullOuterJoins() {
        throw new IllegalStateException("Hibernate won't use this method");
    }

    @Override
    public boolean supportsLimitedOuterJoins() {
        throw new IllegalStateException("Hibernate won't use this method");
    }

    @Override
    public String getSchemaTerm() {
        throw new IllegalStateException("Hibernate won't use this method");
    }

    @Override
    public String getProcedureTerm() {
        throw new IllegalStateException("Hibernate won't use this method");
    }

    @Override
    public String getCatalogTerm() {
        throw new IllegalStateException("Hibernate won't use this method");
    }

    @Override
    public boolean supportsSchemasInDataManipulation() {
        throw new IllegalStateException("Hibernate won't use this method");
    }

    @Override
    public boolean supportsSchemasInProcedureCalls() {
        throw new IllegalStateException("Hibernate won't use this method");
    }

    @Override
    public boolean supportsSchemasInIndexDefinitions() {
        throw new IllegalStateException("Hibernate won't use this method");
    }

    @Override
    public boolean supportsSchemasInPrivilegeDefinitions() {
        throw new IllegalStateException("Hibernate won't use this method");
    }

    @Override
    public boolean supportsCatalogsInDataManipulation() {
        throw new IllegalStateException("Hibernate won't use this method");
    }

    @Override
    public boolean supportsCatalogsInProcedureCalls() {
        throw new IllegalStateException("Hibernate won't use this method");
    }

    @Override
    public boolean supportsCatalogsInIndexDefinitions() {
        throw new IllegalStateException("Hibernate won't use this method");
    }

    @Override
    public boolean supportsCatalogsInPrivilegeDefinitions() {
        throw new IllegalStateException("Hibernate won't use this method");
    }

    @Override
    public boolean supportsPositionedDelete() {
        throw new IllegalStateException("Hibernate won't use this method");
    }

    @Override
    public boolean supportsPositionedUpdate() {
        throw new IllegalStateException("Hibernate won't use this method");
    }

    @Override
    public boolean supportsSelectForUpdate() {
        throw new IllegalStateException("Hibernate won't use this method");
    }

    @Override
    public boolean supportsStoredProcedures() {
        throw new IllegalStateException("Hibernate won't use this method");
    }

    @Override
    public boolean supportsSubqueriesInComparisons() {
        throw new IllegalStateException("Hibernate won't use this method");
    }

    @Override
    public boolean supportsSubqueriesInExists() {
        throw new IllegalStateException("Hibernate won't use this method");
    }

    @Override
    public boolean supportsSubqueriesInIns() {
        throw new IllegalStateException("Hibernate won't use this method");
    }

    @Override
    public boolean supportsSubqueriesInQuantifieds() {
        throw new IllegalStateException("Hibernate won't use this method");
    }

    @Override
    public boolean supportsCorrelatedSubqueries() {
        throw new IllegalStateException("Hibernate won't use this method");
    }

    @Override
    public boolean supportsUnion() {
        throw new IllegalStateException("Hibernate won't use this method");
    }

    @Override
    public boolean supportsUnionAll() {
        throw new IllegalStateException("Hibernate won't use this method");
    }

    @Override
    public boolean supportsOpenCursorsAcrossCommit() {
        throw new IllegalStateException("Hibernate won't use this method");
    }

    @Override
    public boolean supportsOpenCursorsAcrossRollback() {
        throw new IllegalStateException("Hibernate won't use this method");
    }

    @Override
    public boolean supportsOpenStatementsAcrossCommit() {
        throw new IllegalStateException("Hibernate won't use this method");
    }

    @Override
    public boolean supportsOpenStatementsAcrossRollback() {
        throw new IllegalStateException("Hibernate won't use this method");
    }

    // ----------------------------------------------------------------------
    // The following group of methods exposes various limitations
    // based on the target database with the current driver.
    // Unless otherwise specified, a result of zero means there is no
    // limit, or the limit is not known.

    @Override
    public int getMaxBinaryLiteralLength() {
        throw new IllegalStateException("Hibernate won't use this method");
    }

    @Override
    public int getMaxCharLiteralLength() {
        throw new IllegalStateException("Hibernate won't use this method");
    }

    @Override
    public int getMaxColumnNameLength() {
        throw new IllegalStateException("Hibernate won't use this method");
    }

    @Override
    public int getMaxColumnsInGroupBy() {
        throw new IllegalStateException("Hibernate won't use this method");
    }

    @Override
    public int getMaxColumnsInIndex() {
        throw new IllegalStateException("Hibernate won't use this method");
    }

    @Override
    public int getMaxColumnsInOrderBy() {
        throw new IllegalStateException("Hibernate won't use this method");
    }

    @Override
    public int getMaxColumnsInSelect() {
        throw new IllegalStateException("Hibernate won't use this method");
    }

    @Override
    public int getMaxColumnsInTable() {
        throw new IllegalStateException("Hibernate won't use this method");
    }

    @Override
    public int getMaxConnections() {
        throw new IllegalStateException("Hibernate won't use this method");
    }

    @Override
    public int getMaxCursorNameLength() {
        throw new IllegalStateException("Hibernate won't use this method");
    }

    @Override
    public int getMaxIndexLength() {
        throw new IllegalStateException("Hibernate won't use this method");
    }

    @Override
    public int getMaxSchemaNameLength() {
        throw new IllegalStateException("Hibernate won't use this method");
    }

    @Override
    public int getMaxProcedureNameLength() {
        throw new IllegalStateException("Hibernate won't use this method");
    }

    @Override
    public int getMaxCatalogNameLength() {
        throw new IllegalStateException("Hibernate won't use this method");
    }

    @Override
    public int getMaxRowSize() {
        throw new IllegalStateException("Hibernate won't use this method");
    }

    @Override
    public boolean doesMaxRowSizeIncludeBlobs() {
        throw new IllegalStateException("Hibernate won't use this method");
    }

    @Override
    public int getMaxStatementLength() {
        throw new IllegalStateException("Hibernate won't use this method");
    }

    @Override
    public int getMaxStatements() {
        throw new IllegalStateException("Hibernate won't use this method");
    }

    @Override
    public int getMaxTableNameLength() {
        throw new IllegalStateException("Hibernate won't use this method");
    }

    @Override
    public int getMaxTablesInSelect() {
        throw new IllegalStateException("Hibernate won't use this method");
    }

    @Override
    public int getMaxUserNameLength() {
        throw new IllegalStateException("Hibernate won't use this method");
    }

    // ----------------------------------------------------------------------

    @Override
    public int getDefaultTransactionIsolation() {
        throw new IllegalStateException("Hibernate won't use this method");
    }

    @Override
    public boolean supportsTransactions() {
        throw new IllegalStateException("Hibernate won't use this method");
    }

    @Override
    public boolean supportsTransactionIsolationLevel(int level) {
        throw new IllegalStateException("Hibernate won't use this method");
    }

    @Override
    public boolean supportsDataDefinitionAndDataManipulationTransactions() {
        throw new IllegalStateException("Hibernate won't use this method");
    }

    @Override
    public boolean supportsDataManipulationTransactionsOnly() {
        throw new IllegalStateException("Hibernate won't use this method");
    }

    @Override
    public ResultSet getProcedures(String catalog, String schemaPattern, String procedureNamePattern) {
        throw new IllegalStateException("Hibernate won't use this method");
    }

    @Override
    public ResultSet getProcedureColumns(
            String catalog, String schemaPattern, String procedureNamePattern, String columnNamePattern) {
        throw new IllegalStateException("Hibernate won't use this method");
    }

    @Override
    public ResultSet getSchemas() {
        throw new IllegalStateException("Hibernate won't use this method");
    }

    @Override
    public ResultSet getTableTypes() {
        throw new IllegalStateException("Hibernate won't use this method");
    }

    @Override
    public ResultSet getColumnPrivileges(String catalog, String schema, String table, String columnNamePattern) {
        throw new IllegalStateException("Hibernate won't use this method");
    }

    @Override
    public ResultSet getTablePrivileges(String catalog, String schemaPattern, String tableNamePattern) {
        throw new IllegalStateException("Hibernate won't use this method");
    }

    @Override
    public ResultSet getBestRowIdentifier(String catalog, String schema, String table, int scope, boolean nullable) {
        throw new IllegalStateException("Hibernate won't use this method");
    }

    @Override
    public ResultSet getVersionColumns(String catalog, String schema, String table) {
        throw new IllegalStateException("Hibernate won't use this method");
    }

    @Override
    public ResultSet getExportedKeys(String catalog, String schema, String table) {
        throw new IllegalStateException("Hibernate won't use this method");
    }

    @Override
    public boolean supportsResultSetConcurrency(int type, int concurrency) {
        throw new IllegalStateException("Hibernate won't use this method");
    }

    @Override
    public boolean ownUpdatesAreVisible(int type) {
        throw new IllegalStateException("Hibernate won't use this method");
    }

    @Override
    public boolean ownDeletesAreVisible(int type) {
        throw new IllegalStateException("Hibernate won't use this method");
    }

    @Override
    public boolean ownInsertsAreVisible(int type) {
        throw new IllegalStateException("Hibernate won't use this method");
    }

    @Override
    public boolean othersUpdatesAreVisible(int type) {
        throw new IllegalStateException("Hibernate won't use this method");
    }

    @Override
    public boolean othersDeletesAreVisible(int type) {
        throw new IllegalStateException("Hibernate won't use this method");
    }

    @Override
    public boolean othersInsertsAreVisible(int type) {
        throw new IllegalStateException("Hibernate won't use this method");
    }

    @Override
    public boolean updatesAreDetected(int type) {
        throw new IllegalStateException("Hibernate won't use this method");
    }

    @Override
    public boolean deletesAreDetected(int type) {
        throw new IllegalStateException("Hibernate won't use this method");
    }

    @Override
    public boolean insertsAreDetected(int type) {
        throw new IllegalStateException("Hibernate won't use this method");
    }

    @Override
    public ResultSet getUDTs(String catalog, String schemaPattern, String typeNamePattern, int[] types) {
        throw new IllegalStateException("Hibernate won't use this method");
    }

    // ------------------- JDBC 3.0 -------------------------

    @Override
    public boolean supportsSavepoints() {
        throw new IllegalStateException("Hibernate won't use this method");
    }

    @Override
    public boolean supportsMultipleOpenResults() {
        throw new IllegalStateException("Hibernate won't use this method");
    }

    @Override
    public ResultSet getSuperTypes(String catalog, String schemaPattern, String typeNamePattern) {
        throw new IllegalStateException("Hibernate won't use this method");
    }

    @Override
    public ResultSet getSuperTables(String catalog, String schemaPattern, String tableNamePattern) {
        throw new IllegalStateException("Hibernate won't use this method");
    }

    @Override
    public ResultSet getAttributes(
            String catalog, String schemaPattern, String typeNamePattern, String attributeNamePattern) {
        throw new IllegalStateException("Hibernate won't use this method");
    }

    @Override
    public boolean supportsResultSetHoldability(int holdability) {
        throw new IllegalStateException("Hibernate won't use this method");
    }

    @Override
    public int getResultSetHoldability() {
        throw new IllegalStateException("Hibernate won't use this method");
    }

    @Override
    public boolean locatorsUpdateCopy() {
        throw new IllegalStateException("Hibernate won't use this method");
    }

    @Override
    public boolean supportsStatementPooling() {
        throw new IllegalStateException("Hibernate won't use this method");
    }

    // ------------------------- JDBC 4.0 -----------------------------------

    @Override
    public RowIdLifetime getRowIdLifetime() {
        throw new IllegalStateException("Hibernate won't use this method");
    }

    @Override
    public boolean supportsStoredFunctionsUsingCallSyntax() {
        throw new IllegalStateException("Hibernate won't use this method");
    }

    @Override
    public boolean autoCommitFailureClosesAllResultSets() {
        throw new IllegalStateException("Hibernate won't use this method");
    }

    @Override
    public ResultSet getClientInfoProperties() {
        throw new IllegalStateException("Hibernate won't use this method");
    }

    @Override
    public ResultSet getFunctions(String catalog, String schemaPattern, String functionNamePattern) {
        throw new IllegalStateException("Hibernate won't use this method");
    }

    @Override
    public ResultSet getFunctionColumns(
            String catalog, String schemaPattern, String functionNamePattern, String columnNamePattern) {
        throw new IllegalStateException("Hibernate won't use this method");
    }

    // --------------------------JDBC 4.1 -----------------------------

    @Override
    public ResultSet getPseudoColumns(
            String catalog, String schemaPattern, String tableNamePattern, String columnNamePattern) {
        throw new IllegalStateException("Hibernate won't use this method");
    }

    @Override
    public boolean generatedKeyAlwaysReturned() {
        throw new IllegalStateException("Hibernate won't use this method");
    }

    // --------------------------JDBC 4.2 -----------------------------

    // JDBC 4.3
}
