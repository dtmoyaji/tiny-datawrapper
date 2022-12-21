/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.tiny.datawrapper;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;

/**
 * 主にSQL発行時に使用する、カラムの状態を格納するクラス
 *
 * @author Takahiro MURAKAMI
 * @param <T>
 */
public class Condition<T> {

    public static final int SELECTABLE = -1;

    public static final int RELATION_PATH = -2;

    public static final int EQUALS = 0;

    public static final int NOT_EQUALS = 1;

    public static final int IS_NULL = 2;

    public static final int LIKE = 3;

    public static final int GREATER = 4;

    public static final int LOWER = 5;

    public static final int GREATER_EQUAL = 6;

    public static final int LOWER_EQUAL = 7;
    
    public static final int IS_NOT_NULL = 8;

    protected Column<T> column;
    protected int operation;
    protected Object Value;

    public Condition(Column<T> col, int operation, T value) {
        this.column = col;
        this.operation = operation;
        this.Value = value;
    }

    public Condition(Column<T> col, int operation) {
        this.column = col;
        this.operation = operation;
    }

    /**
     * 対象とするカラムでwhere句の表現を取得する。
     *
     * @return where句
     */
    public String getWhere() {
        String rvalue = "%s %s %s";
        String operator = "";
        switch (operation) {
            case EQUALS:
                operator = "=";
                break;
            case NOT_EQUALS:
                operator = "<>";
                break;
            case GREATER:
                operator = ">";
                break;
            case LOWER:
                operator = "<";
                break;
            case GREATER_EQUAL:
                operator = ">=";
                break;
            case LOWER_EQUAL:
                operator = "<=";
                break;
            case IS_NULL:
                operator = "is";
                this.Value = "null";
                break;
            case IS_NOT_NULL:
                operator = "is not";
                this.Value = "null";
                break;
            case LIKE:
                operator = "like";
                break;
            case SELECTABLE:
                this.setSelectable(true);
                return "";
            case RELATION_PATH:
                return "";
        }

        String value = "";
        if (this.Value instanceof String) {
            value = (String) this.Value;
            if (this.column.getType().equals(String.class.getSimpleName())
                || this.column.getType().equals(char[].class.getTypeName())) {
                if (value.contains("'")) {
                    value = value.replaceAll("'", "''");
                    value = String.format("'%s'", value);
                } else {
                    value = String.format("'%s'", value);
                }
            }
        } else if (this.Value instanceof Timestamp) {
            value = ((Timestamp) this.Value).toLocalDateTime()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            if (value.contains("'")) {
                value = value.replaceAll("'", "''");
                value = String.format("'%s'", value);
            }
            if (!value.contains("'")) {
                value = String.format("'%s'", value);
            }
        }else if (this.Value instanceof Date ||
                this.Value instanceof Time){
            value = "'" + this.Value.toString() + "'";
        } else if (this.Value instanceof Column) {
            value = ((Column) this.Value).getFullName();
        } else {
            value = String.valueOf(this.Value);
        }

        rvalue = String.format(
            rvalue,
            this.column.getFullName(),
            operator,
            value);

        return rvalue;
    }

    public Column<T> getColumn() {
        return this.column;
    }

    public Condition<T> setSelectable(boolean sel) {
        this.column.setSelectable(sel);
        return this;
    }

    public Condition<T> setColumnAlias(String labelText) {
        this.column.setAlias(labelText);
        return this;
    }

}
