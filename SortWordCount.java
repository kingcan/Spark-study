package cn.spark.study.core;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import scala.Tuple2;

import java.util.Arrays;

public class SortWordCount {
    public static void main(String[] args) {
        SparkConf sparkConf=new SparkConf()
                .setAppName("WordCountLocal")
                .setMaster("local");
        JavaSparkContext sc=new JavaSparkContext(sparkConf);//将上面的配置信息加入到sparkContext中
        //针对输入源创建一个初始rdd。输入源的数据会打散分配到rdd的每个partition中形成初始的分布式数据集

        JavaRDD<String> lines =sc.textFile("C://Users//HP//Desktop//test.txt");
        //对初始rdd进行transformation操作
        //通常操作会通过创建function并配合rdd的map等算子进行执行
        //拆分成单词
        JavaRDD<String>words=lines.flatMap(line -> Arrays.asList(line.split(" ")).iterator());//这里的rdd还是很大的
        JavaPairRDD<String,Integer>pairs=words.mapToPair(word-> new Tuple2<>(word,1));//word变word加一个1
        JavaPairRDD<String,Integer>wordCounts=pairs.reduceByKey((x,y)->x+y);
        //新需求按单词频数降序排序（king，15）需要转换成（15，king）
        JavaPairRDD<Integer,String>countWords=wordCounts.mapToPair(word->new Tuple2<>(word._2,word._1));//键值对互换
        JavaPairRDD<Integer,String>sortedWords=countWords.sortByKey(false);
        JavaPairRDD<String,Integer>countWords2=sortedWords.mapToPair(word2->new Tuple2<>(word2._2,word2._1));//键值换回来
        countWords2.foreach(word3->System.out.println("单词："+word3._1+"出现了"+word3._2+"次"));

        sc.close();
    }
}
