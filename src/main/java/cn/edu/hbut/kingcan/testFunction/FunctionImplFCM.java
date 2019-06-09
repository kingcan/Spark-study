package cn.edu.hbut.kingcan.testFunction;

import scala.annotation.meta.param;

import java.util.List;

public class FunctionImplFCM {
    int clusternum = 3;//
    int exponent = 3;//模糊指数m
    List<double[]> datas;//数据集
    List<Integer> datas_label;
    int num_data = datas.size();        // 数据行数
    int num_d = datas.get(0).length;    // 数据维数
    double[][] c = new double[clusternum][num_d];//聚类中心（集）
    public  double function(double position []){
        /*
            @param datas         原始数据
     @param datas_label   数据标签
      @param clusternum    类别数量
      @param iternum       迭代次数
      @param exponent      指数
        */
        if(datas == null || datas.size() < 1 || exponent <= 1) {
            return 0;
        }

        double[][] U = new double[clusternum][num_data];

        /** 更新U */
        for (int j = 0; j < clusternum; j++) {
            for (int k = 0; k < num_data; k++) {//要输出
                double sum1 = 0;
                for (int j_a = 0; j_a < clusternum; j_a++) {
                    sum1 += Math.pow((norm(datas, c, k, num_d, j)/norm(datas, c, k, num_d, j_a)), 2/(exponent-1));
                }
                U[j][k] = 1/sum1;
            }
        }
        for (int j = 0; j < num_data; j++) {        // 归一化
            double sum_d = 0;
            for (int i = 0; i < clusternum; i++) {
                sum_d += U[i][j];
            }
            for (int i = 0; i < clusternum; i++) {
                U[i][j] = U[i][j] / sum_d;
            }
        }
        /** 计算目标J函数 */
        double sum = 0;
        for (int j = 0; j < clusternum; j++) {
            for (int k = 0; k < num_data; k++) {
                sum += Math.pow(U[j][k], exponent)*Math.pow(norm(datas, c, k, num_d, j), 2);
            }
        }

        for (int j = 0; j < num_data; j++) {//这里是根据最大隶属度贴标签
            int index = 0;
            double max = U[index][j];
            for (int i = 1; i < clusternum; i++) {
                if(max < U[i][j]) {
                    index = i;
                    max = U[index][j];
                }
            }
            datas_label.set(j, index+1);
        }

        return sum;
    }
    private static double norm(List<double[]> datas, double[][] c, int k, int num_d, int j) {//计算样本与聚类中心的距离
        double sum = 0;
        for (int i = 0; i < num_d; i++) {
            sum += Math.pow(((datas.get(k)[i]) - c[j][i]),2);
        }
        return Math.sqrt(sum);
    }

}
