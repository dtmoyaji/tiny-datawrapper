package org.tiny.datawrapper;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.tiny.datawrapper.h2db.ServerSwitch;

/**
 * サーバースイッチ抽象クラス
 *
 * @author Takahiro MURAKAMI
 */
public class AbstractServerSwitch implements IJdbcSupplier {

    protected String url;

    protected Jdbc cuurentJdbc;

    protected String JdbcDriverName;

    /**
     * ドライバの読み込み（指定したドライバを使用する）.
     *
     * @param name
     */
    public void loadDriver(String name) {
        try {
            Class.forName(name);
            this.JdbcDriverName = name;
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(ServerSwitch.class.getName())
                    .log(Level.SEVERE,
                            null, ex);
        }
    }

    /**
     * JDBCのURLを取得する.
     *
     * @param url
     */
    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public Jdbc getJdbc() {
        return this.cuurentJdbc;
    }

}