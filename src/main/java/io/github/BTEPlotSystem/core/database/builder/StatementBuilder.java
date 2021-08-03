package github.BTEPlotSystem.core.database.builder;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashMap;

public class StatementBuilder {

    public enum StatementType {
        DEFAULT, STRING, INTEGER, BOOLEAN, DATETIME
    }

    private final HashMap<StatementType, Object> statementValues = new HashMap<>();
    private final String sqlStatement;

    public StatementBuilder(String sql) {
        this.sqlStatement = sql;
    }

    public StatementBuilder setDefault(String value) {
        statementValues.put(StatementType.DEFAULT, value);
        return this;
    }

    public StatementBuilder setString(String value) {
        statementValues.put(StatementType.STRING, value);
        return this;
    }

    public StatementBuilder setInt(int value) {
        statementValues.put(StatementType.INTEGER, value);
        return this;
    }

    public StatementBuilder setBoolean(boolean value) {
        statementValues.put(StatementType.BOOLEAN, value);
        return this;
    }

    public StatementBuilder setDateTime(LocalDate value) {
        statementValues.put(StatementType.DATETIME, value);
        return this;
    }

    public PreparedStatementBuilder build() throws SQLException {
        return PreparedStatementBuilder.prepare(sqlStatement, statementValues);
    }

}
