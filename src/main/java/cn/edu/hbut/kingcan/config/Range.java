package cn.edu.hbut.kingcan.config;

import scala.Serializable;

public class Range implements Serializable{
    private static final long serialVersionUID = 2249339944654413131L;
    private double min=0;
    private double max=255;
    public Range(){
    }
    public Range(double min,double max){
        this.min=min;
        this.max=max;
    }
    public double getMin(){
        return min;
    }
    public double getMax(){
        return max;
    }
    public void setMin(double min){
        this.min=min;
    }
    public void setMax(double max){
        this.max=max;
    }
}

