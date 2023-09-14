package org.tiny.datawrapper;

import java.io.Serializable;

/**
 * テーブル間の参照情報.
 * 
 * @author dtmoyaji
 */
public class RelationInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    private Class<? extends Table> Table;
    private Column Column;

    public RelationInfo() {
    }

    /**
     * テーブルの参照情報を生成する.
     * @param table 参照先テーブル
     * @param column 参照先カラム
     */
    public RelationInfo(Class<? extends Table> table, Column column) {
        this.Table = table;
        this.Column = column;
    }

    public void setTable(Class<? extends Table> table) {
        this.Table = table;
    }

    public void setColumnName(Column column) {
        this.Column = column;
    }

    public boolean equals(Class<? extends Table> table, Column column) {
        if (table.equals(Table) && Column.getName().equals(column.getName())) {
            return true;
        }

        return false;
    }

    public Class<? extends org.tiny.datawrapper.Table> getTableClass() {
        return this.Table;
    }
    
    public Column getColumn(){
        return this.Column;
    }

}
