/*
 * The MIT License
 *
 * Copyright 2016 takahiro.
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

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.tiny.datawrapper.entity.ColumnInfo;
import org.tiny.datawrapper.entity.TableInfo;

/**
 * データの取得、登録、更新を行うクラス.
 *
 * @author Takahiro MURAKAMI
 */
@Controller("Jdbc")
public class Jdbc implements Serializable, IJdbcSupplier, IDbSwitch {

    private static final String URL_BASE = "jdbc:h2:tcp:localhost:%s//%s";

    public static final int SERVER_TYPE_UNKNOWN = -1;

    public static final int SERVER_TYPE_H2DB = 0;

    public static final int SERVER_TYPE_MYSQL = 1;

    public static final int SERVER_TYPE_PGSQL = 2;

    /**
     * SQL実行結果の表示モード： 何もしない.
     */
    public static final int LOGGING_MODE_SILENT = 0;

    /**
     * SQL実行結果の表示モード： 構文と結果を標準出力.
     */
    public static final int LOGGING_MODE_STDOUT = 1;

    private static final long serialVersionUID = 1L;

    @Value("${tiny.db.port:9090}")
    private int port;

    @Value("${tiny.db.driver:org.h2.Driver}")
    private String driver;

    @Value("${tiny.db.url:tiny}")
    private String url;

    @Value("${tiny.db.user:sa}")
    private String user;

    @Value("${tiny.db.password:password}")
    private String password;

    private int serverType = Jdbc.SERVER_TYPE_H2DB;

    private int loggingMode = LOGGING_MODE_SILENT;

    private Connection connection;

    private final ArrayList<String> tableEntryCache;

    private final ArrayList<String> columnEntryCache;

    /**
     * コンストラクタ.
     *
     */
    public Jdbc() {
        this.tableEntryCache = new ArrayList<>();
        this.columnEntryCache = new ArrayList<>();
    }

    @Override
    public void off() {
    }

    @Override
    public Jdbc getJdbc() {
        return this;
    }

    /**
     * SQL実行結果のログモードを登録
     *
     * @param mode
     */
    public void setLoggingMode(int mode) {
        this.loggingMode = mode;
    }

    /**
     * SQL実行時のログモードを取得する.
     *
     * この値を参照して、結果の出力を切り替える.
     *
     * @return
     */
    public int getLoggingMode() {
        return this.loggingMode;
    }

    public int getServerType() {
        this.setUrl(this.url);
        return this.serverType;
    }

    /**
     * 認証情報の登録
     *
     * @param user ユーザー名
     * @param pass パスワード
     */
    public final void setAuthInfo(String user, String pass) {
        this.setUser(user);
        this.setPassword(pass);
    }

    /**
     * JDBCのURLを登録する
     *
     * @param url
     */
    public final void setUrl(String url) {
        this.url = url;
        if (this.getUrl().contains("jdbc:h2")) {
            this.serverType = Jdbc.SERVER_TYPE_H2DB;
        } else if (this.getUrl().contains("jdbc:mysql")) {
            this.serverType = Jdbc.SERVER_TYPE_MYSQL;
        } else if (this.getUrl().contains("jdbc:postgresql")) {
            this.serverType = Jdbc.SERVER_TYPE_PGSQL;
        } else {
            this.serverType = Jdbc.SERVER_TYPE_UNKNOWN;
        }
    }

