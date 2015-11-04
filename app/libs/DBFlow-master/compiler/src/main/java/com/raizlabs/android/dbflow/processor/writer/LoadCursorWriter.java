package com.raizlabs.android.dbflow.processor.writer;

import com.google.common.collect.Sets;
import com.raizlabs.android.dbflow.processor.definition.BaseTableDefinition;
import com.raizlabs.android.dbflow.processor.definition.ColumnDefinition;
import com.raizlabs.android.dbflow.processor.definition.TableDefinition;
import com.raizlabs.android.dbflow.processor.model.builder.AdapterQueryBuilder;
import com.raizlabs.android.dbflow.processor.utils.ModelUtils;
import com.raizlabs.android.dbflow.processor.utils.WriterUtils;
import com.squareup.javawriter.JavaWriter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.lang.model.element.Modifier;

/**
 * Description: Writes the load from cursor statement.
 */
public class LoadCursorWriter implements FlowWriter {

    public static final Map<String, String> CURSOR_METHOD_MAP = new HashMap<String, String>() {
        {
            put(byte[].class.getName(), "getBlob");
            put(Byte[].class.getName(), "getBlob");
            put(double.class.getName(), "getDouble");
            put(Double.class.getName(), "getDouble");
            put(float.class.getName(), "getFloat");
            put(Float.class.getName(), "getFloat");
            put(int.class.getName(), "getInt");
            put(Integer.class.getName(), "getInt");
            put(long.class.getName(), "getLong");
            put(Long.class.getName(), "getLong");
            put(short.class.getName(), "getShort");
            put(Short.class.getName(), "getShort");
            put(String.class.getName(), "getString");
        }
    };

    private BaseTableDefinition baseTableDefinition;
    private final boolean isModelContainerDefinition;
    private final boolean implementsLoadFromCursorListener;

    public LoadCursorWriter(BaseTableDefinition baseTableDefinition, boolean isModelContainerDefinition,
                            boolean implementsLoadFromCursorListener) {
        this.baseTableDefinition = baseTableDefinition;
        this.isModelContainerDefinition = isModelContainerDefinition;

        this.implementsLoadFromCursorListener = implementsLoadFromCursorListener;
    }

