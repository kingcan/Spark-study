package cn.edu.hbut.kingcan.optimization.algorithm.hro;

import cn.edu.hbut.kingcan.config.Range;
import cn.edu.hbut.kingcan.testFunction.FunctionImpl;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import scala.Serializable;


import java.util.Arrays;
import java.util.List;

public class SparkHro extends HroWhole2 implements Serializable {
    public SparkHro(int dim, int size, int iter, Range[] ranges, HroUnit[] group, double[] positionBest, double valueBest, double xMax, double xMin, double w, double[] wholeValue, int timeMax, double rate, int threshold, int searchTimes, int tempTimes, double delta) {
        super(dim, size, iter, ranges, group, positionBest, valueBest, xMax, xMin, w, wholeValue, timeMax, rate, threshold, searchTimes, tempTimes, delta);
    }

    public SparkHro(int dim, int size, int iter, double min, double max) {
        super(dim, size, iter, min, max);
    }

    public SparkHro(int dim, int size, int iter, Range[] ranges) {
        super(dim, size, iter, ranges);
    }

    public static void main(String[] args) {
        SparkConf sparkConf = new SparkConf()
                .setAppName("SparkHro")
                .setMaster("local");
        JavaSparkContext sc = new JavaSparkContext(sparkConf);//将PSO简化到这种易学习程度
        sc.setLogLevel("ERROR");
        int dim = 10;
        int size = 60;
        int iter = 500;
        Range range = new Range(-100, 100);
        HroWhole2 hro = new HroWhole2(dim, size, iter, range.getMin(), range.getMax());
        hro.init();
        hro.setFunction(new FunctionImpl(dim, "Sphere"));
        sc.broadcast(hro.function);
        sc.broadcast(hro.valueBest);
        JavaRDD<HroUnit> hros = sc.parallelize(Arrays.asList(hro.getGroup()));//并行化种群
        JavaRDD<HroUnit> hroInitValues = hros.map(new Function<HroUnit, HroUnit>() {//计算初始适应度函数值
            private static final long serialVersionUID = 377542763092572020L;

            @Override
            public HroUnit call(HroUnit hroUnit) throws Exception {
                hro.setFunction(new FunctionImpl(dim, "Sphere"));
                hroUnit.setValue(hro.function.function(hroUnit.getPosition()));
                return hroUnit;
            }
        });
        List<HroUnit> inithros = hroInitValues.collect();
        hro.setGroup(inithros.toArray(new HroUnit[inithros.size()]));
        hro.sortByValue();//初始化排序
        HroUnit[] temphroGroups = hro.getGroup();
        hro.setValueBest(temphroGroups[0].getValue());//排名第一的设置为全局最优
        hro.setPositionBest(temphroGroups[0].getPosition());
        //---------------开始迭代----------------------
        int currentIter = 0;
        while (currentIter < iter) {
            JavaRDD<HroUnit> iterHro = sc.parallelize(Arrays.asList(hro.getGroup()));
            JavaRDD<HroUnit> iterHro2 = iterHro.map(new Function<HroUnit, HroUnit>() {
                private static final long serialVersionUID = 606889003216598009L;

                @Override
                public HroUnit call(HroUnit hroUnit) throws Exception {
                    //取杂交水稻个体的排名
                    int s = hroUnit.getIndexForSort();
                    if (s < size * hro.getRate()) {
                        hro.hybird(hroUnit);
                    } else if (s >= size * hro.getRate() && s < size - size * hro.getRate()) {
                        if (hroUnit.getTimes() <= hro.getTimeMax()) {
                            hro.selfing(hroUnit);
                        } else if (s > 0 && hroUnit.getTimes() > hro.getTimeMax()) {
                            hro.renew(hroUnit);
                        }

                    }
                    return hroUnit;
                }
            });
            List<HroUnit> hroCollect = iterHro2.collect();
            hro.setGroup(hroCollect.toArray(new HroUnit[0]));//把结果取回来
            for (HroUnit hroUpdated : hro.getGroup()) {

                if (hroUpdated.getValue() > hro.getBestValue()) {
                    hro.setValueBest(hroUpdated.getValue());
                    hro.setPositionBest(hroUpdated.getPosition());
                }
            }
            hro.sortByValue();
            hro.wholeValue[currentIter]=hro.getBestValue();
            currentIter++;

        }
        System.out.println("全局最优结果是" + hro.getBestValue());
        sc.close();
    }
}
