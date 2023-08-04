package org.tiny.datawrapper;

import org.tiny.datawrapper.annotations.LogicalName;

/**
 *
 * @author dtmoyaji
 */
@LogicalName("テーブル更新記録")
public class TableUpdateLog extends Table {

    public static final short STATUS_CREATED = 1;
    public static final short SIZE_CHANGED = 2;
    public static final short TYPE_CHANGED = 3;

    public IncrementalKey TableUpdateLogId;

    public CurrentTimestamp Stamp;

    public Column<String> TableName;

    public CurrentTimestamp LastUpdate;

    public Column<Short> LastUpdateStatus;

    @Override
    public void defineColumns() {

        this.TableName.setAllowNull(false)
                .setLength(Column.SIZE_1024);
    }

}
