package org.tiny.datawrapper;

import java.sql.Timestamp;

/**
 * レコード生成日時を記録するためのカラム.
 * 
 * 明示的に操作をしない場合、レコード作成時に1回だけ書き込まれる。
 * 
 * @author bythe
 */
public class StampAtCreation extends Column<Timestamp>{
    
    public StampAtCreation(){
        this.setAllowNull(false)
                .setMargeTarget(false)
                .setInculdeMd5(false)
                .setDefault(Column.DEFAULT_TIMESTAMP);
    }
    
}