    @Override
    public final String getUrl() {
        String rvalue = this.url;
        if (!url.contains("jdbc:")) {
            try {
                File f = new File(this.url);
                String path = f.getCanonicalPath();
                if (path.contains(":")) {
                    path = path.substring(path.indexOf(":") + 2);
                }
                rvalue = String.format(Jdbc.URL_BASE, this.getPort(), path);
            } catch (IOException ex) {
                Logger.getLogger(Jdbc.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return rvalue;
    }

    @Override
    public void setPort(String port) {
        this.port = Integer.valueOf(port);
    }

    @Override
    public int getPort() {
        return this.port;
    }

    /**
     * データを取得する.
     *
     * @param cmd Select文
     *
     * @return 選択結果
     */
    public ResultSet select(String cmd) {
        this.sqlCommandLogging(cmd);
        ResultSet rvalue = null;
        try {
            Connection con = this.getConnection();
            Statement stmt = con.createStatement();
            rvalue = stmt.executeQuery(cmd);
        } catch (SQLException ex) {
            Logger.getLogger(Jdbc.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
        return rvalue;
    }

    /**
     * SQLを実行する.
     *
     * @param cmd SQL構文
     *
     * @return 成功:true 失敗:false
     */
    public boolean execute(String cmd) {
        this.sqlCommandLogging(cmd);
        boolean rvalue = false;
        try (Statement stmt = this.getConnection().createStatement()) {
            rvalue = stmt.execute(cmd);
        } catch (SQLException ex) {
            Logger.getLogger(Jdbc.class
                    .getName()).log(Level.SEVERE, cmd, ex);
        }
        return rvalue;
    }

    /**
     * コネクションを取得する
     *
     * @return
     */
    public Connection getConnection() {
        Connection rvalue = getConnection(this.getUrl(), this.getUser(), this.getPassword());
        return rvalue;
    }
    
    /**
     * テーブル情報、カラム情報を格納するテーブルを生成する。
     */
    public void createInfoTables(){
        if(this.getConnection()!=null){
            TableInfo tableInfo = new TableInfo();
            tableInfo.alterOrCreateTable(this);
            ColumnInfo columnInfo = new ColumnInfo();
            columnInfo.alterOrCreateTable(this);
        }
    }

    /**
     * コネクションを取得する. ついでにテーブルとカラムの名称をJdbc内のキャッシュにロードする
     *
     * @param jdbcUrl JDBC URL
     * @param user データベースユーザ
     * @param password パスワード
     *
     * @return コネクション
     */
    public Connection getConnection(String jdbcUrl, String user, String password) {
        try {
            if (this.connection == null) {
                this.connection = DriverManager.getConnection(jdbcUrl, user, password);

                ResultSet existance = this.connection
                        .getMetaData()
                        .getTables(this.connection.getCatalog(), null, "%", new String[]{"TABLE"});

                while (existance.next()) {
                    String tablename = existance.getString("TABLE_NAME");
                    tablename = NameDescriptor.toJavaName(tablename);
                    tablename = NameDescriptor.toSqlName(tablename, this.getServerType());
                    this.tableEntryCache.add(tablename);
                }
                existance.close();

                existance = this.connection.getMetaData().getColumns(null, null, null, "%");
                while (existance.next()) {
                    String tablename = existance.getString("TABLE_NAME");
                    tablename = NameDescriptor.toJavaName(tablename);
                    tablename = NameDescriptor.toSqlName(tablename, this.getServerType());

                    String columnName = existance.getString("COLUMN_NAME");
                    columnName = NameDescriptor.toJavaName(columnName);
                    columnName = NameDescriptor.toSqlName(columnName, this.getServerType());
                    this.registColumnEntryCache(tablename, columnName);
                }
                existance.close();

            }
        } catch (SQLException ex) {
            Logger.getLogger(Jdbc.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
        return this.connection;
    }

    /**
     * テーブルがサーバにあるかどうかを確認する.
     *
     * @param name
     *
     * @return
     */
    public boolean isExistTable(String name) {

        boolean rvalue = this.tableEntryCache.contains(name);
        if (rvalue) {
            return rvalue;
        }

        try {
            TableInfo tableInfo = new TableInfo();
            tableInfo.setJdbc(this);
            if (tableInfo.isNameEquals(name)) {
                Connection con = this.getConnection();
                String catalog = con.getCatalog();
                DatabaseMetaData meta = this.getConnection().getMetaData();
                String queryName = name.replaceAll("\"", "").replaceAll("`", "");
                ResultSet rs = meta.getTables(catalog, null, queryName,
                        new String[]{"TABLE"});
                if (rs.next()) {
                    rvalue = true;
                    this.registTableEntryCache(name);
                }
                rs.close();
            } else {
                int count = tableInfo.getCount(tableInfo.TablePhisicalName.sameValueOf(name));
                if (count > 0) {
                    rvalue = true;
                }
            }
        } catch (SQLException | TinyDatabaseException ex) {
            Logger.getLogger(Jdbc.class
                    .getName()).log(Level.SEVERE, null, ex);
        }

        return rvalue;
    }

    /**
     * SQLコマンドのログ処理
     *
     * @param cmd
     */
    private void sqlCommandLogging(String cmd) {
        switch (this.getLoggingMode()) {
            case Jdbc.LOGGING_MODE_SILENT:
                break;
            case Jdbc.LOGGING_MODE_STDOUT:
                System.out.println();
                System.out.println(cmd);
                System.out.println();
                break;
        }
    }

    public DatabaseMetaData getMetaData() {
        DatabaseMetaData rvalue = null;
        try {
            rvalue = this.getConnection().getMetaData();
        } catch (SQLException ex) {
            Logger.getLogger(Jdbc.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
        return rvalue;

    }

    public void close() {
        if (this.connection != null) {
            try {
                if (!this.connection.isClosed()) {
                    this.connection.close();
                }
            } catch (SQLException ex) {
                Logger.getLogger(Jdbc.class
                        .getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void registTableEntryCache(String tableName) {
        if (!this.tableEntryCache.contains(tableName)) {
            this.tableEntryCache.add(tableName);
        }
    }

    public void registColumnEntryCache(String tableName, String columnName) {
        String pushName = tableName + "/" + columnName;
        if (!this.hasColumnCacheEntry(tableName, columnName)) {
            this.columnEntryCache.add(pushName);
        }
    }

    public boolean hasColumnCacheEntry(String tableName, String columnName) {
        String pushName = tableName + "/" + columnName;
        return this.columnEntryCache.contains(pushName);
    }

    /**
     * @return the driver
     */
    public String getDriver() {
        return driver;
    }

    /**
     * @param driver the driver to set
     */
    public void setDriver(String driver) {
        this.driver = driver;
        try {
            Class.forName(this.driver);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(Jdbc.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * @return the user
     */
    public String getUser() {
        return user;
    }

    /**
     * @param user the user to set
     */
    public void setUser(String user) {
        this.user = user;
    }

    /**
     * @return the pass
     */
    public String getPassword() {
        return this.password;
    }

    /**
     * @param pass the pass to set
     */
    public void setPassword(String pass) {
        this.password = pass;
    }

}
