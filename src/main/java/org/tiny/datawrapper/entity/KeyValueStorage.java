package org.tiny.datawrapper.entity;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.tiny.datawrapper.Column;
import org.tiny.datawrapper.Table;
import org.tiny.datawrapper.annotations.Comment;
import org.tiny.datawrapper.annotations.LogicalName;
import org.tiny.datawrapper.annotations.TinyTable;

/**
 *
 * @author dtmoyaji
 */
@TinyTable("KEY_VALUE_STORAGE")
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
