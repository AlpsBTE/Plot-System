package github.BTEPlotSystem.core.database.builder;

public class TableBuilder {
    private final StringBuilder columns;
    private final StringBuilder keyConstraints;

    private StringBuilder columnBuilder;
    private int columnCount = 0;
    private int constraintCount = 0;

    private final String tableName;

    public TableBuilder(String tableName) {
        this.tableName = tableName;
        columns = new StringBuilder("CREATE TABLE IF NOT EXISTS " + tableName + " (");
        keyConstraints = new StringBuilder();
    }

    private void finalizeColumn() {
        if (columnBuilder != null) {
            if (columnCount > 0) {
                columns.append(',');
            }
            columns.append(columnBuilder);
            columnCount++;
            columnBuilder = null;
        }
    }

    public TableBuilder column(String column, String type) {
        finalizeColumn();
        columnBuilder = new StringBuilder();
        columnBuilder.append(column).append(" ").append(type);
        return this;
    }

    public TableBuilder primaryKey(boolean autoIncrement) {
        String currentColumn = columnBuilder.substring(0, columnBuilder.indexOf(" "));
        notNull();
        if (autoIncrement) columnBuilder.append(" AUTO_INCREMENT");
        primaryKey(currentColumn);

        return this;
    }

    public TableBuilder notNull() {
        columnBuilder.append(" NOT NULL");
        return this;
    }

    public TableBuilder Null() {
        columnBuilder.append(" NOT NULL");
        return this;
    }

    public TableBuilder defaultValue(boolean value) {
        return defaultValue(value ? "1" : "0");
    }

    public TableBuilder defaultValue(String value) {
        columnBuilder.append(" DEFAULT ").append(value);
        return this;
    }

    public TableBuilder foreignKey(String column, String referencedTable, String referencedColumn) {
        finalizeColumn();
        if (constraintCount > 0) {
            keyConstraints.append(',');
        }
        keyConstraints.append("FOREIGN KEY(")
                .append(column)
                .append(") REFERENCES ")
                .append(referencedTable)
                .append('(')
                .append(referencedColumn)
                .append(')');
        constraintCount++;
        return this;
    }

    private void primaryKey(String column) {
        finalizeColumn();
        if (constraintCount > 0) {
            keyConstraints.append(',');
        }
        keyConstraints.append("PRIMARY KEY (").append(column).append(')');
        constraintCount++;
    }

    public TableBuilder build() {
        return this;
    }

    public String getTableName() {
        return tableName;
    }
}
