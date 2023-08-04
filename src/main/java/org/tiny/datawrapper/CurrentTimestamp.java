package org.tiny.datawrapper;

import java.sql.Timestamp;

/**
 * 日付時刻型
 * このクラスは初期状態ではMD5の計算に使用されないので、日付を含める必要がある場合は
 * 継承クラス側で明示的に含むことを宣言する必要がある。
 *
 * @author dtmoyaji
 */
public class CurrentTimestamp extends Column<Timestamp> {

    public CurrentTimestamp() {
        super();

        this.setAllowNull(false)
            .setDefault(Column.DEFAULT_TIMESTAMP)
            .setInculdeMd5(false);
    }

}
