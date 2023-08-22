package org.tiny.datawrapper.entity;

import org.tiny.datawrapper.Column;
import org.tiny.datawrapper.CurrentTimestamp;
import org.tiny.datawrapper.ShortFlagZero;
import org.tiny.datawrapper.StampAtCreation;
import org.tiny.datawrapper.Table;
import org.tiny.datawrapper.TinyDatabaseException;
import org.tiny.datawrapper.annotations.Comment;
import org.tiny.datawrapper.annotations.LogicalName;
import org.tiny.datawrapper.annotations.TinyTable;

/**
 *
 * @author dtmoyaji
 */
@TinyTable("COLUMN_INFO")
@LogicalName("カラム情報")
@Comment("データテーブル内のカラムの情報を格納したテーブル")
public class ColumnInfo extends Table {

    @LogicalName("カラムのクラス正準名")
    @Comment("このクラスのこのフィールドの場合は、org.clearfy.datawrapper.ColumnInfo.ColumnClassNameが記録される.")
    public Column<String> ColumnClassName;

    @LogicalName("作成日")
    public StampAtCreation Stamp;

    @LogicalName("更新日")
    public CurrentTimestamp Mdate;

    @LogicalName("無効フラグ")
    public ShortFlagZero Disable;

    @LogicalName("テーブル情報ID")
    @Comment("どのテーブルに属しているかを示す値")
    public Column<Integer> TableInfoId;

    @LogicalName("位置")
    @Comment("カラムの並び順")
    public Column<Integer> Ordinal;

    @LogicalName("カラム物理名")
    @Comment("DBサーバー上のカラム名．このカラムの場合は、column_phisical_nameが記録される.")
    public Column<String> ColumnPhisicalName;

    @LogicalName("カラム論理名")
    public Column<String> ColumnLogicalName;

    @LogicalName("型")
    public Column<String> ColumnType;

    @LogicalName("長さ")
    public Column<String> ColumnLength;

    @LogicalName("NULL許可")
    public Column<Short> AllowNullable;

    @LogicalName("説明")
    public Column<String> Description;

    @Override
    public void defineColumns() throws TinyDatabaseException {
        this.ColumnClassName
            .setAllowNull(false)
            .setLength(Column.SIZE_1024)
            .setPrimaryKey(true);

        this.TableInfoId
            .setAllowNull(false)
            .addRelationWith(TableInfo.class);

        this.Ordinal
            .setAllowNull(false)
            .setDefault("-1");

        this.ColumnPhisicalName
            .setAllowNull(false)
            .setLength(Column.SIZE_1024);

        this.ColumnLogicalName
            .setAllowNull(false)
            .setLength(Column.SIZE_1024);

        this.ColumnType
            .setAllowNull(false)
            .setLength(Column.SIZE_128);

        this.ColumnLength
            .setAllowNull(true)
            .setLength(Column.SIZE_8);

        this.AllowNullable
            .setAllowNull(false)
            .setDefault("0");

        this.Description
            .setAllowNull(true)
            .setLength(Column.SIZE_2048);

    }

}
