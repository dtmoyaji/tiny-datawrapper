package org.tiny.datawrapper;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.stereotype.Component;

/**
 * 環境情報用モデル
 * @author dtmoyaji
 */
@Component("FileSystemInfo")
public class FileSystemInfo {

    public String getCurrentPath(){
        return this.getCurrentPath("./");
    }
    
    public String getCurrentPath(String fileName){
        File f = new File(fileName);
        String rvalue = null;
        try {
            rvalue = f.getCanonicalPath();
        } catch (IOException ex) {
            Logger.getLogger(FileSystemInfo.class.getName()).log(Level.SEVERE, null, ex);
        }
        return rvalue;
    }
}
