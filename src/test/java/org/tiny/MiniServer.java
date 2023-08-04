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

    @PostConstruct
    private void postConstruct() {
        tableInfo.alterOrCreateTable();
        columnInfo.alterOrCreateTable();
        jdbcInfo.alterOrCreateTable();
    }

    public MiniServer() {
        System.out.println("CONSTRUCTOR");
    }

    public static final void main(String[] args) {
        SpringApplication.run(MiniServer.class, args);
    }
}
