package github.BTEPlotSystem.core.database;

public abstract class SQL {
    private static final String VARCHAR = "varchar(";

    public static final String INT = "integer";
    public static final String DOUBLE = "double";
    public static final String LONG = "bigint";
    public static final String BOOL = "boolean";
    public static final String DATETIME = "datetime";

    public static String varchar(int length) {
        return VARCHAR + length + ')';
    }

    // TODO: Add date conversion

}
