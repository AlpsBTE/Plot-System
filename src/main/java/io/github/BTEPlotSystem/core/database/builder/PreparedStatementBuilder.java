package github.BTEPlotSystem.core.database.builder;

import github.BTEPlotSystem.core.database.DatabaseConnection;

import java.sql.*;
import java.util.HashMap;
import java.util.Objects;

public class PreparedStatementBuilder {

    private final PreparedStatement preparedStatement;

    public PreparedStatementBuilder(PreparedStatement statement) {
        this.preparedStatement = statement;
    }

    public PreparedStatement getPreparedStatement() {
        return preparedStatement;
    }

    public static PreparedStatementBuilder prepare(String sql, HashMap<StatementBuilder.StatementType, Object> values) throws SQLException {
        PreparedStatement prepStatement;

        try (Connection con = DatabaseConnection.getConnection()) {
            prepStatement = Objects.requireNonNull(con).prepareStatement(sql);

            int counter = 1;
            for (StatementBuilder.StatementType type : values.keySet()) {
                switch (type) {
                    case STRING:
                        prepStatement.setString(counter, (String) values.get(type));
                        break;
                    case INTEGER:
                        prepStatement.setInt(counter, (int) values.get(type));
                        break;
                    case BOOLEAN:
                        prepStatement.setBoolean(counter, (boolean) values.get(type));
                        break;
                    case DATETIME:
                        prepStatement.setDate(counter, (Date) values.get(type));
                        break;
                    default:
                            /*int indexCounter = 0;
                            int index = 0;
                            for (char c : sql.toCharArray()) {
                                index++;
                                if (c == '?') {
                                    if (indexCounter == counter) {
                                        break;
                                    }
                                    indexCounter++;
                                }
                            }

                            sql = sql.substring(0,index) + "DEFAULT(" + values.get(type) + ")";
                            if (sql.length() >= index + 1) sql = sql + sql.substring(index + 1);*/
                        sql = sql.replaceFirst("\\?", "DEFAULT(" + values.get(type) + ")");
                }
                counter++;
            }
        }

        return new PreparedStatementBuilder(prepStatement);
    }
}
