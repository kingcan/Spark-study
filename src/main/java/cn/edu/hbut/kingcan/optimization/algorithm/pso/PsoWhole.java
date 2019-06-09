package cn.edu.hbut.kingcan.optimization.algorithm.pso;

import cn.edu.hbut.kingcan.config.Range;
import cn.edu.hbut.kingcan.config.frame.Algorithm;
import cn.edu.hbut.kingcan.config.frame.Unit;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import scala.Serializable;

import java.util.Random;

public class PsoWhole extends Algorithm implements Serializable{
    private static final long serialVersionUID = -2521468028127039495L;
    protected           int           dim;//维度
    protected           int           size;//种群数量
    protected           int           iter;//迭代数
    protected           Range[]       ranges;
      protected           PsoUnit[]     group;//种群
    protected           double[]      positionBest;//当前最优位置（posBest是个体历史最优）
    protected           double        valueBest = -Double.MAX_VALUE;//当前最优值
    protected           double        xMax =100;//最大范围
    protected           double        xMin=0;//最小范围
    protected           double        maxV;//最大速度
    protected           double        C1,C2,W;//学习因子与惯性系数
    protected           double[]      wholeValue;//每一代的最优值记录



    public PsoWhole()
    {
    }
    public PsoWhole(int dim, int size, int iter, Range[] ranges, PsoUnit[] group, double[] positionBest, double valueBest, double xMax, double xMin, double maxV, double c1, double c2, double w, double[] wholeValue) {
        this.dim = dim;
        this.size = size;
        this.iter = iter;
        this.ranges = ranges;
        this.group = group;
        this.positionBest = positionBest;
        this.valueBest = valueBest;
        this.xMax = xMax;
        this.xMin = xMin;
        this.maxV = maxV;
        C1 = c1;
        C2 = c2;
        W = w;
        this.wholeValue = wholeValue;
    }

    public PsoWhole(int dim, int size, int iter, double min, double max){
        this.dim=dim;
        this.size=size;
        this.iter=iter;
        this.xMax=max;
        this.xMin=min;
        this.ranges=new Range[dim];
        for(int i=0;i<dim;i++){
            this.ranges[i]=new Range(xMin,xMax);
        }
        this.group=new PsoUnit[size];
        this.positionBest=new double[dim];
        this.wholeValue=new double[iter];
        this.C1=this.C2=2;
        this.W=1;
        this.maxV=5.0d;
    }

    public PsoWhole(int dim,int size,int iter,Range[]ranges){
        this.dim=dim;
        this.size=size;
        this.iter=iter;
        this.ranges=new Range[dim];
        for(int i=0;i<dim;i++){
            if(i<ranges.length){
                this.ranges[i]=new Range(ranges[i].getMin(),ranges[i].getMax());
            }
        }
        this.group=new PsoUnit[size];
        this.positionBest=new double[dim];
        this.wholeValue=new double[iter];
        this.C1=this.C2=2;
        this.W=1;
        this.maxV=5.0d;
    }
    //初始化位置，速度。
    @Override
    public void init() {
        // TODO Auto-generated method stub
        for (int s = 0; s < size; s++){
            PsoUnit pu = new PsoUnit(dim);
            for (int d = 0; d < dim; d++){
                pu.setPosition(d, randNumDouble(ranges[d].getMin(), ranges[d].getMax()));
                pu.setVelocity(d, randNumDouble(-maxV, maxV));
            }
//            double value=fitFunction(pu.getPosition());//这一步应该并行化
//           pu.setValue(value);
            pu.setBestPosition();//将初始化的每一维元素赋值给position
            group[s] =pu ;
        }
    }


   @Override
    protected void update() {
        // TODO Auto-generated method stub
        for (int s = 0; s < size; s++){//并行化
            updateVelocity(s);
            updatePosition(s);
        }
    }
    @Override
    public void iteration() {
        // TODO Auto-generated method stub
        for (int i = 0; i < iter; i++){
            //惯性系数随着迭代次数越来越小。
			W=(1.0d-(double)(i)/(double)iter);
//			W=(1.0d-(double)(i%(iter/100))/(double)(iter/100));
//			W=Math.pow(0.5d, i/100)*Math.abs(Math.cos((i/iter)*Math.PI));


            update();
            wholeValue[i]=valueBest;
        }
    }

