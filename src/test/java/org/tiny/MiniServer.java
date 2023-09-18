package org.tiny;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.tiny.datawrapper.Jdbc;
import org.tiny.datawrapper.entity.ColumnInfo;
import org.tiny.datawrapper.entity.JdbcInfo;
import org.tiny.datawrapper.entity.TableInfo;
import org.tiny.datawrapper.h2db.ServerSwitch;

/**
 * 単体で起動するミニサーバー
 *
 * @author Takahiro MURAKAMI
 */
@ComponentScan
@Configuration
@SpringBootApplication
@EnableAutoConfiguration
public class MiniServer {

    @Autowired
    @Qualifier("Jdbc")
    private Jdbc jdbc;
    
    @Autowired
    @Qualifier("TABLE_INFO")
    private TableInfo tableInfo;
    
    @Autowired
    @Qualifier("COLUMN_INFO")
    private ColumnInfo columnInfo;

    @Autowired
    @Qualifier("JDBC_INFO")
    private JdbcInfo jdbcInfo;

    @Autowired
    private ServerSwitch sswitch;

    @PostConstruct
    private void postConstruct() {
        sswitch.setServerPorts(jdbc.getPort(), -1);
        sswitch.setUrl(jdbc.getUrl());
        sswitch.on();
        System.out.println(this.jdbc.getUrl()); 
        tableInfo.alterOrCreateTable();
        columnInfo.alterOrCreateTable();
        jdbcInfo.alterOrCreateTable();
    }

    public MiniServer() {
        System.out.println("CONSTRUCTOR");
    }

    public void onServer(){
        this.sswitch.on();
    }

    public void offServer(){
        this.sswitch.off();
    }

    public static final void main(String[] args) {
        SpringApplication.run(MiniServer.class, args);
    }

}
