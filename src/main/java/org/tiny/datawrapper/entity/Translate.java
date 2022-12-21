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
