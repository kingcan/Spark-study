package cn.edu.hbut.kingcan.optimization.algorithm.pso;

import cn.edu.hbut.kingcan.config.frame.Unit;
import scala.Serializable;

public class PsoUnit extends Unit implements Serializable{
    private static final long serialVersionUID = 4849033805711552182L;
    private double[]velocity;
    private double[]posBest;

    public PsoUnit(int dim, double[] velocity, double[] posBest) {
        super(dim);
        this.velocity = velocity;
        this.posBest = posBest;
    }

    public PsoUnit(int dim){
        super(dim);
        velocity=new double[dim];
        posBest=new double[dim];
    }
    public void setVelocity(double[]velocity){
        for(int i=0;i<dim;i++){
            this.velocity[i]=velocity[i];
        }
    }
    public double[]getVelocity(){
        return velocity;
    }
    public void setVelocity(int location,double value){
        velocity[location]=value;
    }
    public double getVelocity(int location){
        return velocity[location];
    }
    public void setBestPosition(){
        for(int i=0;i<dim;i++){
            posBest[i]=position[i];
        }
    }
    public void setBestPosition(double[] pos){
        for(int i=0;i<dim;i++){
            posBest[i]=pos[i];
        }
    }
    public double[]getBestPosition(){
        return posBest;
    }
    public void setBestPosition(int location,double value){
        posBest[location]=value;
    }
    public double getBestPosition(int location){
        return posBest[location];
    }
}
