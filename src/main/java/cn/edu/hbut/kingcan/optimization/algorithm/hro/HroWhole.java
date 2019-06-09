package cn.edu.hbut.kingcan.optimization.algorithm.hro;

import cn.edu.hbut.kingcan.config.Range;
import cn.edu.hbut.kingcan.config.frame.Algorithm;
import cn.edu.hbut.kingcan.config.frame.Unit;

import java.util.Arrays;

public class HroWhole extends Algorithm {
    protected    int                        dim;//维度
    protected    int                        size;//种群数量
    protected    int                        iter;//迭代数
    private      Range[]                    ranges;
    private      HroUnit[]                  group;//种群
    private      double[]                   positionBest;//当前最优位置
    private      double                     valueBest=-Double.MAX_VALUE;//当前最优值
    private 	 double 					xMax=100;//最大范围
    private 	 double 					xMin=0;//最小范围
    private 	 double						w=1.0d;
    private 	 double[]					wholeValue;
    private  	 int 						timeMax=60;
    private 	 double 					rate=0.33d;
    private      int                        threshold=1;
    private      int						searchTimes=0;
    private      int						tempTimes=0;
    private 	 double            			delta=0.0002d;

    public HroWhole(int dim, int size, int iter, double min, double max){
        this.dim=dim;
        this.size=size;
        this.iter=iter;
        this.xMax=max;
        this.xMin=min;
//		this.timeMax=60;
        ranges=new Range[dim];
        for(int d=0;d<dim;d++){
            ranges[d]=new Range(xMin,xMax);
        }

        group=new HroUnit[this.size];
        positionBest=new double[dim];
        wholeValue=new double[iter];
    }
   public HroWhole(int dim,int size,int iter,Range[]ranges){
        this.dim=dim;
        this.size=size;
        this.iter=iter;
//		this.timeMax=60;
        this.ranges=new Range[dim];
        for(int d=0;d<dim;d++){
            if(d<ranges.length){
                this.ranges[d]=new Range(ranges[d].getMin(),ranges[d].getMax());
            }
        }
        group=new HroUnit[this.size];
        positionBest=new double[dim];
        wholeValue=new double[iter];
    }

    private void sortByValue(){
        Arrays.sort(group, new MyComprator());
    }

    @Override
    public void init() {
        // TODO Auto-generated method stub
        for (int s = 0; s < size; s++){
            HroUnit hu = new HroUnit(dim);
            for (int d = 0; d < dim; d++){
                hu.setPosition(d, randNumDouble(ranges[d].getMin(), ranges[d].getMax()));
            }
            hu.setValue(fitFunction(hu.getPosition()));
            group[s] =hu ;
        }
    }
    //种群从优到劣：保持系、恢复系、不育系
    @Override
    protected void update() {
        // TODO Auto-generated method stub
        for(int s=0;s<size;s++){
            if(s<size*rate){
                hybird(s);
            }
            else if(s>=size*rate&&s<size-size*rate){
                if(group[s].getTimes()<=timeMax){
                    selfing(s);
                }else if(s>0&&group[s].getTimes()>timeMax){
                    renew(s);
                }

            }
        }
    }
    private void hybird(int id){
        double[]positionNew=new double[dim];
        for(int d=0;d<dim;d++){
            double rnd1=randNumDouble(-1.0d,1.0d);
            double rnd2=randNumDouble(-1.0d,1.0d);//randGuass(0.0f,1.0f);//
//			随机杂交（在保持系和不育系，头尾分别随机取一个位置杂交）
            int rndId1=randNumInt(0,(int) (size*rate-1));
            int rndId2=randNumInt((int) (size-size*rate),size-1);
            positionNew[d]=rnd1*group[rndId1].getPosition(d)/(rnd1+rnd2)
                    +rnd2*group[rndId2].getPosition(d)/(rnd1+rnd2);
//			//对映杂交
//			positionNew[d]=rnd1*group[id].getPosition(d)/(rnd1+rnd2)
//			+rnd2*group[(int) (id+size-1-size*rate)].getPosition(d)/(rnd1+rnd2);

            if(positionNew[d]>ranges[d].getMax()){
                positionNew[d]=ranges[d].getMax();
            }
            if(positionNew[d]<ranges[d].getMin()){
                positionNew[d]=ranges[d].getMin();
            }
        }
        double temp=fitFunction(positionNew);
        if(temp>group[(int) (id+size-1-size*rate)].getValue()){
            group[(int) (id+size-1-size*rate)].setPosition(positionNew);
            group[(int) (id+size-1-size*rate)].setValue(temp);
//			searchTimes++;
        }
        if(temp>valueBest){
            valueBest=temp;
            for(int j=0;j<dim;j++){
                positionBest[j]=positionNew[j];
            }
        }
    }




