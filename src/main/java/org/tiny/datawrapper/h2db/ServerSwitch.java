package org.tiny.datawrapper.h2db;

import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.h2.tools.Server;
import org.springframework.stereotype.Controller;
import org.tiny.datawrapper.AbstractServerSwitch;
import org.tiny.datawrapper.Jdbc;
 
/**
 * サーバーの起動停止を扱うクラス.
 *
 * @author Takahiro MURAKAMI
 */
@Controller("ServerSwitch")
public class ServerSwitch extends AbstractServerSwitch {
 
    /**
     * JDBCDriverのクラス名.
     */
    protected static String JDBC_DRIVER_MANAGER = "org.h2.Driver";
 
    /**
     * データベースの停止中.
     */
    public static final int DATABASE_STOPED = 0;
 
    /**
     * データベース実行中.
     */
    public static final int DATABASE_RUNNING = 1;
 
    /**
     * データベースモード：TCP.
     */
    public static final String DATABASE_MODE_TCP = "tcp";
 
    /**
     * データベースモード: HTTP.
     */
    public static final String DATABASE_MODE_HTTP = "http";
 
    /**
     * データベースの状態.
     */
    protected int dbStatus = ServerSwitch.DATABASE_STOPED;
 
    /**
     * データベースHTTP.
     */
    protected Server httpDbServer;
 
    /**
     * データベースTCP.
     */
    protected Server tcpDbServer;
 
    /**
     * データベースのTCPポート.
     */
    protected int jdbcTcpPort = 0;
 
    /**
     * データベースのHTTPポート.
     */
    protected int jdbcHttpPort = 0;
 
    /**
     * ドライバの読み込み（規定のドライバを使用する).
     */
    public void loadDriver() {
        this.loadDriver(ServerSwitch.JDBC_DRIVER_MANAGER);
    }
 
 
    @Override
    public Jdbc getJdbc() {
        Jdbc rvalue = new JdbcH2(this.url);
        this.cuurentJdbc = rvalue;
        return rvalue;
    }
 
    /**
     * サーバーポート番号.
     *
     * @param tcpPort
     * @param httpPort
     */
    public void setServerPorts(int tcpPort, int httpPort) {
        this.jdbcHttpPort = httpPort;
        this.jdbcTcpPort = tcpPort;
    }
 
    /**
     * サーバーの起動.
     */
    public void on() {
        //DBサーバーのhttp起動
        try {
            if (this.jdbcHttpPort > 0) {
                this.httpDbServer = Server
                        .createWebServer(
                                "-webPort",
                                String.valueOf(this.jdbcHttpPort),
                                "-webAllowOthers"
                        );
                if (!this.httpDbServer.isRunning(true)) {
                    this.httpDbServer.start();
                }
            }
 
            //DBサーバーのtcp起動
            if (this.jdbcTcpPort > 0) {
                this.tcpDbServer = Server
                        .createTcpServer(
                          "-ifNotExists",
                                  "-tcpPort",
                                String.valueOf(this.jdbcTcpPort),
                                "-tcpAllowOthers"
                        );
                if (!this.tcpDbServer.isRunning(true)) {
                    this.tcpDbServer.start();
                }
            }
 
            //DBを稼働中にする。
            this.dbStatus = ServerSwitch.DATABASE_RUNNING;
        } catch (SQLException ex) {
            Logger.getLogger(ServerSwitch.class.getName())
                    .log(Level.SEVERE,
                            null, ex);
        }
    }
 
    /**
     * サーバーの状態を取得する.
     */
    public int getStatus() {
        return this.dbStatus;
    }
 
    /**
     * サーバーを停止する。
     */
    public void off() {
        if (this.httpDbServer != null) {
            this.httpDbServer.stop();
        }
 
        if (this.tcpDbServer != null) {
            this.tcpDbServer.stop();
        }
 
        this.dbStatus = ServerSwitch.DATABASE_STOPED;
    }
 
    /**
     * データベースの圧縮
     */
    public void conpact() {
        JdbcH2 jdbc = (JdbcH2) this.cuurentJdbc;
        jdbc.shutdownConpact();
        this.off();
        this.on();
    }
 
}