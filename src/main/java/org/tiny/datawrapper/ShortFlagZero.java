package org.tiny.datawrapper;

/**
 * 主として有効無効を格納するカラム.
 * このクラスは初期状態ではMD5の計算に使用されないので、
 * 計算に使用する場合は明示的に継承クラスで宣言する必要がある.
 *
 * @author dtmoyaji
 */
public class ShortFlagZero extends Column<Short> {

    public ShortFlagZero() {
        super();

        this.setAllowNull(false)
            .setDefault("0")
            .setInculdeMd5(false);
    }

}
