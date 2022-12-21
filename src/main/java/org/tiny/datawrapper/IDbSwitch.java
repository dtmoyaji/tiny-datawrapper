package org.tiny.datawrapper;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import org.springframework.stereotype.Component;

/**
 *
 * @author Takahiro MURAKAMI
 */
@Component
public interface IDbSwitch extends IJdbcSupplier{

    String getDriver();

    String getPassword();

    int getPort();

    String getUrl();

    String getUser();

    void off();

    void setDriver(String driver);

    void setPassword(String pass);

    void setPort(String port);

    void setUrl(String url);

    void setUser(String user);
    
}
