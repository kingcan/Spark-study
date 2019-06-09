//package cn.edu.hbut.kingcan.optimization.algorithm.pso;
//
//import cn.edu.hbut.kingcan.config.Range;
//import cn.edu.hbut.kingcan.config.frame.Algorithm;
//import cn.edu.hbut.kingcan.config.frame.Unit;
//import cn.edu.hbut.kingcan.testFunction.FunctionImpl;
//import org.apache.spark.SparkConf;
//import org.apache.spark.api.java.JavaRDD;
//import org.apache.spark.api.java.JavaSparkContext;
//import org.apache.spark.api.java.function.Function;
//import scala.Serializable;
//
//import java.util.Arrays;
//import java.util.List;
//import java.util.Random;
//
//public class PsoSpark extends Algorithm implements Serializable {
//    protected           int           dim;//维度
//    protected           int           size;//种群数量
//    protected           int           iter;//迭代数
//    protected           Range[]       ranges;
//    protected           PsoUnit[]     group;//种群
//    protected           double[]      positionBest;//当前最优位置（posBest是个体历史最优）
//    protected           double        valueBest = -Double.MAX_VALUE;//当前最优值
//    protected           double        xMax =100;//最大范围
//    protected           double        xMin=0;//最小范围
//    protected       static     double        maxV;//最大速度
//    protected    static        double        C1;
//    protected static double C2;
//    protected static double W;//学习因子与惯性系数
//    protected           double[]      wholeValue;//每一代的最优值记录
//
//    private static final long serialVersionUID = 6856557851928304976L;
//
//    public static void main(String[] args) {
//        SparkConf sparkConf = new SparkConf()
//                .setAppName("SparkPsoTest")
//                .setMaster("local");
//        JavaSparkContext sc = new JavaSparkContext(sparkConf);//将PSO简化到这种易学习程度
//        int dim=10;
//        int size = 1000;
//        int iter =10;
//        Range range=new  Range(-100, 100);
//        PsoWhole pso  = new PsoWhole(dim, size, iter, range.getMin(), range.getMax());
//        pso.init();
//        //pso.valueBest=100.0;
//        sc.broadcast(pso.positionBest);
//        sc.broadcast(pso.valueBest);
//        System.out.println("初始速度"+ Arrays.toString(pso.getGroup(0).getVelocity()));
//        System.out.println("初始位置"+Arrays.toString(pso.getGroup(0).getBestPosition()));
//        //pso.setFunction(new FunctionImpl(dim,"f1"));
//        JavaRDD<PsoUnit> psos = sc.parallelize(Arrays.asList(pso.group));
//        //---------------------------初始化种群(获取每个个体的fitness)------------
//        JavaRDD<PsoUnit>pso2 = psos.map(new Function<PsoUnit, PsoUnit>() {
//            private static final long serialVersionUID = -3338871691620853220L;
//
//            @Override
//            public PsoUnit call(PsoUnit psoUnit){
//                pso.setFunction(new FunctionImpl(dim,"f1"));
//                psoUnit.setValue(pso.function.function(psoUnit.getPosition()));//设置pBest（i）
//                return  psoUnit;
//            }
//        });
//        List<PsoUnit> hrosinit = pso2.collect();
//
//        pso.group=hrosinit.toArray(new PsoUnit[hrosinit.size()]);
//        int currentIter=0;
//        //-----------------------------开始迭代--------------------------------------------
//        while(currentIter<iter) {
//
//            JavaRDD<PsoUnit> pso22=sc.parallelize(Arrays.asList(pso.group));
//            JavaRDD<PsoUnit> pso3 = pso22.map(new Function<PsoUnit, PsoUnit>() {
//                private static final long serialVersionUID = -6181899017224080504L;
//
//                @Override
//                public PsoUnit call(PsoUnit psoUnit) throws Exception {
//                    PsoSpark.updateVelocity(psoUnit);
//                    pso.updatePosition(psoUnit);
//                    pso.setFunction(new FunctionImpl(dim,"f1"));
//                    psoUnit.setValue(pso.function.function(psoUnit.getPosition()));
//                    return psoUnit;
//                }
//            });
//            JavaRDD<PsoUnit> pso33 = pso3.map(new Function<PsoUnit, PsoUnit>() {
//                private static final long serialVersionUID = 5493756334021228407L;
//
//                @Override
//                public PsoUnit call(PsoUnit psoUnit) throws Exception {
//                    pso.setFunction(new FunctionImpl(dim,"f1"));
//                    double value= pso.function.function(psoUnit.getPosition());
//                    if (value>pso.getBestValue()){
//                        psoUnit.setValue(value);
//                        psoUnit.setBestPosition();
//                    }
//                    return psoUnit;
//                }
//            });
//
//            List<PsoUnit> display = pso33.collect();
//            // Collections.addAll(display, pso.group);
//            pso.group = display.toArray(new PsoUnit[display.size()]);//更新group
//            for (PsoUnit psoeach : pso.group) {
//                if (psoeach.getValue() >pso.getBestValue()) {
//                    pso.setBestValue(psoeach.getValue());
//                    pso.setBestPosition(psoeach.getPosition());
//                }
//            }
//
//            //System.out.println(currentIter+"代:全局最优位置" + Arrays.toString(pso.getBestPosition()));
//            // System.out.println(currentIter+":次迭代的最优值" + pso.getBestValue());
//            System.out.println(display.toArray());
////            for (PsoUnit ds : display) {
////                System.out.println("更新后的速度？" + Arrays.toString(ds.getVelocity()));
////                System.out.println("更新后的位置？" + Arrays.toString(ds.getBestPosition()));
////            }
//            System.out.println(currentIter+":次迭代的最优值" + pso.getBestValue());
//            pso.setW((1.0d-(double)(currentIter)/(double)iter));
//            currentIter++;
//        }
//        sc.close();
//    }
//    public void init() {
//        // TODO Auto-generated method stub
//        for (int s = 0; s < size; s++){
//            PsoUnit pu = new PsoUnit(dim);
//            for (int d = 0; d < dim; d++){
//                pu.setPosition(d, randNumDouble(ranges[d].getMin(), ranges[d].getMax()));
//                pu.setVelocity(d, randNumDouble(-maxV, maxV));
//            }
//            pu.setBestPosition();//将初始化的每一维元素赋值给position
//            group[s] =pu ;
//        }
//    }
//
//    @Override
//    protected void update() {
//
//    }
//
//    @Override
//    public void iteration() {
//
//    }
//
//    @Override
//    public void run() {
//
//    }
//
//    static  void updateVelocity(PsoUnit psoUnit){//此处无全局变量
//        for (int d = 0; d < psoUnit.getBestPosition().length; d++){
//            double V = 0;
//            Random random=new Random();
//            V = W*psoUnit.getVelocity(d) +
//                    C1*this.randNumDouble(0, 1)*(psoUnit.getBestPosition(d)-psoUnit.getPosition(d)) +
//                    C2*randNumDouble(0, 1)*(positionBest[d] - psoUnit.getPosition(d));
//            if (V>maxV){
//                V = maxV;
//            }
//            if (V < -maxV){
//                V = -maxV;
//            }
//            psoUnit.setVelocity(d, V);
//        }
//    }
//    void updatePosition(PsoUnit psoUnit){//但这里有用到全局变量
//        for (int i = 0; i < dim; i++){
//            double X = 0;
//            X = psoUnit.getPosition(i) + psoUnit.getVelocity(i);
//            if (X>ranges[i].getMax()){
//                X = ranges[i].getMax();
//            }
//            if (X < ranges[i].getMin()){
//                X = ranges[i].getMin();
//            }
//            psoUnit.setPosition(i, X);
//        }
////        double result = fitFunction(psoUnit.getPosition());
////        if (result>psoUnit.getValue()){
////            psoUnit.setValue(result);
////            psoUnit.setBestPosition();
////        }
////        if (result>valueBest){
////            for(int i=0;i<dim;i++){
////                positionBest[i] = psoUnit.getPosition(i);
////            }
////            valueBest = result;
////        }
//    }
//    public double[] getBestPosition(){
//        return positionBest;
//    }
//    public double getBestValue(){
//        return valueBest;
//    }
//    public void setBestPosition(double[]position){
//        for(int i=0;i<dim;i++){
//            positionBest[i]=position[i];
//        }
//    }
//    public void setBestValue(double value){
//        valueBest=value;
//    }
//    public double[]getWholeValue(){
//        return wholeValue;
//    }
//    public double getWholeValue(int id){
//        return wholeValue[id];
//    }
//    public void setWholeValue(int id,double value){
//        wholeValue[id]=value;
//    }
//    public void realise(){
//        //mBuff=null;
//        group=null;
//        positionBest=null;
//        wholeValue=null;
//    }
//    public void setC1(double C1){
//        this.C1=C1;
//    }
//    public void setC2(double C2){
//        this.C2=C2;
//    }
//    public void setW(double W){
//        this.W=W;
//    }
//    public void setMaxV(double maxV){
//        this.maxV=maxV;
//    }
//    public void setMax(double xMax){
//        this.xMax=xMax;
//    }
//    public void setMin(double xMin){
//        this.xMin=xMin;
//    }
//    public void setGroup(int i,PsoUnit unit){
//        group[i].setPosition(unit.getPosition());
//        group[i].setValue(unit.getValue());
//        group[i].setVelocity(unit.getVelocity());
//        group[i].setBestPosition(unit.getBestPosition());
//    }
//    public Unit[] getGroup(){
//        return (Unit[])group;
//    }
//    public PsoUnit getGroup(int id){
//        return group[id];
//    }
//    public double[] getPosition(int i){
//        return group[i].getPosition();
//    }
//
//    public void setGroup(int i,double[] position,double value){
//        group[i].setPosition(position);
//        group[i].setValue(value);
//    }
//}
