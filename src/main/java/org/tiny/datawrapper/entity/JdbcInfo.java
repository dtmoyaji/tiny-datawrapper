/*
 * The MIT License
 *
 * Copyright 2019 Takahiro MURAKAMI.
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
 *
 * @author Takahiro MURAKAMI
 */
@TinyTable("JDBC_INFO")
@LogicalName("JDBC情報")
@Comment("アプリケーションが利用するJDBC情報を格納するテーブル")
public class JdbcInfo extends Table {

    @LogicalName("JDBCURL")
    public Column<String> JdbcUrl;

    @LogicalName("作成日")
    public StampAtCreation Stamp;

    @LogicalName("更新日")
    public CurrentTimestamp Mdate;

    @LogicalName("無効フラグ")
    public ShortFlagZero Disable;

    @LogicalName("サーバー名")
    public Column<String> JdbcServerName;

    @LogicalName("JDBCドライバクラス")
    public Column<String> JdbcDriverClass;

    @LogicalName("ユーザー名")
    public Column<String> JdbcUser;

    @LogicalName("パスワード")
    public Column<String> JdbcPassword;

    @Override
    public void defineColumns() {

        this.JdbcServerName.setLength(Column.SIZE_512)
                .setAllowNull(false)
                .setPrimaryKey(true);

        this.JdbcDriverClass.setLength(Column.SIZE_1024)
                .setAllowNull(false);

        this.JdbcUrl.setLength(Column.SIZE_1024)
                .setPrimaryKey(true)
                .setAllowNull(false);

        this.JdbcUser.setLength(Column.SIZE_512)
                .setAllowNull(false);

        this.JdbcPassword.setLength(Column.SIZE_512);

    }
}
