package cn.edu.hbut.kingcan.optimization.algorithm.hro;

import cn.edu.hbut.kingcan.config.frame.Unit;

public class HroUnit extends Unit {
    private int indexForSort;
    private int times;
    public HroUnit(int dim){
        super(dim);
        times=0;
    }
    public void setTimes(int times){
        this.times=times;
    }
    public int getTimes(){
        return this.times;
    }

    public int getIndexForSort() {
        return indexForSort;
    }

    public void setIndexForSort(int indexForSort) {
        this.indexForSort = indexForSort;
    }
}

