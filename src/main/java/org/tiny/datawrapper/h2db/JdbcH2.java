package org.tiny.datawrapper.h2db;

import org.tiny.datawrapper.Jdbc;

/**
 * Jdbcの機能にH2dbの特殊な実装を追加
 *
 * @author Takahiro MURAKAMI
 */
public class JdbcH2 extends Jdbc {

    public JdbcH2(String url) {
        super();
        this.setUrl(url);
    }

    /**
     * データベースの圧縮と停止
     */
    public void shutdownConpact() {
        this.execute("SHUTDOWN COMPACT");
    }

}