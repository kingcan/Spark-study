package cn.edu.hbut.kingcan.optimization.algorithm.pso;

import cn.edu.hbut.kingcan.config.Range;
import cn.edu.hbut.kingcan.config.frame.Algorithm;
import cn.edu.hbut.kingcan.testFunction.FunctionImpl;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import scala.Serializable;

import java.util.Arrays;
import java.util.List;

public class SparkPsoTest extends PsoWhole implements Serializable {

    private static final long serialVersionUID = 6856557851928304976L;

    public SparkPsoTest(){

    }

    public SparkPsoTest(int dim, int size, int iter, Range[] ranges, PsoUnit[] group, double[] positionBest, double valueBest, double xMax, double xMin, double maxV, double c1, double c2, double w, double[] wholeValue) {
        super(dim, size, iter, ranges, group, positionBest, valueBest, xMax, xMin, maxV, c1, c2, w, wholeValue);
    }

    public SparkPsoTest(int dim, int size, int iter, double min, double max) {
        super(dim, size, iter, min, max);
    }

    public SparkPsoTest(int dim, int size, int iter, Range[] ranges) {
        super(dim, size, iter, ranges);
    }

    public static void main(String[] args) {
        SparkConf sparkConf = new SparkConf()
                .setAppName("SparkPsoTest")
                .setMaster("local");
        JavaSparkContext sc = new JavaSparkContext(sparkConf);//将PSO简化到这种易学习程度
        int dim=10;
        int size = 50;
        int iter =100;
        Range range=new  Range(-100, 100);
        PsoWhole pso  = new PsoWhole(dim, size, iter, range.getMin(), range.getMax());
        pso.init();
        pso.setFunction(new FunctionImpl(dim,"f1"));
        JavaRDD<PsoUnit> psos = sc.parallelize(Arrays.asList(pso.group));
        JavaRDD<PsoUnit>pso2 = psos.map(new Function<PsoUnit, PsoUnit>() {
            @Override
            public PsoUnit call(PsoUnit psoUnit){

                psoUnit.setValue(pso.function.function(psoUnit.getPosition()));//设置pBest（i）
                return  psoUnit;
            }
                             });
        List<PsoUnit> hrosinit = pso2.collect();
        pso.setBestValue();
        pso.setBestPosition();
//        JavaRDD<PsoUnit>pso3=pso2.map(new Function<PsoUnit, PsoUnit>() {
//            @Override
//            public PsoUnit call(PsoUnit psoUnit) throws Exception {
//                return pso.setWholeValue(0,);
//            }
//        });
        List<PsoUnit> display = pso2.collect();
        System.out.println(display.toArray());
             for(PsoUnit ds:display)
                 System.out.println("什么都看不到？"+ds.getValue());
             sc.close();
    }
}
