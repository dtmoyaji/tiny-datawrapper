package org.tiny.datawrapper;

import java.sql.Timestamp;

/**
 * マージ文を作成する
 *
 * @author dtmoyaji
 */
public class MergeDescriptor {

    public static String getMergeSentence(int ServerType, String tableName, Column... columns) {
        String cmd = "";
        switch (ServerType) {
            case Jdbc.SERVER_TYPE_H2DB:
                cmd = MergeDescriptor.getSimpleMergeSentence(tableName, columns);
                break;
            case Jdbc.SERVER_TYPE_MYSQL:
                cmd = MergeDescriptor.getInsertUpateSentence(tableName, columns);
                break;
        }

        return cmd;
    }

    private static String getInsertUpdateOnConflictSentence(String tableName, Column... columns) {
        String cmd = "INSERT INTO %s (%s) VALUES (%s)\n"
                + "ON CONFLICT (%s) \n"
                + "DO UPDATE SET %s ";
        String fields = "";
        String values = "";
        String keys = "";
        String updates = "";

        for (Column column : columns) {
            if (column.isMargeTarget()) {
                String updatervalue = "";
                fields += "," + column.getName();
                if (column.getValue() == null
                        || column.getValue().toString().length() < 1) {
                    if (column.isNullable()) {
                        values += ", null";
                        updatervalue = "null";
                    } else {
                        if (column.getType().equals(Integer.class.getSimpleName())
                                && column.isAutoIncrement()) {
                            values += ", null";
                            updatervalue = "null";
                        } else if (column.getType().equals(Timestamp.class.getSimpleName())
                                && column.getDefault().equals(Column.DEFAULT_TIMESTAMP)) {
                            updatervalue = Column.DEFAULT_TIMESTAMP;
                            values += ", " + Column.DEFAULT_TIMESTAMP;

                        } else if (column.getDefault().length() > 0) {
                            updatervalue = "'" + column.getDefault() + "'";
                            values += ", " + updatervalue;

                        } else {
                            updatervalue = "''";
                            values += ", ''";
                        }
                    }
                } else {
                    if (column.getType().equals(char[].class.getSimpleName())) {
                        char[] buf = (char[]) column.getValue();
                        String value = String.valueOf(buf);
                        updatervalue = "'" + value + "'";
                        values += ", " + updatervalue;
                    } else {
                        updatervalue = "'" + String.valueOf(column.getValue()) + "'";
                        values += ", " + updatervalue;
                    }
                }
                if (!column.isPrimaryKey()) {
                    String updaterfomat = ", " + column.getName() + " = ";
                    updaterfomat += updatervalue;
                    updates += updaterfomat;
                } else {
                    keys = ", " + column.getName();
                }
            }
        }

        if (updates.length() < 1) {
            String name = columns[0].getName();
            String value = columns[0].getName();
            updates = String.format(" %s = %s", name, value);
        }

        fields = fields.substring(1);
        values = values.substring(1);
        keys = keys.substring(1);
        updates = updates.substring(1);
        cmd = String.format(cmd, tableName, fields, values, keys, updates);
        return cmd;
    }

    private static String getInsertUpateSentence(String tableName, Column... columns) {
        String cmd = "insert into %s (%s) values (%s) on duplicate key update %s";
        String fields = "";
        String values = "";
        String updates = "";

        for (Column column : columns) {
            if (column.isMargeTarget() && !column.getClass().getName().equals(StampAtCreation.class.getName())) {
                String updatervalue = "";
                fields += "," + column.getName();
                if (column.getValue() == null
                        || column.getValue().toString().length() < 1) {
                    if (column.isNullable()) {
                        values += ", null";
                        updatervalue = "null";
                    } else {
                        if (column.getType().equals(Integer.class.getSimpleName())
                                && column.isAutoIncrement()) {
                            values += ", null";
                            updatervalue = "null";
                        } else if (column.getType().equals(Timestamp.class.getSimpleName())
                                && column.getDefault().equals(Column.DEFAULT_TIMESTAMP)) {
                            updatervalue = Column.DEFAULT_TIMESTAMP;
                            values += ", " + Column.DEFAULT_TIMESTAMP;

                        } else if (column.getDefault().length() > 0) {
                            updatervalue = "'" + column.getDefault() + "'";
                            values += ", " + updatervalue;

                        } else {
                            updatervalue = "''";
                            values += ", ''";
                        }
                    }
                } else {
                    if (column.getType().equals(char[].class.getSimpleName())) {
                        char[] buf = (char[]) column.getValue();
                        String value = String.valueOf(buf);
                        updatervalue = "'" + value + "'";
                        values += ", " + updatervalue;
                    } else {
                        updatervalue = "'" + String.valueOf(column.getValue()) + "'";
                        values += ", " + updatervalue;
                    }
                }
                if (!column.isPrimaryKey()) {
                    String updaterfomat = ", " + column.getName() + " = ";
                    updaterfomat += updatervalue;
                    updates += updaterfomat;
                }
            }
        }

        if (updates.length() < 1) {
            String name = columns[0].getName();
            String value = columns[0].getName();
            updates = String.format(" %s = %s", name, value);
        }

        fields = fields.substring(1);
        values = values.substring(1);
        updates = updates.substring(1);
        cmd = String.format(cmd, tableName, fields, values, updates);
        return cmd;
    }

    private static String getSimpleMergeSentence(String tableName, Column... columns) {

        String cmd = "merge into %s (%s) values (%s)";

        String fields = "";
        String values = "";

        for (Column column : columns) {
            if (column.isMargeTarget() && !(column instanceof StampAtCreation)) {
                fields += "," + column.getName();
                if (column.getValue() == null
                        || column.getValue().toString().length() < 1) {
                    if (column.isNullable()) {
                        values += ", null";
                    } else {
                        if (column.getType().equals(Integer.class.getSimpleName())
                                && column.isAutoIncrement()) {
                            values += ", null";
                        } else if ((column.getType().equals(Timestamp.class.getSimpleName()) 
                                && column.getDefault().equals(Column.DEFAULT_TIMESTAMP))
                                || column instanceof CurrentTimestamp
                                ) {
                            values += "," + Column.DEFAULT_TIMESTAMP;

                        } else if (column.getDefault().length() > 0) {
                            values += ", '" + column.getDefault() + "'";

                        } else {
                            values += ", ''";
                        }
                    }
                } else {
                    if (column.getType().equals(char[].class.getSimpleName())) {
                        char[] buf = (char[]) column.getValue();
                        String value = String.valueOf(buf);
                        values += ",'" + value + "'";
                    } else {
                        values += ",'" + String.valueOf(column.getValue()) + "'";
                    }
                }
            }
        }

        fields = fields.substring(1);
        values = values.substring(1);
        cmd = String.format(cmd, tableName, fields, values);

        return cmd;
    }

}
