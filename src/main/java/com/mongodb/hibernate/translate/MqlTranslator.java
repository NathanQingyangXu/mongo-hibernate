/*
 * Copyright 2025-present MongoDB, Inc.
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

package com.mongodb.hibernate.translate;

import static com.mongodb.hibernate.translate.TypeReference.COLLECTION_NAME;
import static com.mongodb.hibernate.translate.TypeReference.COMMAND;
import static com.mongodb.hibernate.translate.TypeReference.FIELD_NAME;
import static com.mongodb.hibernate.translate.TypeReference.FIELD_VALUE;
import static com.mongodb.hibernate.translate.TypeReference.FILTER;
import static com.mongodb.hibernate.translate.TypeReference.PROJECT_STAGE_SPECIFICATION;

import com.mongodb.hibernate.internal.NotYetImplementedException;
import com.mongodb.hibernate.internal.mongoast.AstElement;
import com.mongodb.hibernate.internal.mongoast.AstPlaceholder;
import com.mongodb.hibernate.internal.mongoast.command.AstAggregateCommand;
import com.mongodb.hibernate.internal.mongoast.command.AstCommand;
import com.mongodb.hibernate.internal.mongoast.command.AstInsertCommand;
import com.mongodb.hibernate.internal.mongoast.command.aggregate.AstPipeline;
import com.mongodb.hibernate.internal.mongoast.command.aggregate.AstStage;
import com.mongodb.hibernate.internal.mongoast.command.aggregate.stage.AstMatchStage;
import com.mongodb.hibernate.internal.mongoast.command.aggregate.stage.AstProjectStage;
import com.mongodb.hibernate.internal.mongoast.command.aggregate.stage.AstProjectStageSpecification;
import com.mongodb.hibernate.internal.mongoast.expression.AstExpression;
import com.mongodb.hibernate.internal.mongoast.expression.AstFieldPathExpression;
import com.mongodb.hibernate.internal.mongoast.filter.AstComparisonFilterOperation;
import com.mongodb.hibernate.internal.mongoast.filter.AstComparisonFilterOperator;
import com.mongodb.hibernate.internal.mongoast.filter.AstFieldOperationFilter;
import com.mongodb.hibernate.internal.mongoast.filter.AstFilter;
import com.mongodb.hibernate.internal.mongoast.filter.AstFilterField;
import com.mongodb.hibernate.internal.mongoast.filter.AstMatchesEverythingFilter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.bson.json.JsonWriter;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.internal.util.collections.Stack;
import org.hibernate.metamodel.mapping.ModelPartContainer;
import org.hibernate.persister.entity.AbstractEntityPersister;
import org.hibernate.persister.internal.SqlFragmentPredicate;
import org.hibernate.query.spi.QueryOptions;
import org.hibernate.query.sqm.ComparisonOperator;
import org.hibernate.query.sqm.tree.expression.Conversion;
import org.hibernate.sql.ast.Clause;
import org.hibernate.sql.ast.SqlAstNodeRenderingMode;
import org.hibernate.sql.ast.SqlAstTranslator;
import org.hibernate.sql.ast.spi.SqlSelection;
import org.hibernate.sql.ast.tree.SqlAstNode;
import org.hibernate.sql.ast.tree.Statement;
import org.hibernate.sql.ast.tree.delete.DeleteStatement;
import org.hibernate.sql.ast.tree.expression.AggregateColumnWriteExpression;
import org.hibernate.sql.ast.tree.expression.Any;
import org.hibernate.sql.ast.tree.expression.BinaryArithmeticExpression;
import org.hibernate.sql.ast.tree.expression.CaseSearchedExpression;
import org.hibernate.sql.ast.tree.expression.CaseSimpleExpression;
import org.hibernate.sql.ast.tree.expression.CastTarget;
import org.hibernate.sql.ast.tree.expression.Collation;
import org.hibernate.sql.ast.tree.expression.ColumnReference;
import org.hibernate.sql.ast.tree.expression.Distinct;
import org.hibernate.sql.ast.tree.expression.Duration;
import org.hibernate.sql.ast.tree.expression.DurationUnit;
import org.hibernate.sql.ast.tree.expression.EmbeddableTypeLiteral;
import org.hibernate.sql.ast.tree.expression.EntityTypeLiteral;
import org.hibernate.sql.ast.tree.expression.Every;
import org.hibernate.sql.ast.tree.expression.Expression;
import org.hibernate.sql.ast.tree.expression.ExtractUnit;
import org.hibernate.sql.ast.tree.expression.Format;
import org.hibernate.sql.ast.tree.expression.JdbcLiteral;
import org.hibernate.sql.ast.tree.expression.JdbcParameter;
import org.hibernate.sql.ast.tree.expression.ModifiedSubQueryExpression;
import org.hibernate.sql.ast.tree.expression.NestedColumnReference;
import org.hibernate.sql.ast.tree.expression.Over;
import org.hibernate.sql.ast.tree.expression.Overflow;
import org.hibernate.sql.ast.tree.expression.QueryLiteral;
import org.hibernate.sql.ast.tree.expression.SelfRenderingExpression;
import org.hibernate.sql.ast.tree.expression.SqlSelectionExpression;
import org.hibernate.sql.ast.tree.expression.SqlTuple;
import org.hibernate.sql.ast.tree.expression.Star;
import org.hibernate.sql.ast.tree.expression.Summarization;
import org.hibernate.sql.ast.tree.expression.TrimSpecification;
import org.hibernate.sql.ast.tree.expression.UnaryOperation;
import org.hibernate.sql.ast.tree.expression.UnparsedNumericLiteral;
import org.hibernate.sql.ast.tree.from.FromClause;
import org.hibernate.sql.ast.tree.from.FunctionTableReference;
import org.hibernate.sql.ast.tree.from.NamedTableReference;
import org.hibernate.sql.ast.tree.from.QueryPartTableReference;
import org.hibernate.sql.ast.tree.from.TableGroup;
import org.hibernate.sql.ast.tree.from.TableGroupJoin;
import org.hibernate.sql.ast.tree.from.TableReferenceJoin;
import org.hibernate.sql.ast.tree.from.ValuesTableReference;
import org.hibernate.sql.ast.tree.insert.InsertSelectStatement;
import org.hibernate.sql.ast.tree.predicate.BetweenPredicate;
import org.hibernate.sql.ast.tree.predicate.BooleanExpressionPredicate;
import org.hibernate.sql.ast.tree.predicate.ComparisonPredicate;
import org.hibernate.sql.ast.tree.predicate.ExistsPredicate;
import org.hibernate.sql.ast.tree.predicate.FilterPredicate;
import org.hibernate.sql.ast.tree.predicate.GroupedPredicate;
import org.hibernate.sql.ast.tree.predicate.InArrayPredicate;
import org.hibernate.sql.ast.tree.predicate.InListPredicate;
import org.hibernate.sql.ast.tree.predicate.InSubQueryPredicate;
import org.hibernate.sql.ast.tree.predicate.Junction;
import org.hibernate.sql.ast.tree.predicate.LikePredicate;
import org.hibernate.sql.ast.tree.predicate.NegatedPredicate;
import org.hibernate.sql.ast.tree.predicate.NullnessPredicate;
import org.hibernate.sql.ast.tree.predicate.Predicate;
import org.hibernate.sql.ast.tree.predicate.SelfRenderingPredicate;
import org.hibernate.sql.ast.tree.predicate.ThruthnessPredicate;
import org.hibernate.sql.ast.tree.select.QueryGroup;
import org.hibernate.sql.ast.tree.select.QueryPart;
import org.hibernate.sql.ast.tree.select.QuerySpec;
import org.hibernate.sql.ast.tree.select.SelectClause;
import org.hibernate.sql.ast.tree.select.SelectStatement;
import org.hibernate.sql.ast.tree.select.SortSpecification;
import org.hibernate.sql.ast.tree.update.Assignment;
import org.hibernate.sql.ast.tree.update.UpdateStatement;
import org.hibernate.sql.exec.spi.JdbcOperation;
import org.hibernate.sql.exec.spi.JdbcOperationQuerySelect;
import org.hibernate.sql.exec.spi.JdbcParameterBinder;
import org.hibernate.sql.exec.spi.JdbcParameterBindings;
import org.hibernate.sql.model.MutationOperation;
import org.hibernate.sql.model.ast.ColumnWriteFragment;
import org.hibernate.sql.model.ast.TableInsert;
import org.hibernate.sql.model.ast.TableMutation;
import org.hibernate.sql.model.internal.OptionalTableUpdate;
import org.hibernate.sql.model.internal.TableDeleteCustomSql;
import org.hibernate.sql.model.internal.TableDeleteStandard;
import org.hibernate.sql.model.internal.TableInsertCustomSql;
import org.hibernate.sql.model.internal.TableInsertStandard;
import org.hibernate.sql.model.internal.TableUpdateCustomSql;
import org.hibernate.sql.model.internal.TableUpdateStandard;
import org.hibernate.sql.results.jdbc.spi.JdbcValuesMappingProducer;
import org.jspecify.annotations.Nullable;

final class MqlTranslator<T extends JdbcOperation> implements SqlAstTranslator<T> {

    private final SessionFactoryImplementor sessionFactory;
    private final Statement statement;

    private final AstVisitorValueHolder astVisitorValueHolder = new AstVisitorValueHolder();

    private final List<JdbcParameterBinder> parameterBinders = new ArrayList<>();
    private final Set<String> affectedTableNames = new HashSet<>();

    MqlTranslator(SessionFactoryImplementor sessionFactory, Statement statement) {
        this.sessionFactory = sessionFactory;
        this.statement = statement;
    }

    @Override
    public SessionFactoryImplementor getSessionFactory() {
        return sessionFactory;
    }

    @Override
    public void render(SqlAstNode sqlAstNode, SqlAstNodeRenderingMode sqlAstNodeRenderingMode) {
        throw new NotYetImplementedException();
    }

    @Override
    public boolean supportsFilterClause() {
        throw new NotYetImplementedException();
    }

    @Override
    public QueryPart getCurrentQueryPart() {
        throw new NotYetImplementedException();
    }

    @Override
    public Stack<Clause> getCurrentClauseStack() {
        throw new NotYetImplementedException();
    }

    @Override
    public Set<String> getAffectedTableNames() {
        throw new NotYetImplementedException();
    }

    @Override
    @SuppressWarnings({"unchecked"})
    public T translate(JdbcParameterBindings jdbcParameterBindings, QueryOptions queryOptions) {
        if (statement instanceof TableMutation<?> tableMutation) {
            if (statement instanceof TableInsert) {
                return (T) translateTableMutation(tableMutation);
            } else {
                return (T) new JdbcMutationOperationAdapter();
            }
        }
        if (statement instanceof SelectStatement selectStatement) {
            return (T) translateSelect(selectStatement);
        }
        throw new NotYetImplementedException();
    }

    private JdbcOperationQuerySelect translateSelect(SelectStatement selectStatement) {
        var selectCommand = astVisitorValueHolder.getValue(COMMAND, () -> visitSelectStatement(selectStatement));

        return new JdbcOperationQuerySelect(
                translateMongoCommand(selectCommand),
                parameterBinders,
                buildJdbcValuesMappingProducer(selectStatement),
                affectedTableNames);
    }

    private JdbcValuesMappingProducer buildJdbcValuesMappingProducer(SelectStatement selectStatement) {
        return getSessionFactory()
                .getFastSessionServices()
                .getJdbcValuesMappingProducerProvider()
                .buildMappingProducer(selectStatement, getSessionFactory());
    }

    private MutationOperation translateTableMutation(TableMutation<?> tableMutation) {
        var rootAstNode = astVisitorValueHolder.getValue(COMMAND, () -> tableMutation.accept(this));
        return tableMutation.createMutationOperation(translateMongoCommand(rootAstNode), parameterBinders);
    }

    private static String translateMongoCommand(AstCommand astCommand) {
        var writer = new StringWriter();
        astCommand.render(new JsonWriter(writer));
        return writer.toString();
    }

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // Table Mutation: insertion

    @Override
    public void visitStandardTableInsert(TableInsertStandard tableInsertStandard) {
        var tableName = tableInsertStandard.getTableName();
        var astElements = new ArrayList<AstElement>(tableInsertStandard.getNumberOfValueBindings());
        for (var columnValueBinding : tableInsertStandard.getValueBindings()) {
            var astValue = astVisitorValueHolder.getValue(
                    FIELD_VALUE, () -> columnValueBinding.getValueExpression().accept(this));
            var columnExpression = columnValueBinding.getColumnReference().getColumnExpression();
            astElements.add(new AstElement(columnExpression, astValue));
        }
        astVisitorValueHolder.setValue(COMMAND, new AstInsertCommand(tableName, astElements));
    }

    @Override
    public void visitColumnWriteFragment(ColumnWriteFragment columnWriteFragment) {
        if (columnWriteFragment.getParameters().size() != 1) {
            throw new NotYetImplementedException();
        }
        var jdbcParameter = columnWriteFragment.getParameters().iterator().next();
        parameterBinders.add(jdbcParameter.getParameterBinder());
        astVisitorValueHolder.setValue(FIELD_VALUE, AstPlaceholder.INSTANCE);
    }

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    @Override
    public void visitSelectStatement(SelectStatement selectStatement) {
        selectStatement.getQueryPart().accept(this);
    }

    @Override
    public void visitQuerySpec(QuerySpec querySpec) {
        var tableName = astVisitorValueHolder.getValue(
                COLLECTION_NAME, () -> querySpec.getFromClause().accept(this));
        var stageList = new ArrayList<AstStage>();

        var filter = renderWhereClause(querySpec.getWhereClauseRestrictions());
        stageList.add(new AstMatchStage(filter));

        List<AstProjectStageSpecification> projectStageSpecifications = astVisitorValueHolder.getValue(
                PROJECT_STAGE_SPECIFICATION, () -> visitSelectClause(querySpec.getSelectClause()));
        stageList.add(new AstProjectStage(projectStageSpecifications));

        astVisitorValueHolder.setValue(COMMAND, new AstAggregateCommand(tableName, new AstPipeline(stageList)));
    }

    @Override
    public void visitFromClause(FromClause fromClause) {
        var tableGroups = fromClause.getRoots();
        if (tableGroups.size() != 1) {
            throw new NotYetImplementedException();
        }
        var tableGroup = tableGroups.iterator().next();
        ModelPartContainer modelPart = tableGroup.getModelPart();
        if (modelPart instanceof AbstractEntityPersister) {
            String[] querySpaces = (String[]) ((AbstractEntityPersister) modelPart).getQuerySpaces();
            affectedTableNames.addAll(Arrays.asList(querySpaces));
        }
        tableGroup.getPrimaryTableReference().accept(this);
    }

    @Override
    public void visitNamedTableReference(NamedTableReference namedTableReference) {
        astVisitorValueHolder.setValue(COLLECTION_NAME, namedTableReference.getTableExpression());
    }

    private AstFilter renderWhereClause(@Nullable Predicate whereClauseRestrictions) {
        if (whereClauseRestrictions == null || whereClauseRestrictions.isEmpty()) {
            return AstMatchesEverythingFilter.INSTANCE;
        }
        return astVisitorValueHolder.getValue(FILTER, () -> whereClauseRestrictions.accept(this));
    }

    @Override
    public void visitRelationalPredicate(ComparisonPredicate comparisonPredicate) {
        astVisitorValueHolder.setValue(
                FILTER,
                renderComparisonStandard(
                        comparisonPredicate.getLeftHandExpression(),
                        comparisonPredicate.getOperator(),
                        comparisonPredicate.getRightHandExpression()));
    }

    private AstFilter renderComparisonStandard(Expression lhs, ComparisonOperator operator, Expression rhs) {
        var fieldName = astVisitorValueHolder.getValue(FIELD_NAME, () -> lhs.accept(this));
        var value = astVisitorValueHolder.getValue(FIELD_VALUE, () -> rhs.accept(this));
        return new AstFieldOperationFilter(
                new AstFilterField(fieldName),
                new AstComparisonFilterOperation(getComparisonFilterOperator(operator), value));
    }

    private AstComparisonFilterOperator getComparisonFilterOperator(ComparisonOperator operator) {
        return switch (operator) {
            case EQUAL -> AstComparisonFilterOperator.EQ;
            case NOT_EQUAL -> AstComparisonFilterOperator.NE;
            case LESS_THAN -> AstComparisonFilterOperator.LT;
            case LESS_THAN_OR_EQUAL -> AstComparisonFilterOperator.LTE;
            case GREATER_THAN -> AstComparisonFilterOperator.GT;
            case GREATER_THAN_OR_EQUAL -> AstComparisonFilterOperator.GTE;
            default -> throw new NotYetImplementedException("unknown operator: " + operator.name());
        };
    }

    @Override
    public void visitDeleteStatement(DeleteStatement deleteStatement) {
        throw new NotYetImplementedException();
    }

    @Override
    public void visitUpdateStatement(UpdateStatement updateStatement) {
        throw new NotYetImplementedException();
    }

    @Override
    public void visitInsertStatement(InsertSelectStatement insertSelectStatement) {
        throw new NotYetImplementedException();
    }

    @Override
    public void visitAssignment(Assignment assignment) {
        throw new NotYetImplementedException();
    }

    @Override
    public void visitQueryGroup(QueryGroup queryGroup) {
        throw new NotYetImplementedException();
    }

    @Override
    public void visitSortSpecification(SortSpecification sortSpecification) {
        throw new NotYetImplementedException();
    }

    @Override
    public void visitOffsetFetchClause(QueryPart queryPart) {
        throw new NotYetImplementedException();
    }

    @Override
    public void visitSelectClause(SelectClause selectClause) {
        astVisitorValueHolder.setValue(PROJECT_STAGE_SPECIFICATION, renderSqlSelections(selectClause));
    }

    private List<AstProjectStageSpecification> renderSqlSelections(SelectClause selectClause) {
        var sqlSelections = selectClause.getSqlSelections();
        var size = sqlSelections.size();
        var projectStageSpecifications = new ArrayList<AstProjectStageSpecification>(size);

        for (var i = 0; i < size; i++) {
            var sqlSelection = sqlSelections.get(i);
            if (sqlSelection.isVirtual()) {
                continue;
            }
            if (sqlSelection.getExpression() instanceof ColumnReference columnReference) {
                AstExpression projectionExpression =
                        new AstFieldPathExpression("$" + columnReference.getColumnExpression());
                projectStageSpecifications.add(AstProjectStageSpecification.Set("f" + i, projectionExpression));
            } else {
                throw new NotYetImplementedException();
            }
        }
        projectStageSpecifications.add(AstProjectStageSpecification.ExcludeId());
        return projectStageSpecifications;
    }

    @Override
    public void visitColumnReference(ColumnReference columnReference) {
        astVisitorValueHolder.setValue(FIELD_NAME, columnReference.getColumnExpression());
    }

    @Override
    public void visitParameter(JdbcParameter jdbcParameter) {
        astVisitorValueHolder.setValue(FIELD_VALUE, AstPlaceholder.INSTANCE);
        parameterBinders.add(jdbcParameter.getParameterBinder());
    }

    @Override
    public void visitSqlSelection(SqlSelection sqlSelection) {
        throw new NotYetImplementedException();
    }

    @Override
    public void visitTableGroup(TableGroup tableGroup) {
        throw new NotYetImplementedException();
    }

    @Override
    public void visitTableGroupJoin(TableGroupJoin tableGroupJoin) {
        throw new NotYetImplementedException();
    }

    @Override
    public void visitValuesTableReference(ValuesTableReference valuesTableReference) {
        throw new NotYetImplementedException();
    }

    @Override
    public void visitQueryPartTableReference(QueryPartTableReference queryPartTableReference) {
        throw new NotYetImplementedException();
    }

    @Override
    public void visitFunctionTableReference(FunctionTableReference functionTableReference) {
        throw new NotYetImplementedException();
    }

    @Override
    public void visitTableReferenceJoin(TableReferenceJoin tableReferenceJoin) {
        throw new NotYetImplementedException();
    }

    @Override
    public void visitNestedColumnReference(NestedColumnReference nestedColumnReference) {
        throw new NotYetImplementedException();
    }

    @Override
    public void visitAggregateColumnWriteExpression(AggregateColumnWriteExpression aggregateColumnWriteExpression) {
        throw new NotYetImplementedException();
    }

    @Override
    public void visitExtractUnit(ExtractUnit extractUnit) {
        throw new NotYetImplementedException();
    }

    @Override
    public void visitFormat(Format format) {
        throw new NotYetImplementedException();
    }

    @Override
    public void visitDistinct(Distinct distinct) {
        throw new NotYetImplementedException();
    }

    @Override
    public void visitOverflow(Overflow overflow) {
        throw new NotYetImplementedException();
    }

    @Override
    public void visitStar(Star star) {
        throw new NotYetImplementedException();
    }

    @Override
    public void visitTrimSpecification(TrimSpecification trimSpecification) {
        throw new NotYetImplementedException();
    }

    @Override
    public void visitCastTarget(CastTarget castTarget) {
        throw new NotYetImplementedException();
    }

    @Override
    public void visitBinaryArithmeticExpression(BinaryArithmeticExpression binaryArithmeticExpression) {
        throw new NotYetImplementedException();
    }

    @Override
    public void visitCaseSearchedExpression(CaseSearchedExpression caseSearchedExpression) {
        throw new NotYetImplementedException();
    }

    @Override
    public void visitCaseSimpleExpression(CaseSimpleExpression caseSimpleExpression) {
        throw new NotYetImplementedException();
    }

    @Override
    public void visitAny(Any any) {
        throw new NotYetImplementedException();
    }

    @Override
    public void visitEvery(Every every) {
        throw new NotYetImplementedException();
    }

    @Override
    public void visitSummarization(Summarization summarization) {
        throw new NotYetImplementedException();
    }

    @Override
    public void visitOver(Over<?> over) {
        throw new NotYetImplementedException();
    }

    @Override
    public void visitSelfRenderingExpression(SelfRenderingExpression selfRenderingExpression) {
        throw new NotYetImplementedException();
    }

    @Override
    public void visitSqlSelectionExpression(SqlSelectionExpression sqlSelectionExpression) {
        throw new NotYetImplementedException();
    }

    @Override
    public void visitEntityTypeLiteral(EntityTypeLiteral entityTypeLiteral) {
        throw new NotYetImplementedException();
    }

    @Override
    public void visitEmbeddableTypeLiteral(EmbeddableTypeLiteral embeddableTypeLiteral) {
        throw new NotYetImplementedException();
    }

    @Override
    public void visitTuple(SqlTuple sqlTuple) {
        throw new NotYetImplementedException();
    }

    @Override
    public void visitCollation(Collation collation) {
        throw new NotYetImplementedException();
    }

    @Override
    public void visitJdbcLiteral(JdbcLiteral<?> jdbcLiteral) {
        throw new NotYetImplementedException();
    }

    @Override
    public void visitQueryLiteral(QueryLiteral<?> queryLiteral) {
        throw new NotYetImplementedException();
    }

    @Override
    public <N extends Number> void visitUnparsedNumericLiteral(UnparsedNumericLiteral<N> unparsedNumericLiteral) {
        throw new NotYetImplementedException();
    }

    @Override
    public void visitUnaryOperationExpression(UnaryOperation unaryOperation) {
        throw new NotYetImplementedException();
    }

    @Override
    public void visitModifiedSubQueryExpression(ModifiedSubQueryExpression modifiedSubQueryExpression) {
        throw new NotYetImplementedException();
    }

    @Override
    public void visitBooleanExpressionPredicate(BooleanExpressionPredicate booleanExpressionPredicate) {
        throw new NotYetImplementedException();
    }

    @Override
    public void visitBetweenPredicate(BetweenPredicate betweenPredicate) {
        throw new NotYetImplementedException();
    }

    @Override
    public void visitFilterPredicate(FilterPredicate filterPredicate) {
        throw new NotYetImplementedException();
    }

    @Override
    public void visitFilterFragmentPredicate(FilterPredicate.FilterFragmentPredicate filterFragmentPredicate) {
        throw new NotYetImplementedException();
    }

    @Override
    public void visitSqlFragmentPredicate(SqlFragmentPredicate sqlFragmentPredicate) {
        throw new NotYetImplementedException();
    }

    @Override
    public void visitGroupedPredicate(GroupedPredicate groupedPredicate) {
        throw new NotYetImplementedException();
    }

    @Override
    public void visitInListPredicate(InListPredicate inListPredicate) {
        throw new NotYetImplementedException();
    }

    @Override
    public void visitInSubQueryPredicate(InSubQueryPredicate inSubQueryPredicate) {
        throw new NotYetImplementedException();
    }

    @Override
    public void visitInArrayPredicate(InArrayPredicate inArrayPredicate) {
        throw new NotYetImplementedException();
    }

    @Override
    public void visitExistsPredicate(ExistsPredicate existsPredicate) {
        throw new NotYetImplementedException();
    }

    @Override
    public void visitJunction(Junction junction) {
        throw new NotYetImplementedException();
    }

    @Override
    public void visitLikePredicate(LikePredicate likePredicate) {
        throw new NotYetImplementedException();
    }

    @Override
    public void visitNegatedPredicate(NegatedPredicate negatedPredicate) {
        throw new NotYetImplementedException();
    }

    @Override
    public void visitNullnessPredicate(NullnessPredicate nullnessPredicate) {
        throw new NotYetImplementedException();
    }

    @Override
    public void visitThruthnessPredicate(ThruthnessPredicate thruthnessPredicate) {
        throw new NotYetImplementedException();
    }

    @Override
    public void visitSelfRenderingPredicate(SelfRenderingPredicate selfRenderingPredicate) {
        throw new NotYetImplementedException();
    }

    @Override
    public void visitDurationUnit(DurationUnit durationUnit) {
        throw new NotYetImplementedException();
    }

    @Override
    public void visitDuration(Duration duration) {
        throw new NotYetImplementedException();
    }

    @Override
    public void visitConversion(Conversion conversion) {
        throw new NotYetImplementedException();
    }

    @Override
    public void visitCustomTableInsert(TableInsertCustomSql tableInsertCustomSql) {
        throw new NotYetImplementedException();
    }

    @Override
    public void visitStandardTableDelete(TableDeleteStandard tableDeleteStandard) {
        throw new NotYetImplementedException();
    }

    @Override
    public void visitCustomTableDelete(TableDeleteCustomSql tableDeleteCustomSql) {
        throw new NotYetImplementedException();
    }

    @Override
    public void visitStandardTableUpdate(TableUpdateStandard tableUpdateStandard) {
        throw new NotYetImplementedException();
    }

    @Override
    public void visitOptionalTableUpdate(OptionalTableUpdate optionalTableUpdate) {
        throw new NotYetImplementedException();
    }

    @Override
    public void visitCustomTableUpdate(TableUpdateCustomSql tableUpdateCustomSql) {
        throw new NotYetImplementedException();
    }
}
