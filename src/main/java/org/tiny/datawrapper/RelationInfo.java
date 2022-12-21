/*
 * The MIT License
 *
 * Copyright 2017 tmworks.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.tiny.datawrapper;

import java.io.Serializable;

/**
 * テーブル間の参照情報.
 * 
 * @author Takahiro MURAKAMI
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

    public Class<? extends org.tiny.datawrapper.Table> getTable() {
        return this.Table;
    }

}
