package org.tiny.datawrapper;

/**
 * ビューを扱うクラス。
 * @author dtmoyaji
 */
public class View extends Table {

    public View() {
        super();
    }

    @Override
    public void alterOrCreateTable(Jdbc jdbc) {
            this.setJdbc(jdbc);
    }

    @Override
    public void defineColumns() {
    }
}
