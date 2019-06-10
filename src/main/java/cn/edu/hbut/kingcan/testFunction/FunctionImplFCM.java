package cn.edu.hbut.kingcan.testFunction;

import cn.edu.hbut.kingcan.config.frame.Function;
import scala.Serializable;

import java.util.ArrayList;
import java.util.List;

public class FunctionImplFCM implements Function, Serializable {

    private static final long serialVersionUID = 3673941067129025715L;
    private  int exponent = 3;//模糊指数m
    private  List<double[]> datas;//数据集(外界传入)
    private int clusternum;//聚类数目(外界传入)
    private  List<Integer> datas_label;
    int num_data ;        // 数据行数
    int num_d ;    // 数据维数
    double[][] U; //= new double[clusternum][num_data];
    double[][] c; //= new double[clusternum][num_d];//聚类中心（集）
      public FunctionImplFCM(){
      }
      public FunctionImplFCM(List<double[]> datas,int clusternum){
          this.datas=datas;
          this.clusternum=clusternum;
          this.num_data= datas.size();
          this.num_d= datas.get(0).length;
          this.c= new double[clusternum][num_d];//聚类中心（集）
          this.U = new double[clusternum][num_data];
          this.datas_label=new ArrayList<>();
      }

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
          for (int i=0;i<clusternum;i++) {
              for (int j = 0; j < num_d; j++)
                  c[i][j] = position[i * num_d + j];
          }

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
            datas_label.add(j,index+1);
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

    public int getClusternum() {
        return clusternum;
    }

    public void setClusternum(int clusternum) {
        this.clusternum = clusternum;
    }

    public int getExponent() {
        return exponent;
    }

    public void setExponent(int exponent) {
        this.exponent = exponent;
    }

    public List<double[]> getDatas() {
        return datas;
    }

    public void setDatas(List<double[]> datas) {
        this.datas = datas;
    }

    public List<Integer> getDatas_label() {
        return datas_label;
    }

    public void setDatas_label(List<Integer> datas_label) {
        this.datas_label = datas_label;
    }

    public int getNum_data() {
        return num_data;
    }

    public void setNum_data(int num_data) {
        this.num_data = num_data;
    }

    public int getNum_d() {
        return num_d;
    }

    public void setNum_d(int num_d) {
        this.num_d = num_d;
    }

    public double[][] getU() {
        return U;
    }

    public void setU(double[][] u) {
        U = u;
    }

    public double[][] getC() {
        return c;
    }

    public void setC(double[][] c) {
        this.c = c;
    }
}
