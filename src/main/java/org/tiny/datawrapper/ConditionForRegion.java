package org.tiny.datawrapper;

import java.io.Serializable;

/**
 * limitとoffsetを扱うためのCondition
 *
 * @author bythe
 * @param <T>
 */
public class ConditionForRegion<T> extends Condition<T> implements Serializable {

    public static final long serialVersionUID = -1L;

    public static final int LIMIT = 200;
    public static final int OFFSET = 201;

    public ConditionForRegion(int operation, Object value) {
        super(operation, value);
    }
}
