/*
 * The MIT License
 *
 * Copyright 2017 Takahiro MURAKAMI.
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

import org.tiny.datawrapper.annotations.ClearfyTable;
import org.tiny.datawrapper.annotations.Comment;
import org.tiny.datawrapper.annotations.LogicalName;
import org.tiny.datawrapper.Column;
import org.tiny.datawrapper.CurrentTimestamp;
import org.tiny.datawrapper.ShortFlagZero;
import org.tiny.datawrapper.StampAtCreation;
import org.tiny.datawrapper.Table;

/**
 * テーブル定義を格納する
 *
 * @author Takahiro MURAKAMI
 */
@ClearfyTable("TABLE_INFO")
@LogicalName("テーブル情報")
@Comment("データテーブルの情報を格納したテーブル.")
public class TableInfo extends Table {

    public static final String MESSAGE_LOGICALNAME_LOST = "Annotation LogicalName is not defined.";

    @LogicalName("テーブル情報ID")
    public Column<Integer> TableInfoId;

    @LogicalName("作成日")
    public StampAtCreation Stamp;

    @LogicalName("更新日")
    public CurrentTimestamp Mdate;

    @LogicalName("無効フラグ")
    public ShortFlagZero Disable;

    @LogicalName("テーブルのクラス正準名")
    @Comment("テーブルの完全名称を格納する. このフィールドは、org.clearfy.datawrapper.TableInfo.TableClass")
    public Column<String> TableClassName;

    @LogicalName("物理名")
    @Comment("SQLサーバー上の名称を格納する. 例)TableInfoクラスの場合は、table_class")
    public Column<String> TablePhisicalName;

    @LogicalName("論理名")
    public Column<String> TableLogicalName;
    
    @LogicalName("説明")
    public Column<String> Description;

    @Override
    public void defineColumns() {
        
        this.TableInfoId.setPrimaryKey(true); // Mysqlのために訂正
        
        this.TableClassName.setLength(Column.SIZE_512)
            // .setPrimaryKey(true) // Mysqlのため訂正
            .setAllowNull(false);

        this.TableInfoId.setAllowNull(false)
            .setAutoIncrement(true);

        this.TablePhisicalName.setLength(Column.SIZE_1024)
            .setAllowNull(false);

        this.TableLogicalName.setLength(Column.SIZE_1024)
            .setAllowNull(false)
            .setDefault("'Annotation LogicalName is not defined.'");
        
        this.Description.setLength(Column.SIZE_2048);

    }

}
