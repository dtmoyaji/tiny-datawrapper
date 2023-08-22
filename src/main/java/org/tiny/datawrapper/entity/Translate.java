package org.tiny.datawrapper.entity;

import org.tiny.datawrapper.Column;
import org.tiny.datawrapper.CurrentTimestamp;
import org.tiny.datawrapper.ShortFlagZero;
import org.tiny.datawrapper.StampAtCreation;
import org.tiny.datawrapper.Table;
import org.tiny.datawrapper.annotations.Comment;
import org.tiny.datawrapper.annotations.LogicalName;
import org.tiny.datawrapper.annotations.TinyTable;

/**
 * 翻訳
 *
 * @author dtmoyaji
 */
@TinyTable("TRANSLATE")
@LogicalName("翻訳")
@Comment("言語別にテキスト文を格納する.")
public class Translate extends Table {

    public Column<String> Language;
    public Column<String> TranslateKey;
    public StampAtCreation Stamp;
    public CurrentTimestamp Mdate;
    public ShortFlagZero Disable;
    public Column<String> TranslateData;

    @Override
    public void defineColumns() {
        this.Language.setAllowNull(false)
            .setLength(Column.SIZE_32)
            .setPrimaryKey(true);

        this.TranslateKey.setAllowNull(false)
            .setLength(Column.SIZE_512)
            .setPrimaryKey(true);

        this.TranslateData.setAllowNull(false)
            .setLength(Column.SIZE_1024);

    }

}
