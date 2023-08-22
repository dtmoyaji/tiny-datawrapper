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
 * @author dtmoyaji
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
    public Column<String> User;

    @LogicalName("パスワード")
    public Column<String> Password;

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

        this.User.setLength(Column.SIZE_512)
                .setAllowNull(false);

        this.Password.setLength(Column.SIZE_512);

    }
}
