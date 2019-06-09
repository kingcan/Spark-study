package cn.edu.hbut.kingcan.config.frame;

import java.io.Serializable;

public class Unit implements scala.Serializable{
    private static final long serialVersionUID = 775854412183714181L;
    protected int dim;//维度
    protected double[] position;//位置
    protected double value;
    public Unit(int dim){
        this.dim=dim;
        position=new double[dim];
        value=-Double.MAX_VALUE;
    }
    public void setPosition(double []position){
        for(int i=0;i<dim;i++){
            this.position[i]=position[i];
        }
    }
    public double[]getPosition(){
        return this.position;
    }
    public void setPosition(int location,double position){
        this.position[location]=position;
    }
    public double getPosition(int location){
        return this.position[location];
    }
    public void setValue(double value){
        this.value=value;
    }
    public double getValue(){
        return this.value;
    }
}
