package cn.edu.hbut.kingcan.optimization.algorithm.hro;

import java.util.Comparator;

// 比较器(保持系都是)
public  class MyComprator implements Comparator {
public int compare(Object arg0, Object arg1) {
        HroUnit t1 = (HroUnit) arg0;
        HroUnit t2 = (HroUnit) arg1;
        if (t1.getValue() < t2.getValue()) {
        return 1;
        } else if (t1.getValue() == t2.getValue()) {
        return 0;
        } else {
        return -1;
        }
        // return t1.getValue()<t2.getValue()? 1:-1;
        }
        }