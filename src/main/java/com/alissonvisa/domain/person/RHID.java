package com.alissonvisa.domain.person;

import com.alissonvisa.util.RandomUtil;

/**
 * Random Hex ID
 */
public class RHID {

    private RHID(){}

    public RHID(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }

    private String value;

    public static String randomHexID() {
        return RandomUtil.unique();
    }

    public static RHID valueOf(String value) {
        return new RHID(value);
    }
}
