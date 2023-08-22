package org.tiny.datawrapper;

/**
 *
 * @author dtmoyaji
 */
public class IncrementalKey extends Column<Integer> {

    public IncrementalKey() {
        super();

        this.setAllowNull(false)
                .setAutoIncrement(true)
                .setPrimaryKey(true)
                .setInculdeMd5(false); //サーバー登録時に発行する値はMD5に含めない。
    }

}
