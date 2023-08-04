/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.tiny.datawrapper;

/**
 * limitとoffsetを扱うためのCondition
 *
 * @author bythe
 */
public class ConditionForRegion<T> extends Condition<T>{
    
    public static final int LIMIT = 200;
    public static final int OFFSET = 201;
    
    public ConditionForRegion(int operation, Object value) {
        super(operation, value);
    }
}
