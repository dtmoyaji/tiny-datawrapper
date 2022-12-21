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
package org.tiny.datawrapper.entity;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.tiny.datawrapper.annotations.ClearfyTable;
import org.tiny.datawrapper.annotations.Comment;
import org.tiny.datawrapper.annotations.LogicalName;
import org.tiny.datawrapper.Column;
import org.tiny.datawrapper.Table;

/**
 *
 * @author Takahiro MURAKAMI <daianji@gmail.com>
 */
@ClearfyTable("KEY_VALUE_STORAGE")
@LogicalName("値保存用")
@Comment("キーと値をセットで保管する")
public class KeyValueStorage extends Table {

    public Column<String> StorageKey;

    public Column<Timestamp> Stamp;

    public Column<Timestamp> Mdate;

    public Column<Short> Disable;

    public Column<String> Value;

    @Override
    public void defineColumns() {
        StorageKey.setPrimaryKey(true)
                .setLength(Column.SIZE_512)
                .setAllowNull(false);

        Stamp.setDefault("CURRENT_TIMESTAMP")
                .setAllowNull(false);

        Mdate.setDefault("CURRENT_TIMESTAMP")
                .setAllowNull(false);

        Disable.setDefault("0")
                .setAllowNull(false);

        Value.setLength(Column.SIZE_1024)
                .setAllowNull(false);

    }

    public String getParameter(String key) {

        String rvalue = "";
        try {
            this.selectAllColumn();
            ResultSet rs = this.select(this.StorageKey.sameValueOf(key),
                    this.Value.setSelectable(true)
            );
            if (rs.next()) {
                rvalue = this.Value.of(rs);
            }
        } catch (SQLException ex) {
            Logger.getLogger(KeyValueStorage.class.getName()).log(Level.SEVERE, null, ex);
        }
        return rvalue;
    }

    public void setParameter(String key, String value) {
        this.merge(this.StorageKey.setValue(key),
                this.Value.setValue(value)
        );
    }

}