    //更新速度
    protected  void updateVelocity(int id){
        for (int d = 0; d < dim; d++){
            double V = 0;
            V = W*group[id].getVelocity(d) +
                    C1*randNumDouble(0, 1)*(group[id].getBestPosition(d)-group[id].getPosition(d)) +
                    C2*randNumDouble(0, 1)*(positionBest[d] - group[id].getPosition(d));
            if (V>maxV){
                V = maxV;
            }
            if (V < -maxV){
                V = -maxV;
            }
            group[id].setVelocity(d, V);
        }
    }

    //更新位置
    protected void updatePosition(int id){
        for (int i = 0; i < dim; i++){
            double X = 0;
            X = group[id].getPosition(i) + group[id].getVelocity(i);
            if (X>ranges[i].getMax()){
                X = ranges[i].getMax();
            }
            if (X < ranges[i].getMin()){
                X = ranges[i].getMin();
            }
            group[id].setPosition(i, X);
        }
        double result = fitFunction(group[id].getPosition());
        if (result>group[id].getValue()){
            group[id].setValue(result);
            group[id].setBestPosition();
        }
        if (result>valueBest){
            for(int i=0;i<dim;i++){
                positionBest[i] = group[id].getPosition(i);
            }
            valueBest = result;
        }
    }

     public PsoUnit updateVelocity(PsoUnit psoUnit){//此处无全局变量
        for (int d = 0; d < dim; d++){
            double V = 0;
            V = W*psoUnit.getVelocity(d) +
                    C1*randNumDouble(0, 1)*(psoUnit.getBestPosition(d)-psoUnit.getPosition(d)) +
                    C2*randNumDouble(0, 1)*(positionBest[d] - psoUnit.getPosition(d));
            if (V>maxV){
                V = maxV;
            }
            if (V < -maxV){
                V = -maxV;
            }
            psoUnit.setVelocity(d, V);
        }
        return psoUnit;
    }
    public PsoUnit updatePosition(PsoUnit psoUnit){//但这里有用到全局变量
        for (int i = 0; i < dim; i++){
            double X = 0;
            X = psoUnit.getPosition(i) + psoUnit.getVelocity(i);
            if (X>ranges[i].getMax()){
                X = ranges[i].getMax();
            }
            if (X < ranges[i].getMin()){
                X = ranges[i].getMin();
            }
            psoUnit.setPosition(i, X);
        }
        return psoUnit;
     }
    //运行
    public void run(){
        init();
        iteration();
    }
    public double[] getBestPosition(){
        return positionBest;
    }
    public double getBestValue(){
        return valueBest;
    }
    public void setBestPosition(double[]position){
        for(int i=0;i<dim;i++){
            positionBest[i]=position[i];
        }
    }
    public void setBestValue(double value){
        valueBest=value;
    }
    public double[]getWholeValue(){
        return wholeValue;
    }
    public double getWholeValue(int id){
        return wholeValue[id];
    }
    public void setWholeValue(int id,double value){
        wholeValue[id]=value;
    }
    public void realise(){
        //mBuff=null;
        group=null;
        positionBest=null;
        wholeValue=null;
    }
    public void setC1(double C1){
        this.C1=C1;
    }
    public void setC2(double C2){
        this.C2=C2;
    }
    public void setW(double W){
        this.W=W;
    }
    public void setMaxV(double maxV){
        this.maxV=maxV;
    }
    public void setMax(double xMax){
        this.xMax=xMax;
    }
    public void setMin(double xMin){
        this.xMin=xMin;
    }
    public void setGroup(int i,PsoUnit unit){
        group[i].setPosition(unit.getPosition());
        group[i].setValue(unit.getValue());
        group[i].setVelocity(unit.getVelocity());
        group[i].setBestPosition(unit.getBestPosition());
    }
    public Unit[] getGroup(){
        return (Unit[])group;
    }
    public PsoUnit getGroup(int id){
        return group[id];
    }
    public double[] getPosition(int i){
        return group[i].getPosition();
    }

    public void setGroup(int i,double[] position,double value){
        group[i].setPosition(position);
        group[i].setValue(value);
    }

}

