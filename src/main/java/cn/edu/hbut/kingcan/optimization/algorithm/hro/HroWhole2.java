package cn.edu.hbut.kingcan.optimization.algorithm.hro;
//spark版本hro
import cn.edu.hbut.kingcan.config.Range;
import cn.edu.hbut.kingcan.config.frame.Algorithm;
import cn.edu.hbut.kingcan.config.frame.Unit;

import java.util.Arrays;

public class HroWhole2 extends Algorithm {
        protected    int                        dim;//维度
        protected    int                        size;//种群数量
        protected    int                        iter;//迭代数
        private      Range[]                    ranges;
        private      HroUnit[]                  group;//种群
        private      double[]                   positionBest;//当前最优位置
        protected       double                     valueBest=-Double.MAX_VALUE;//当前最优值
        private 	 double 					xMax=100;//最大范围
        private 	 double 					xMin=0;//最小范围
        private 	 double						w=1.0d;
        protected  	 double[]					wholeValue;
        private  	 int 						timeMax=60;
        private 	 double 					rate=0.33d;
        private      int                        threshold=1;
        private      int						searchTimes=0;
        private      int						tempTimes=0;
        private 	 double            			delta=0.0002d;

    public HroWhole2(int dim, int size, int iter, Range[] ranges, HroUnit[] group, double[] positionBest, double valueBest, double xMax, double xMin, double w, double[] wholeValue, int timeMax, double rate, int threshold, int searchTimes, int tempTimes, double delta) {
        this.dim = dim;
        this.size = size;
        this.iter = iter;
        this.ranges = ranges;
        this.group = group;
        this.positionBest = positionBest;
        this.valueBest = valueBest;
        this.xMax = xMax;
        this.xMin = xMin;
        this.w = w;
        this.wholeValue = wholeValue;
        this.timeMax = timeMax;
        this.rate = rate;
        this.threshold = threshold;
        this.searchTimes = searchTimes;
        this.tempTimes = tempTimes;
        this.delta = delta;
    }

    public HroWhole2(int dim, int size, int iter, double min, double max){
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
        public HroWhole2(int dim,int size,int iter,Range[]ranges){
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

        protected void sortByValue(){
            Arrays.sort(group, new MyComprator());
            for (int i=0;i<group.length;i++){
                group[i].setIndexForSort(i);//设置排名
            }
        }

        @Override
        public void init() {
            // TODO Auto-generated method stub
            for (int s = 0; s < size; s++){
                HroUnit hu = new HroUnit(dim);
                for (int d = 0; d < dim; d++){
                    hu.setPosition(d, randNumDouble(ranges[d].getMin(), ranges[d].getMax()));
                }
                group[s] =hu ;
            }
        }
        //种群从优到劣：保持系、恢复系、不育系
        @Override
        protected void update() {
            // TODO Auto-generated method stub

        }
        protected HroUnit hybird(HroUnit hroUnit){
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
            if(temp>hroUnit.getValue()){
                hroUnit.setPosition(positionNew);
                hroUnit.setValue(temp);
//			searchTimes++;
            }
            return hroUnit;

        }




        protected HroUnit selfing(HroUnit hroUnit){
            double[]positionNew=new double[dim];

            double rnd1=randNumDouble(0.0d,1.0d);
            int rndId=randNumInt((int)(size*rate),(int)(size-size*rate-1));
            for(int d=0;d<dim;d++){

                positionNew[d]=hroUnit.getPosition(d)+
                        rnd1*(positionBest[d]-group[rndId].getPosition(d));

                if(positionNew[d]>ranges[d].getMax()){
                    positionNew[d]=ranges[d].getMax();
                }
                if(positionNew[d]<ranges[d].getMin()){
                    positionNew[d]=ranges[d].getMin();
                }
            }
            double temp=fitFunction(positionNew);

            if(temp>hroUnit.getValue()){
                hroUnit.setPosition(positionNew);
                hroUnit.setValue(temp);
//			searchTimes++;
            }else{
                hroUnit.setTimes(hroUnit.getTimes()+1);
            }

            return hroUnit;
        }

        protected HroUnit renew(HroUnit hroUnit){
//		searchTimes++;
            double[]positionNew=new double[dim];
            for(int d=0;d<dim;d++){
                positionNew[d]=hroUnit.getPosition(d)+
                        randNumDouble(ranges[d].getMin(),ranges[d].getMax());
                if(positionNew[d]>ranges[d].getMax()){
                    positionNew[d]=ranges[d].getMax();
                }
                if(positionNew[d]<ranges[d].getMin()){
                    positionNew[d]=ranges[d].getMin();
                }
            }
            double temp=fitFunction(positionNew);
            hroUnit.setPosition(positionNew);
            hroUnit.setValue(temp);
            hroUnit.setTimes(0);

            return hroUnit;
        }
        @Override
        public void iteration() {
            // TODO Auto-generated method stub
            sortByValue();
            for(int j=0;j<dim;j++){
                positionBest[j]=group[0].getPosition(j);
            }
            for(int i=0;i<iter;i++){

                update();
                sortByValue();
                wholeValue[i]=valueBest;

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
        public HroUnit[]getGroup(){
            return group;
        }

    public int getDim() {
        return dim;
    }

    public void setDim(int dim) {
        this.dim = dim;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getIter() {
        return iter;
    }

    public void setIter(int iter) {
        this.iter = iter;
    }

    public Range[] getRanges() {
        return ranges;
    }

    public void setRanges(Range[] ranges) {
        this.ranges = ranges;
    }

    public void setGroup(HroUnit[] group) {
        this.group = group;
    }

    public double[] getPositionBest() {
        return positionBest;
    }

    public void setPositionBest(double[] positionBest) {
        this.positionBest = positionBest;
    }

    public double getValueBest() {
        return valueBest;
    }

    public void setValueBest(double valueBest) {
        this.valueBest = valueBest;
    }

    public double getxMax() {
        return xMax;
    }

    public void setxMax(double xMax) {
        this.xMax = xMax;
    }

    public double getxMin() {
        return xMin;
    }

    public void setxMin(double xMin) {
        this.xMin = xMin;
    }

    public double getW() {
        return w;
    }

    public void setW(double w) {
        this.w = w;
    }

    public void setWholeValue(double[] wholeValue) {
        this.wholeValue = wholeValue;
    }

    public int getTimeMax() {
        return timeMax;
    }


    public double getRate() {
        return rate;
    }

    public void setRate(double rate) {
        this.rate = rate;
    }


}


