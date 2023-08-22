package org.tiny.datawrapper;

import org.springframework.stereotype.Component;

/**
 *
 * @author dtmoyaji
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
