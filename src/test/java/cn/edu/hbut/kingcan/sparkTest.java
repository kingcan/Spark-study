//package cn.edu.hbut.kingcan;
//
//
//        import java.io.Serializable;
//        import java.util.ArrayList;
//        import java.util.Arrays;
//        import java.util.HashMap;
//        import java.util.Iterator;
//        import java.util.List;
//        import java.util.Map;
//        import java.util.Map.Entry;
//
//        import org.apache.spark.SparkConf;
//        import org.apache.spark.api.java.JavaPairRDD;
//        import org.apache.spark.api.java.JavaRDD;
//        import org.apache.spark.api.java.JavaSparkContext;
//        import org.apache.spark.api.java.function.FlatMapFunction;
//        import org.apache.spark.api.java.function.Function;
//        import org.apache.spark.api.java.function.Function2;
//        import org.apache.spark.api.java.function.VoidFunction;
//        import org.junit.After;
//        import org.junit.Before;
//        import org.junit.Test;
//        import scala.Tuple2;
//
///**
// * 需要实现Serializable接口，不然会报Task not serializable错误
// *
// */
//public class TransformationsOperator implements Serializable {
//
//    private static final long serialVersionUID = 1L;
//    // 定义为transient，不需要序列化，不然会报Task not serializable错误
//    transient SparkConf conf ;
//    transient JavaSparkContext sc ;
//
//    @Before
//    public void Before(){
//        conf = new SparkConf().setMaster("local").setAppName("TransformationsOperator");
//        sc = new JavaSparkContext(conf);
//    }
//    /**
//     * Return a new distributed dataset formed by passing each element of the
//     * source through a function func. 通过函数将RDD中的每个元素进行转换形成一个新的RDD
//     *
//     * @param
//     */
//    // map，一次只处理一个parition中的一条数据。
//    @Test
//    public void MapOperator() {
//        List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5);
//        JavaRDD<Integer> numberRDD = sc.parallelize(numbers);// 得到一个RDD
//        JavaRDD<String> results = numberRDD.map(new Function<Integer, String>() {// 使用map操作将Integer类型转换成String
//            private static final long serialVersionUID = 1L;
//
//            @Override
//            public String call(Integer number) throws Exception {
//
//                return "number:" + number;
//            }
//        });
//
//        results.foreach(new VoidFunction<String>() {
//            private static final long serialVersionUID = 1L;
//
//            @Override
//            public void call(String arg0) throws Exception {
//                System.out.println(arg0);
//            }
//        });
//    }
//
//    /**
//     * Similar to map, but runs separately on each partition (block) of the RDD,
//     * so func must be of type Iterator => Iterator when running on an RDD of type T.
//     * 作用与map一致，不过是以每个parition作为一个操作单位的，所以返回类型是一个Iterator。
//     * @param sc
//     */
//    //mapPartitions，这个是针对Partition的操作，一次会处理一个partition的所有数据
//    //可以通过FlatMapFunction的参数看到，第一个是Iterator< String>的，
//    //也就是输入的数据是一个Iterator，输出的是Integer，这个输入的 Iterator就是将一个partition中的所有数据传入进来，
//    //经过操作后变成一个Iterable<Integer>的，然后在自动压缩成Integer。
//    @Test
//    public void MapPartitionsOperator() {
//
//        List<String> names = Arrays.asList("zhangsan", "lisi", "wangwu");
//
//        JavaRDD<String> nameRDD = sc.parallelize(names, 2);
//
//        final Map<String, Integer> scoreMap = new HashMap<>();
//        scoreMap.put("zhangsan", 100);
//        scoreMap.put("lisi", 99);
//        scoreMap.put("wangwu", 98);
//
//        // 这里会使用FlatMapFunction将Iterator中的数据自动压缩成Integer数据。
//        JavaRDD<Integer> scoreRDD = nameRDD.mapPartitions(new FlatMapFunction<Iterator<String>, Integer>() {
//
//            private static final long serialVersionUID = 1L;
//
//            @Override
//            public Iterable<Integer> call(Iterator<String> iterator) throws Exception {
//                List<Integer> scores = new ArrayList<>();
//                while (iterator.hasNext()) {
//                    String name = iterator.next();
//                    int score = scoreMap.get(name);
//                    scores.add(score);
//                }
//                return scores;
//            }
//        });
//
//        scoreRDD.foreach(new VoidFunction<Integer>() {
//
//            private static final long serialVersionUID = 1L;
//
//            @Override
//            public void call(Integer score) throws Exception {
//                System.out.println(score);
//            }
//        });
//
//    }
//
//    /**
//     * Similar to mapPartitions, but also provides func with an integer value representing the index of the partition,
//     * so func must be of type (Int, Iterator) => Iterator when running on an RDD of type T.
//     * 与上述的mapParitions神似，不过每次调用call函数的时候会传入一个当前parition的下标进来
//     * @param sc
//     */
//    // 可以看到使用了哪一个parition,采用分区的话:parallelize优先级最高，其次conf.set,最后时local[];
//    @Test
//    public void MapPartitionsWithIndexOperator() {
//        List<String> names = Arrays.asList("zhangsan", "lisi", "wangwu");
//        JavaRDD<String> nameRDD = sc.parallelize(names, 2);// 这里加载的数据设置成2个partition。
//        JavaRDD<String> results = nameRDD
//                .mapPartitionsWithIndex(new Function2<Integer, Iterator<String>, Iterator<String>>() {
//                    private static final long serialVersionUID = 1L;
//
//                    // 这里会有一个Integer的index，可以通过这个来查看当前操作属于哪一个parition。
//                    @Override
//                    public Iterator<String> call(Integer index, Iterator<String> names) throws Exception {
//                        List<String> nameList = new ArrayList<>();
//
//                        while (names.hasNext()) {
//                            String name = names.next();
//                            name = index + ":" + name;
//                            nameList.add(name);
//                        }
//                        return nameList.iterator();
//                    }
//                }, true);
//
//        results.foreach(new VoidFunction<String>() {
//
//            private static final long serialVersionUID = 1L;
//
//            @Override
//            public void call(String name) throws Exception {
//                System.out.println(name);
//            }
//        });
//
//    }
//
//    /**
//     * Return a new dataset formed by selecting those elements of the source on which func returns true
//     * 通过函数筛选出所需要的数据元素，返回true代表保留，false代表抛弃。
//     * @param sc
//     */
//    // 过滤出一部分数据
//    @Test
//    public void FilterOperator() {
//        List<Integer> scores = Arrays.asList(43, 60, 59, 70, 81);
//        JavaRDD<Integer> scoresRDD = sc.parallelize(scores);
//
//        // 筛选出分数小于60的。
//        JavaRDD<Integer> results = scoresRDD.filter(new Function<Integer, Boolean>() {
//
//            private static final long serialVersionUID = 1L;
//
//            @Override
//            public Boolean call(Integer score) throws Exception {
//
//                return score < 60;
//            }
//        });
//
//        results.foreach(new VoidFunction<Integer>() {
//
//            private static final long serialVersionUID = 1L;
//
//            @Override
//            public void call(Integer score) throws Exception {
//                System.out.println(score);
//            }
//        });
//
//    }
//
//    /**
//     * Decrease the number of partitions in the RDD to numPartitions.
//     * Useful for running operations more efficiently after filtering down a large dataset.
//     * 将RDD中的partition进行减少，尤其是在上述的filter之后使用效果更好，
//     * 因为filter会可能会过滤掉大量的数据从而导致一个partition中的数据量很少，
//     * 这时候使用coalesce算子可以尽量的合并partition，一定程度少减少数据倾斜的问题。
//     */
//    // 将partition的数量减少
//    //代码会将第一次运行时数据所在的partition的下标进行保存，
//    //然后将parition减少，再次运行将第二次的partition下标进行保存，方便对比查看。
//    @Test
//    public void CoalesceOperator() {
//        List<String> students = Arrays.asList("stu1", "stu2", "stu3", "stu4", "stu5", "stu6");
//        JavaRDD<String> cls = sc.parallelize(students, 4);// 设置为四个partition
//
//        JavaRDD<String> temp = cls.mapPartitionsWithIndex(new Function2<Integer, Iterator<String>, Iterator<String>>() {
//
//            private static final long serialVersionUID = 1L;
//
//            @Override
//            public Iterator<String> call(Integer index, Iterator<String> cls) throws Exception {
//                List<String> list = new ArrayList<>();
//
//                while (cls.hasNext()) {
//                    String stu = cls.next();
//                    stu = "1[" + index + "]" + stu;
//                    list.add(stu);
//                }
//
//                return list.iterator();
//            }
//        }, true);
//
//        JavaRDD<String> temp2 = temp.coalesce(2);//将四个partition减少到两个
//
//        JavaRDD<String> result = temp2
//                .mapPartitionsWithIndex(new Function2<Integer, Iterator<String>, Iterator<String>>() {
//
//                    private static final long serialVersionUID = 1L;
//
//                    @Override
//                    public Iterator<String> call(Integer index, Iterator<String> cls) throws Exception {
//                        List<String> list = new ArrayList<>();
//                        while (cls.hasNext()) {
//                            String stu = cls.next();
//                            stu = "2[" + index + "]," + stu;
//                            list.add(stu);
//                        }
//                        return list.iterator();
//                    }
//                }, true);
//
//        result.foreach(new VoidFunction<String>() {
//
//            private static final long serialVersionUID = 1L;
//
//            @Override
//            public void call(String stu) throws Exception {
//                System.out.println(stu);
//            }
//        });
//    }
//
//    /**
//     * 6、repartition(numPartitions)
//     * Reshuffle the data in the RDD randomly to create either more or fewer partitions and balance it across them.
//     * This always shuffles all data over the network.
//     * 用来增加parition，并且会将其中的数据进行平衡操作，使用shuffle操作。
//     */
//    // 增加Partition，使用shuffle操作
//    //将两次运行的partition的下标进行保存，方便对比。
//    @Test
//    public void RepartitionOperator() {
//        List<String> students = Arrays.asList("stu1", "stu2", "stu3", "stu4", "stu5", "stu6");
//        JavaRDD<String> cls = sc.parallelize(students, 2);// 设置为两个partition
//
//        JavaRDD<String> temp = cls.mapPartitionsWithIndex(new Function2<Integer, Iterator<String>, Iterator<String>>() {
//
//            private static final long serialVersionUID = 1L;
//
//            @Override
//            public Iterator<String> call(Integer index, Iterator<String> cls) throws Exception {
//                List<String> list = new ArrayList<>();
//
//                while (cls.hasNext()) {
//                    String stu = cls.next();
//                    stu = "1[" + index + "]" + stu;
//                    list.add(stu);
//                }
//
//                return list.iterator();
//            }
//        }, true);
//
//        JavaRDD<String> temp2 = temp.repartition(3);//增加到三个
//
//        JavaRDD<String> result = temp2
//                .mapPartitionsWithIndex(new Function2<Integer, Iterator<String>, Iterator<String>>() {
//
//                    private static final long serialVersionUID = 1L;
//
//                    @Override
//                    public Iterator<String> call(Integer index, Iterator<String> cls) throws Exception {
//                        List<String> list = new ArrayList<>();
//                        while (cls.hasNext()) {
//                            String stu = cls.next();
//                            stu = "2[" + index + "]," + stu;
//                            list.add(stu);
//                        }
//                        return list.iterator();
//                    }
//                }, true);
//
//        result.foreach(new VoidFunction<String>() {
//
//            private static final long serialVersionUID = 1L;
//
//            @Override
//            public void call(String stu) throws Exception {
//                System.out.println(stu);
//            }
//        });
//
//    }
//
//    /**
//     * Similar to map, but each input item can be mapped to 0 or more output items (so func should return a Seq rather than a single item).
//     * 与最开始介绍的map类似，不过map每次操作一个数据并且返回一个数据，但时flatMap可能会返回多个数据。
//     */
//    // 每次對傳進來的一行數據進行單詞的切割
//    public void FlatMapOperator() {
//        List<String> words = Arrays.asList("hello ha", "nihao haha", "hello hao");
//        JavaRDD<String> wordRDD = sc.parallelize(words);
//
//        JavaRDD<String> result = wordRDD.flatMap(new FlatMapFunction<String, String>() {
//
//            /**
//             *
//             */
//            private static final long serialVersionUID = 1L;
//
//            @Override
//            public Iterable<String> call(String line) throws Exception {
//
//                // 按照空格将每次传进来的数据进行分割并返回。
//                return Arrays.asList(line.split(" "));
//            }
//        });
//
//        result.foreach(new VoidFunction<String>() {
//
//            /**
//             *
//             */
//            private static final long serialVersionUID = 1L;
//
//            @Override
//            public void call(String word) throws Exception {
//                System.out.println(word);
//            }
//        });
//
//    }
//
//    /**
//     * Return all the elements of the dataset as an array at the driver program.
//     * This is usually useful after a filter or other operation that returns a sufficiently small subset of the data.
//     * 将集群中的其他节点（如果有的话）的数据pull到driver所在的机器上，
//     * 如果数据量过大的话可能会造成内存溢出的现象，所以官方的建议就是返回的数据量小的话会很有用。
//     */
//    // foreach在从节点进行的遍历，collect会从集群中把数据pull到driver所在的机器上
//    @Test
//    public void CollectOperator() {
//        List<Integer> numberList = Arrays.asList(1, 2, 3, 4, 5);
//        JavaRDD<Integer> numberRDD = sc.parallelize(numberList);
//
//        JavaRDD<Integer> temp = numberRDD.map(new Function<Integer, Integer>() {
//
//            /**
//             *
//             */
//            private static final long serialVersionUID = 1L;
//
//            @Override
//            public Integer call(Integer arg0) throws Exception {
//
//                return arg0 * 2;
//            }
//        });
//
//        List<Integer> result = temp.collect();
//
//        for (Integer num : result) {
//            System.out.println(num);
//        }
//
//    }
//
//    /**
//     * Return the number of elements in the dataset
//     * 统计一下RDD中存在多少数据量。
//     * @param sc
//     */
//    // 统计一下RDD里面有多少数据
//    @Test
//    public void CountOperator() {
//        List<String> stu = Arrays.asList("stu1","stu2","stu3","stu4","stu5");
//        JavaRDD<String> stuRDD = sc.parallelize(stu);
//
//        long count = stuRDD.count();
//
//        System.out.println(count);
//
//    }
//
//    /**
//     * 10、 groupByKey([numTasks])
//     * When called on a dataset of (K, V) pairs, returns a dataset of (K, Iterable) pairs.
//     *Note: If you are grouping in order to perform an aggregation (such as a sum or average) over each key, using reduceByKey or aggregateByKey will yield much better performance.
//     *Note: By default, the level of parallelism in the output depends on the number of partitions of the parent RDD. You can pass an optional numTasks argument to set a different number of tasks.
//     *作用就是将RDD中根据Key进行分组操作，所有Key对应的是一个Iterable。
//     *第一个Note介绍的就是说reduceByKey或者aggregateByKey的性能要比这个groupByKey 的性能好：
//     *如果能用reduceByKey，那就用reduceByKey，因为它会在map端，先进行本地combine，可以大大减少要传输到reduce端的数据量，减小网络传输的开销。
//     *只有在reduceByKey处理不了时，才用groupByKey().map()来替代。 因为reduceBykey聚合后传输的数据量就变少了,
//     *而groupBykey没聚合会传递到taskResult上面数据量比较大。
//     *更好的解释可以看一下这个博客：http://blog.csdn.net/zongzhiyuan/article/details/49965021
//     *第二个Note说的是并行度的问题（通俗的就是task），注意到groupByKey可以跟着一个参数，
//     *这个参数可以决定下面的操作时候的并行度，如果没有设置的话，就默认为父RDD的并行度，
//     *如果设置了话就按照参数的来进行分配，并且下面的task也会变成该参数对应的并行度。
//     */
//
//    // 按照key进行分组
//    @Test
//    public void GroupByKeyOperator() {
//        List<Tuple2<String, Integer>> scoreList = Arrays.asList(
//                new Tuple2<String, Integer>("zhangsan", 100),
//                new Tuple2<String,Integer>("zhangsan", 50),
//                new Tuple2<String,Integer>("lisi", 99),
//                new Tuple2<String,Integer>("wangwu", 120),
//                new Tuple2<String,Integer>("wangwu", 30));
//
//        JavaPairRDD<String, Integer> scoreRDD = sc.parallelizePairs(scoreList,2);//设置为两个partition
//
//        JavaPairRDD<String, Iterable<Integer>> result = scoreRDD.groupByKey(3);
//
//        //此时的并行度依然为3，如果不传入参数的话就使用的是父RDD也就是scoreRDD的并行度，也就是2
//        result.foreach(new VoidFunction<Tuple2<String,Iterable<Integer>>>() {
//
//            private static final long serialVersionUID = 1L;
//
//            @Override
//            public void call(Tuple2<String, Iterable<Integer>> score) throws Exception {
//                System.out.println(score._1 + " " + score._2);
//            }
//        });
//
//    }
//
//    /**
//     * 11、reduce(func)。
//     * Aggregate the elements of the dataset using a function func (which takes two arguments and returns one).
//     * The function should be commutative and associative so that it can be computed correctly in parallel.
//     * 每次传入两个参数通过函数func得到一个返回值，然后使用该值继续与后面的数进行调用func，直到所有的数据计算完成，最后返回一个计算结果。
//     */
//    // 传入两个参数并返回一个结果
//    @Test
//    public void ReduceOperator() {
//        List<Integer> numbers = Arrays.asList(1,2,3,4,5,6);
//
//        JavaRDD<Integer> numberRDD = sc.parallelize(numbers);
//
//        int sum = numberRDD.reduce(new Function2<Integer, Integer, Integer>() {
//
//            private static final long serialVersionUID = 1L;
//
//            @Override
//            public Integer call(Integer num1, Integer num2) throws Exception {
//
//                return num1 + num2;
//            }
//        });
//
//        System.out.println(sum);
//
//    }
//
//    /**
//     * 12、reduceByKey(func, [numTasks])
//     * When called on a dataset of (K, V) pairs, returns a dataset of (K, V) pairs where the values for each key are aggregated using the given reduce function func,
//     * which must be of type (V,V) => V. Like in groupByKey, the number of reduce tasks is configurable through an optional second argument.
//     * 简单的说就是groupByKey + reduce。先按照Key进行分组，然后将每组的Key进行reduce操作，
//     * 得到一个Key对应一个Value的RDD。第二个参数就是指定使用多少task来执行reduce操作
//     */
//    //reduceByKey = groupByKey + reduce
//    @Test
//    public void ReduceByKeyOperator() {
//        List<Tuple2<String, Integer>> scoreList = Arrays.asList(
//                new Tuple2<String, Integer>("zhangsan", 100),
//                new Tuple2<String,Integer>("zhangsan", 50),
//                new Tuple2<String,Integer>("lisi", 99),
//                new Tuple2<String,Integer>("wangwu", 120),
//                new Tuple2<String,Integer>("wangwu", 30));
//
//        JavaPairRDD<String, Integer> scoreRDD = sc.parallelizePairs(scoreList);
//
//        scoreRDD.reduceByKey(new Function2<Integer, Integer, Integer>() {
//
//            private static final long serialVersionUID = 1L;
//
//            @Override
//            public Integer call(Integer score1, Integer score2) throws Exception {
//                return score1 + score2;
//            }
//        },2).foreach(new VoidFunction<Tuple2<String,Integer>>() {
//
//
//            private static final long serialVersionUID = 1L;
//
//            @Override
//            public void call(Tuple2<String, Integer> score) throws Exception {
//                System.out.println("name:" + score._1 + " score:" + score._2);
//            }
//        });
//
//    }
//
//
//    /**
//     * 13.sample(withReplacement, fraction, seed)。
//     * Sample a fraction fraction of the data, with or without replacement, using a given random number generator seed
//     * 对RDD中的数据进行随机取样操作，sample第一个参数代表产生的样本数据是否可以重复，
//     * 第二个参数代表取样的比例，
//     * 第三个数值代表一个随机数种子，如果传入一个常数，那么每次取样结果会一样。
//     */
//    // 随机从RDD中取样
//    @Test
//    public void SampleOperator() {
//        List<String> stu = Arrays.asList("stu1","stu2","stu3","stu4","stu5","stu6");
//        JavaRDD<String> stuRDD = sc.parallelize(stu);
//
//        // 第一个参数决定取样结果是否可重复,第二个参数决定取多少比例的数据,第三个是自定义的随机数种子，如果传入一个常数则每次产生的值一样
//        stuRDD.sample(false, 0.5).foreach(new VoidFunction<String>() {
//
//
//            private static final long serialVersionUID = 1L;
//
//            @Override
//            public void call(String arg0) throws Exception {
//                System.out.println(arg0);
//            }
//        });
//
//    }
//
//    /**
//     * 14.take(n)。
//     * Return an array with the first n elements of the dataset.
//     * 将RDD中的前多少数据返回过来，返回结果为数组形式
//     */
//    // 取出RDD中的前多少数据
//    @Test
//    public void TakeOperator() {
//        List<Integer> numbers = Arrays.asList(1,2,3,4,5);
//
//        JavaRDD<Integer> numberRDD = sc.parallelize(numbers);
//
//        List<Integer> nums = numberRDD.take(3);
//
//        for (Integer num : nums) {
//            System.out.println(num);
//        }
//    }
//
//    /**
//     * 15.takeSample(withReplacement, num, [seed])。
//     * Return an array with a random sample of num elements of the dataset, with or without replacement, optionally pre-specifying a random number generator seed.
//     * 先进行sample然后进行take操作。
//     */
//    //先 sample，再take
//    @Test
//    public void TakeSampleOperator() {
//        List<Integer> numbers = Arrays.asList(1,2,3,4,5);
//
//        JavaRDD<Integer> numberRDD = sc.parallelize(numbers);
//
//        List<Integer> nums = numberRDD.takeSample(false, 2);
//
//        for (Integer num : nums) {
//            System.out.println(num);
//        }
//    }
//
//    /**
//     * 16.union(otherDataset)。
//     * Return a new dataset that contains the union of the elements in the source dataset and the argument.
//     * 返回两个RDD中的并集（但并不会去重），并且parition也会合并，也就是并行度会发生改变
//     */
//    // Union，将两个RDD组合起来返回一个新的RDD,partition也合并
//    @Test
//    public void UnionOperator() {
//        List<String> names1 = Arrays.asList("stu1","stu2","stu3");
//        List<String> names2 = Arrays.asList("stu1","stu5","stu6");
//
//        JavaRDD<String> nameRDD1 = sc.parallelize(names1,2);//两个parition
//        JavaRDD<String> nameRDD2 = sc.parallelize(names2);//一个partition
//
//        nameRDD1.union(nameRDD2).foreach(new VoidFunction<String>() {//此时由三个parition，也就有三个task
//
//            private static final long serialVersionUID = 1L;
//
//            @Override
//            public void call(String name) throws Exception {
//                System.out.println(name);
//            }
//        });
//    }
//
//    /**
//     * 17.distinct([numTasks]))。
//     * Return a new dataset that contains the distinct elements of the source dataset.
//     * 简单的去重操作
//     */
//    // 去重
//    @Test
//    public void DistinctOperator() {
//        List<String> stu = Arrays.asList("wangwu","lisi","zhaoliu","lisi");
//
//        JavaRDD<String> stuRDD = sc.parallelize(stu);
//
//        stuRDD.distinct().foreach(new VoidFunction<String>() {
//
//
//            private static final long serialVersionUID = 1L;
//
//            @Override
//            public void call(String stu) throws Exception {
//                System.out.println(stu);
//            }
//        });
//
//    }
//
//    /**
//     * 18. sortByKey([ascending], [numTasks])。
//     * When called on a dataset of (K, V) pairs where K implements Ordered,
//     * returns a dataset of (K, V) pairs sorted by keys in ascending or descending order,
//     * as specified in the boolean ascending argument.
//     * 根据Key进行排序操作，如果第一个参数为true，则结果为升序，反之为降序。第二个参数就是决定执行的task数目
//     */
//    //根据key排序
//    @Test
//    public void SortByKeyOperator() {
//        List<Tuple2<Integer, String>> stus = Arrays.asList(
//                new Tuple2<Integer, String>(10, "lisi"),
//                new Tuple2<Integer, String>(20, "wangwu"),
//                new Tuple2<Integer, String>(10, "zhaoliu"),
//                new Tuple2<Integer, String>(30, "zhangsan"));
//
//        JavaPairRDD<Integer, String> stusRDD = sc.parallelizePairs(stus);
//
//        stusRDD.sortByKey(true,2).foreach(new VoidFunction<Tuple2<Integer,String>>() {
//
//
//            private static final long serialVersionUID = 1L;
//
//            @Override
//            public void call(Tuple2<Integer, String> stu) throws Exception {
//                System.out.println("name:" + stu._2 + ",score:" + stu._1);
//            }
//        });
//
//    }
//
//    /**
//     * 19.saveAsTextFile(path)。
//     * Write the elements of the dataset as a text file (or set of text files) in a given directory in the local filesystem,
//     * HDFS or any other Hadoop-supported file system. Spark will call toString on each element to convert it to a line of text in the file.
//     * 将RDD保存在文件系统上，Spark会调用元素的toString方法作为一行数据。
//     */
//    //将RDD中的数据进行保存
//    //会生成一个testSaveAsTextFile文件夹，如果文件夹存在则抛出异常。
//    @Test
//    public void SaveAsTextFileOperator() {
//        List<Integer> numbers = Arrays.asList(1,2,3,4,5);
//
//        JavaRDD<Integer> numberRDD = sc.parallelize(numbers);
//
//        JavaRDD<Integer> result = numberRDD.map(new Function<Integer, Integer>() {
//
//
//            private static final long serialVersionUID = 1L;
//
//            @Override
//            public Integer call(Integer number) throws Exception {
//
//                return number * 2;
//            }
//        });
//
//        result.saveAsTextFile("./SaveAsTextFileOperator");//保存在当前目录下
//        //result.saveAsTextFile("hdfs://xxx.xx.xx.xx:xxxx/testSaveAsTextFile");//保存在HDFS上
//
//    }
//
//    /**
//     * 20.intersection(otherDataset)。
//     * Return a new RDD that contains the intersection of elements in the source dataset and the argument.
//     * 作用就是将两个RDD求交集，当然也进行了去重操作。
//     */
//    // 求交集并去重
//    @Test
//    public void IntersectionOperator() {
//        List<String> stus1 = Arrays.asList("stu1","stu2","stu2");
//        List<String> stus2 = Arrays.asList("stu2","stu3","stu3");
//
//        JavaRDD<String> stuRDD1 = sc.parallelize(stus1);
//        JavaRDD<String> stuRDD2 = sc.parallelize(stus2);
//
//        stuRDD1.intersection(stuRDD2).foreach(new VoidFunction<String>() {
//
//            /**
//             *
//             */
//            private static final long serialVersionUID = 1L;
//
//            @Override
//            public void call(String stu) throws Exception {
//                System.out.println(stu);
//            }
//        });
//    }
//
//    /**
//     * 21.cartesian(otherDataset)。
//     * When called on datasets of types T and U, returns a dataset of (T, U) pairs (all pairs of elements).
//     * 相当于进行了一次笛卡尔积的计算，将两个RDD中的数据一一对应起来。
//     */
//    // 笛卡尔积
//    public void CartesianOperator() {
//        List<String> hero = Arrays.asList("张飞","貂蝉","吕布");
//        List<String> skill = Arrays.asList("闪现","斩杀","眩晕");
//
//        JavaRDD<String> heroRDD = sc.parallelize(hero);
//        JavaRDD<String> skillRDD = sc.parallelize(skill);
//
//        heroRDD.cartesian(skillRDD).foreach(new VoidFunction<Tuple2<String,String>>() {
//
//            /**
//             *
//             */
//            private static final long serialVersionUID = 1L;
//
//            @Override
//            public void call(Tuple2<String, String> arg0) throws Exception {
//                System.out.println(arg0);
//            }
//        });
//
//    }
//
//    /**
//     * 22.countByKey()。
//     * Only available on RDDs of type (K, V). Returns a hashmap of (K, Int) pairs with the count of each key.
//     * 只能用在(K,V)类型，用来统计每个key的数据有多少个，返回一个(K,Int)。
//     */
//    // 根据Key进行统计
//    @Test
//    public void CountByKeyOperator() {
//        List<Tuple2<String, String>> stus = Arrays.asList(
//                new Tuple2<String, String>("class1", "stu1"),
//                new Tuple2<String, String>("class1", "stu2"),
//                new Tuple2<String, String>("class2", "stu3"),
//                new Tuple2<String, String>("class1", "stu4"));
//
//        JavaPairRDD<String, String> stuRDD = sc.parallelizePairs(stus);
//
//        Map<String, Object> result = stuRDD.countByKey();
//
//        for (Entry<String, Object> map : result.entrySet()) {
//            System.out.println(map.getKey() + " " +  map.getValue());
//        }
//    }
//
//    /**
//     * 23.first()。
//     * Return the first element of the dataset (similar to take(1)).
//     * 取出第一个，跟take(1)相似。
//     */
//    // 取出第一个元素
//    @Test
//    public void FirstOperator() {
//        List<String> stus = Arrays.asList("stu1","stu2","stu3");
//        JavaRDD<String> stuRDD = sc.parallelize(stus);
//        String firstStu = stuRDD.first();
//        System.out.println(firstStu);
//    }
//
//    /**
//     * 24.cogroup(otherDataset, [numTasks])。
//     * When called on datasets of type (K, V) and (K, W), returns a dataset of (K, (Iterable, Iterable)) tuples.
//     * This operation is also called groupWith.
//     * 将两个RDD按照Key进行汇总，第一个RDD中的Key对应的数据放在一个Iterable中，
//     * 第二个RDD中同样的Key对应的数据放在一个Iterable中，
//     * 最后得到一个Key，对应两个Iterable的数据。第二个参数就是指定task数量。
//     */
//    // 按照Key进行分类汇总
//    @Test
//    public void CogroupOperator() {
//        List<Tuple2<String, String>> stus = Arrays.asList(
//                new Tuple2<String, String>("stu1", "zhangsan"),
//                new Tuple2<String, String>("stu2", "lisi"),
//                new Tuple2<String, String>("stu3", "lisi"),
//                new Tuple2<String, String>("stu2", "wangwu"),
//                new Tuple2<String, String>("stu2", "lisi"));
//
//        List<Tuple2<String, String>> scores = Arrays.asList(
//                new Tuple2<String, String>("stu1", "90"),
//                new Tuple2<String, String>("stu1", "100"),
//                new Tuple2<String, String>("stu2", "80"),
//                new Tuple2<String, String>("stu3", "120"));
//
//        JavaPairRDD<String, String> stuRDD = sc.parallelizePairs(stus);
//        JavaPairRDD<String, String> scoreRDD = sc.parallelizePairs(scores);
//
//        JavaPairRDD<String, Tuple2<Iterable<String>, Iterable<String>>> result = stuRDD.cogroup(scoreRDD);
//
//        result.foreach(new VoidFunction<Tuple2<String,Tuple2<Iterable<String>,Iterable<String>>>>() {
//
//            private static final long serialVersionUID = 1L;
//
//            @Override
//            public void call(Tuple2<String, Tuple2<Iterable<String>, Iterable<String>>> result) throws Exception {
//                System.out.println(result._1);//第一个Tuple2的Key
//                System.out.println(result._2._1);//第一个Tuple2的Vale
//                System.out.println(result._2._2);//第二个Tuple2的Value
//            }
//        });
//    }
//
//    /**
//     * 25.join(otherDataset, [numTasks])。
//     * When called on datasets of type (K, V) and (K, W), returns a dataset of (K, (V, W)) pairs with all pairs of elements for each key.
//     * Outer joins are supported through leftOuterJoin, rightOuterJoin, and fullOuterJoin.
//     * 同样的也是按照Key将两个RDD中进行汇总操作，不过会对每个Key所对应的两个RDD中的数据进行笛卡尔积计算。
//     */
//    //按照Key进行分类汇总，并且做笛卡尔积
//    @Test
//    public void JoinOperator() {
//        List<Tuple2<String, String>> stus = Arrays.asList(
//                new Tuple2<String, String>("stu1", "zhangsan"),
//                new Tuple2<String, String>("stu2", "lisi"),
//                new Tuple2<String, String>("stu3", "lisi"),
//                new Tuple2<String, String>("stu2", "wangwu"),
//                new Tuple2<String, String>("stu2", "lisi"));
//
//        List<Tuple2<String, String>> scores = Arrays.asList(
//                new Tuple2<String, String>("stu1", "90"),
//                new Tuple2<String, String>("stu1", "100"),
//                new Tuple2<String, String>("stu2", "80"),
//                new Tuple2<String, String>("stu3", "120"));
//
//        JavaPairRDD<String, String> stuRDD = sc.parallelizePairs(stus);
//        JavaPairRDD<String, String> scoreRDD = sc.parallelizePairs(scores);
//
//        JavaPairRDD<String, Tuple2<String, String>> result = stuRDD.join(scoreRDD);
//
//        result.foreach(new VoidFunction<Tuple2<String,Tuple2<String,String>>>() {
//
//
//            private static final long serialVersionUID = 1L;
//
//            @Override
//            public void call(Tuple2<String, Tuple2<String, String>> result) throws Exception {
//                System.out.println(result._1);
//                System.out.println(result._2._1);
//                System.out.println(result._2._2);
//                System.out.println();
//            }
//        });
//
//    }
//
//    @After
//    public void after(){
//        sc.close();
//    }
//}
//