    private void selfing(int id){
        double[]positionNew=new double[dim];

        double rnd1=randNumDouble(0.0d,1.0d);
        int rndId=randNumInt((int)(size*rate),(int)(size-size*rate-1));
        for(int d=0;d<dim;d++){

            positionNew[d]=group[id].getPosition(d)+
                    rnd1*(positionBest[d]-group[rndId].getPosition(d));

            if(positionNew[d]>ranges[d].getMax()){
                positionNew[d]=ranges[d].getMax();
            }
            if(positionNew[d]<ranges[d].getMin()){
                positionNew[d]=ranges[d].getMin();
            }
        }
        double temp=fitFunction(positionNew);

        if(temp>group[id].getValue()){
            group[id].setPosition(positionNew);
            group[id].setValue(temp);
//			searchTimes++;
        }else{
            group[id].setTimes(group[id].getTimes()+1);
        }
        if(temp>valueBest){
            group[id].setTimes(0);
            valueBest=temp;
            for(int j=0;j<dim;j++){
                positionBest[j]=positionNew[j];
            }
        }
    }

    private void renew(int id){
//		searchTimes++;
        double[]positionNew=new double[dim];
        for(int d=0;d<dim;d++){
            positionNew[d]=group[id].getPosition(d)+
                    randNumDouble(ranges[d].getMin(),ranges[d].getMax());
            if(positionNew[d]>ranges[d].getMax()){
                positionNew[d]=ranges[d].getMax();
            }
            if(positionNew[d]<ranges[d].getMin()){
                positionNew[d]=ranges[d].getMin();
            }
        }
        double temp=fitFunction(positionNew);
        group[id].setPosition(positionNew);
        group[id].setValue(temp);
        group[id].setTimes(0);
        if(temp>valueBest){
            valueBest=temp;
            for(int j=0;j<dim;j++){
                positionBest[j]=positionNew[j];
            }
        }
    }
    @Override
    public void iteration() {
        // TODO Auto-generated method stub
        sortByValue();
        for(int j=0;j<dim;j++){
            positionBest[j]=group[0].getPosition(j);
        }
        for(int i=0;i<iter;i++){
//			timeMax=(int) (100+(40-100)*(double)i/(double)iter);
//			timeMax=(int) (200*(1-Math.sin(Math.PI/2*(double)i/(double)iter)));
//			timeMax=(int) (360*(Math.sin(Math.PI/2*(double)i/(double)iter+Math.PI/2)))+40;
//			timeMax=(int) (200*Math.pow(0.99d, (double)(i)/10.0d));
//			timeMax=20+(i/800)*60;
//			rate=0.45d+(0.25d-0.45d)*(double)i/(double)iter;

//			rate=0.2*Math.exp(-0.002*i)*Math.cos(i*Math.PI/100)+0.25;


            update();
            sortByValue();
            wholeValue[i]=valueBest;
//			System.out.println("rate:"+rate);
//			tempTimes+=searchTimes;
//			//自适应不育系比例模块1
//			if(i>0){
//				if(searchTimes>tempTimes/i){
//					if(rate<0.45d){
//						rate+=delta;
//					}
//					searchTimes=0;
//				}else{
//					if(rate>0.05d){
//						rate-=delta;
//					}
//					searchTimes=0;
//				}
//			}



            //自适应不育系比例模块1
//			if(i>0){
//				if(valueBest>wholeValue[i-1]){
//					searchTimes=0;
//					if(rate<0.4d){
//						rate+=delta;
//					}
//
//				}else{
//					if(searchTimes<threshold){
//						searchTimes++;
//					}else{
//						if(rate>0.1d){
//							rate-=(delta*threshold);
//						}
//						searchTimes=0;
//					}
//				}
//			}

//			//自适应不育系比例模块2
//			if(i>0){
//				if(valueBest>wholeValue[i-1]){
//					rate=0.1d;
//					searchTimes=0;
//				}else{
//					if(searchTimes<threshold){
//						searchTimes++;
//					}else{
//						rate=0.4d;
//						searchTimes=0;
//					}
//				}
//			}

        }
    }
    //运行
    public void run(){
        init();
        iteration();
    }
    public double getBestValue(){
        return valueBest;
    }
    public double[] getBestPosition(){
        return positionBest;
    }
    public double[]getWholeValue(){
        return wholeValue;
    }
    public void realise(){
        group=null;
        positionBest=null;
        wholeValue=null;
    }
    public void setMax(double xMax){
        this.xMax=xMax;
    }
    public void setMin(double xMin){
        this.xMin=xMin;
    }
    public Unit[]getGroup(){
        return group;
    }
}


