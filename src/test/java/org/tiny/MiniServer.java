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
