package cn.edu.hbut.kingcan.optimization.algorithm.pso;

import cn.edu.hbut.kingcan.config.Range;
import cn.edu.hbut.kingcan.config.frame.Algorithm;
import cn.edu.hbut.kingcan.testFunction.FunctionImpl;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.storage.StorageLevel;
import scala.Serializable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class SparkPsoTest extends PsoWhole implements Serializable {

    private static final long serialVersionUID = 6856557851928304976L;

    public static void main(String[] args) {
        SparkConf sparkConf = new SparkConf()
                .setAppName("SparkPsoTest")
                .setMaster("local");
        JavaSparkContext sc = new JavaSparkContext(sparkConf);//将PSO简化到这种易学习程度
        sc.setLogLevel("ERROR");
        int dim=10;
        int size = 60;
        int iter =500;
        Range range=new  Range(-100, 100);
        PsoWhole pso  = new PsoWhole(dim, size, iter, range.getMin(), range.getMax());
        pso.init();
        //pso.valueBest=100.0;
        sc.broadcast(pso.positionBest);
        sc.broadcast(pso.valueBest);
        System.out.println("初始速度"+ Arrays.toString(pso.getGroup(0).getVelocity()));
        System.out.println("初始位置"+Arrays.toString(pso.getGroup(0).getBestPosition()));
        //pso.setFunction(new FunctionImpl(dim,"f1"));
        JavaRDD<PsoUnit> psos = sc.parallelize(Arrays.asList(pso.group));
        //---------------------------初始化种群(获取每个个体的fitness)------------
        JavaRDD<PsoUnit>pso2 = psos.map(new Function<PsoUnit, PsoUnit>() {
            private static final long serialVersionUID = -3338871691620853220L;

            @Override
            public PsoUnit call(PsoUnit psoUnit){
                pso.setFunction(new FunctionImpl(dim,"f1"));
                psoUnit.setValue(pso.function.function(psoUnit.getPosition()));//设置pBest（i）
                return  psoUnit;
            }
        });
        List<PsoUnit> hrosinit = pso2.collect();
        pso.group=hrosinit.toArray(new PsoUnit[hrosinit.size()]);
        //min()
        for (PsoUnit psoeach : pso.group) {
            if (psoeach.getValue() >pso.getBestValue()) {
                pso.setBestValue(psoeach.getValue());
                pso.setBestPosition(psoeach.getPosition());
            }
        }

        int currentIter=0;
        //-----------------------------开始迭代--------------------------------------------
        while(currentIter<iter) {

            JavaRDD<PsoUnit> pso22=sc.parallelize(Arrays.asList(pso.group));
            JavaRDD<PsoUnit> pso3 = pso22.map(new Function<PsoUnit, PsoUnit>() {
                private static final long serialVersionUID = -6181899017224080504L;

                @Override
                public PsoUnit call(PsoUnit psoUnit) throws Exception {
                   PsoUnit newPso = pso.updateVelocity(psoUnit);
                    PsoUnit newPso2 =pso.updatePosition(newPso);


                    return newPso2;
                }
            });
            JavaRDD<PsoUnit> pso33 = pso3.map(new Function<PsoUnit, PsoUnit>() {
                private static final long serialVersionUID = 5493756334021228407L;

                @Override
                public PsoUnit call(PsoUnit psoUnit) throws Exception {
                    pso.setFunction(new FunctionImpl(dim,"f1"));
                    double value= pso.function.function(psoUnit.getPosition());
                    if (value>psoUnit.getValue()){
                        psoUnit.setValue(value);
                        psoUnit.setBestPosition();
                    }
                    return psoUnit;
                }
            });

            List<PsoUnit> display = pso33.collect();
            // Collections.addAll(display, pso.group);
            pso.group = display.toArray(new PsoUnit[display.size()]);//更新group
            for (PsoUnit psoeach : pso.group) {
                if (psoeach.getValue() >pso.getBestValue()) {
                    pso.setBestValue(psoeach.getValue());
                    pso.setBestPosition(psoeach.getPosition());
                }
            }

            //System.out.println(currentIter+"代:全局最优位置" + Arrays.toString(pso.getBestPosition()));
            // System.out.println(currentIter+":次迭代的最优值" + pso.getBestValue());
            //System.out.println(display.toArray());
//            for (PsoUnit ds : display) {
//                System.out.println("更新后的速度？" + Arrays.toString(ds.getVelocity()));
//                System.out.println("更新后的位置？" + Arrays.toString(ds.getBestPosition()));
//            }
           // System.out.println(currentIter+":次迭代的最优值" + pso.getBestValue());
            pso.setW((1.0d-(double)(currentIter%(iter/100))/(double)(iter/100)));
            currentIter++;
        }
        System.out.println(currentIter+":最终迭代的最优值" + pso.getBestValue());
        sc.close();
    }
}