    @Override
    public void write(JavaWriter javaWriter) throws IOException {
        javaWriter.emitEmptyLine();
        javaWriter.emitAnnotation(Override.class);
        final String[] params = new String[4];
        params[0] = "Cursor";
        params[1] = "cursor";
        params[2] = ModelUtils.getParameter(isModelContainerDefinition, baseTableDefinition.getModelClassName());
        params[3] = ModelUtils.getVariable(isModelContainerDefinition);
        WriterUtils.emitMethod(javaWriter, new FlowWriter() {
            @Override
            public void write(JavaWriter javaWriter) throws IOException {

                for (ColumnDefinition columnDefinition : baseTableDefinition.getColumnDefinitions()) {
                    columnDefinition.writeLoadFromCursorDefinition(baseTableDefinition, javaWriter, isModelContainerDefinition);
                }

                if (implementsLoadFromCursorListener && !isModelContainerDefinition) {
                    javaWriter.emitStatement("%1s.onLoadFromCursor(%1s)", ModelUtils.getVariable(isModelContainerDefinition),
                            params[1]);
                }

            }
        }, "void", "loadFromCursor", Sets.newHashSet(Modifier.PUBLIC), params);

        if(baseTableDefinition instanceof TableDefinition) {

            final TableDefinition tableDefinition = ((TableDefinition) baseTableDefinition);

            if (tableDefinition.hasCachingId) {
                String[] params2 = new String[2];
                params2[0] = ModelUtils.getParameter(isModelContainerDefinition, tableDefinition.getModelClassName());
                params2[1] = ModelUtils.getVariable(isModelContainerDefinition);
                WriterUtils.emitOverriddenMethod(javaWriter, new FlowWriter() {
                    @Override
                    public void write(JavaWriter javaWriter) throws IOException {
                        ColumnDefinition columnDefinition = tableDefinition.getColumnDefinitions().get(0);
                        AdapterQueryBuilder queryBuilder = new AdapterQueryBuilder()
                                .append("return ")
                                .appendCast("long")
                                .append(ModelUtils.getVariable(isModelContainerDefinition));

                        if (!isModelContainerDefinition) {
                            queryBuilder.append(".").append(columnDefinition.columnFieldName);
                        } else {
                            String containerKeyName = columnDefinition.columnFieldName;
                            if (columnDefinition.containerKeyName != null) {
                                containerKeyName = columnDefinition.containerKeyName;
                            }
                            queryBuilder.append(".").appendGetValue(containerKeyName);
                        }

                        javaWriter.emitStatement(queryBuilder.append(")").getQuery());
                    }
                }, "long", "getCachingId", Sets.newHashSet(Modifier.PUBLIC), params2);

                if (!isModelContainerDefinition) {
                    WriterUtils.emitOverriddenMethod(javaWriter, new FlowWriter() {
                        @Override
                        public void write(JavaWriter javaWriter) throws IOException {
                            ColumnDefinition columnDefinition = tableDefinition.getColumnDefinitions().get(0);

                            javaWriter.emitStatement("return %1s.%1s", tableDefinition.getTableSourceClassName(), columnDefinition.columnName.toUpperCase());
                        }
                    }, "String", "getCachingColumnName", Sets.newHashSet(Modifier.PUBLIC));
                }
            } else if (tableDefinition.hasAutoIncrement) {
                params[0] = ModelUtils.getParameter(isModelContainerDefinition, tableDefinition.getModelClassName());
                params[1] = ModelUtils.getVariable(isModelContainerDefinition);
                params[2] = "long";
                params[3] = "id";
                WriterUtils.emitOverriddenMethod(javaWriter, new FlowWriter() {
                    @Override
                    public void write(JavaWriter javaWriter) throws IOException {
                        ColumnDefinition columnDefinition = tableDefinition.autoIncrementDefinition;
                        AdapterQueryBuilder queryBuilder = new AdapterQueryBuilder()
                                .append(ModelUtils.getVariable(isModelContainerDefinition));

                        if (!isModelContainerDefinition) {
                            queryBuilder.append(".").append(columnDefinition.columnFieldName)
                                    .append(" = ")
                                    .appendCast(columnDefinition.columnFieldType)
                                    .append(params[3]).append(")");
                        } else {
                            String containerKeyName = columnDefinition.columnFieldName;
                            if (columnDefinition.containerKeyName != null) {
                                containerKeyName = columnDefinition.containerKeyName;
                            }
                            queryBuilder.appendPut(containerKeyName)
                                    .append(params[3]).append(")");
                        }

                        javaWriter.emitStatement(queryBuilder.getQuery());
                    }
                }, "void", "updateAutoIncrement", Sets.newHashSet(Modifier.PUBLIC), params);

                String[] params2 = new String[2];
                params2[0] = ModelUtils.getParameter(isModelContainerDefinition, tableDefinition.getModelClassName());
                params2[1] = ModelUtils.getVariable(isModelContainerDefinition);
                WriterUtils.emitOverriddenMethod(javaWriter, new FlowWriter() {
                    @Override
                    public void write(JavaWriter javaWriter) throws IOException {
                        ColumnDefinition columnDefinition = tableDefinition.autoIncrementDefinition;
                        AdapterQueryBuilder queryBuilder = new AdapterQueryBuilder()
                                .append("return ")
                                .appendCast("long")
                                .append(ModelUtils.getVariable(isModelContainerDefinition));

                        if (!isModelContainerDefinition) {
                            queryBuilder.append(".").append(columnDefinition.columnFieldName);
                        } else {
                            String containerKeyName = columnDefinition.columnFieldName;
                            if (columnDefinition.containerKeyName != null) {
                                containerKeyName = columnDefinition.containerKeyName;
                            }
                            queryBuilder.append(".").appendGetValue(containerKeyName);
                        }

                        javaWriter.emitStatement(queryBuilder.append(")").getQuery());
                    }
                }, "long", "getAutoIncrementingId", Sets.newHashSet(Modifier.PUBLIC), params2);

                if (!isModelContainerDefinition) {
                    WriterUtils.emitOverriddenMethod(javaWriter, new FlowWriter() {
                        @Override
                        public void write(JavaWriter javaWriter) throws IOException {
                            ColumnDefinition columnDefinition = tableDefinition.autoIncrementDefinition;

                            javaWriter.emitStatement("return %1s.%1s", tableDefinition.getTableSourceClassName(), columnDefinition.columnName.toUpperCase());
                        }
                    }, "String", "getAutoIncrementingColumnName", Sets.newHashSet(Modifier.PUBLIC));
                }
            }
        }
    }
}
