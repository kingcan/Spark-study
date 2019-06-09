package cn.edu.hbut.kingcan.config.frame;

import scala.Serializable;

import java.util.Random;

public abstract class Algorithm implements Serializable{
    private static final long serialVersionUID = 7054278936068372233L;
    private Random r=new Random();
    public      Function function;//算法的适应度函数接口
    public abstract void init();
    protected abstract void update();
    public abstract void iteration();
    public abstract void run();

    public abstract double getBestValue();
    public abstract double[] getBestPosition();
    public abstract double[]getWholeValue();
    public abstract void realise();

    //算法的适应度函数，若设置了则使用设置的，否则默认为0
    protected  double fitFunction(double[]pos){
        if(function!=null){
            return function.function(pos);
        }else{
            return 0;
        }
    }

    //设置算法的适应度函数
    public void setFunction(Function function){
        this.function=function;
    }

    //随机数
    public double randNumDouble(double min,double max){
        double i=r.nextDouble()*(max-min)+min;
        return i;
    }
    public double randGauss(double a,double b){
        return  (r.nextGaussian()*Math.sqrt(b)+a);
    }
    //随机数
    public int randNumInt(int min,int max){
        int i=(int) (r.nextInt(max-min+1)+min);
        return i;
    }
}