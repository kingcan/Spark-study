package cn.edu.hbut.kingcan.testFunction;

public class TestFunction {
    private String functionName;
    private int dim;
    public TestFunction(int dim,String functionName){
        this.dim=dim;
        this.functionName=functionName;
    }
    public double functionValue(double[]pos){
        double value=0;
        switch(functionName){

            case "f1"://f1
                value=Sphere(pos);
                break;
            case "f2"://f2
                value=WeightedSphere(pos);
                break;
            case "f3"://f3
                value=Ackley(pos);
                break;
            case "f4"://f4
                value=Rosenbrock(pos);
                break;
            case "f5"://f5
                value=Rastrigin(pos);
                break;
            case "f6"://f6
                value=Griewangk(pos);
                break;
            case "f7"://f7
                value=Michalewicz(pos);
                break;
            case "f8":
                value=f8(pos);
                break;
            case "f9":
                value=f9(pos);
                break;
            case "f10":
                value=f10(pos);
                break;
            case "f11":
                value=f11(pos);
                break;
            case "f12":
                value=f12(pos);
                break;
            case "f13":
                value=f13(pos);
                break;
            case "f14":
                value=f14(pos);
                break;
            case "f15":
                value=f15(pos);
                break;
            case "f16":
                value=f16(pos);
                break;
            case "f17":
                value=f17(pos);
                break;

        }
        return value;
    }

    public int getDim(){
        return this.dim;
    }






    private double Michalewicz(double[]pos){
        double value=0;
        for(int i=0;i<dim;i++){
            value+=Math.pow(Math.sin((i+1)*pos[i]*pos[i]/Math.PI), 20)*Math.sin(pos[i]);
        }
        return -1.0d*value;
    }
    private double Sphere(double[] pos) {
        double value=0;
        for(int i=0;i<dim;i++){
            value+=pos[i]*pos[i];
        }
        return -1*value;
    }

    private double WeightedSphere(double[] pos) {
        double value=0;
        for(int i=0;i<dim;i++){
            value+=(i+1)*pos[i]*pos[i];
        }
        return -1*value;
    }
    private double Rastrigin(double[] pos) {
        // TODO Auto-generated method stub
        double value=0;
        for(int i=0;i<dim;i++){
            value+=pos[i]*pos[i]-10*Math.cos(2*Math.PI*pos[i])+10;
        }
        return -1*value;
    }
    private double Griewangk(double[] pos) {
        // TODO Auto-generated method stub
        double value=0;
        double temp=1;
        for(int i=0;i<dim;i++){
            value+=pos[i]*pos[i];
            temp*=Math.cos(pos[i]/Math.sqrt(i+1));
        }
        value/=4000;
        value-=temp;
        value+=1;
        return -1*value;
    }
    private double Rosenbrock(double[] pos) {
        // TODO Auto-generated method stub
        double value=0;
        for(int i=0;i<dim-1;i++){
            value+=100*(pos[i]*pos[i]-pos[i+1])*(pos[i]*pos[i]-pos[i+1])+(1-pos[i])*(1-pos[i]);
        }
        return -1*value;
    }
    private double Ackley(double[] pos) {
        // TODO Auto-generated method stub
        double temp1=0;
        double temp2=0;
        double temp3=0;
        double temp4=0;
        double value=0;
        for(int i=0;i<dim;i++){
            temp1+=pos[i]*pos[i];
            temp2+=Math.cos(2*Math.PI*pos[i]);
        }
        temp3=(-0.2f*Math.sqrt(temp1/dim));
        temp4=temp2/(double)dim;
        value=(-Math.pow(Math.E, temp4)+Math.E-20*Math.pow(Math.E, temp3)+20);
        return -1*value;
    }
    private double f8(double[] pos) {
        // TODO Auto-generated method stub
        double value=0;
        double temp=0;

        for(int i=0;i<dim;i++){
            temp+=pos[i]*pos[i];
        }
        value= (Math.pow(temp, 0.25)*(Math.pow(Math.sin(50*Math.pow(temp, 0.1)), 2)+1.0));
        return -1*value;
    }

    private double f9(double[] pos) {
        // TODO Auto-generated method stub

        double value=0;
        double temp=0;

        for(int i=0;i<dim;i++){
            temp+=(pos[i])*(pos[i]);
        }
        double temp1= Math.sin(Math.sqrt(temp));
        double temp2=(0.001f*temp*temp);
        value=(0.5+(temp1*temp1-0.5)/((1+temp2)*(1+temp2)));
        return -1*value;
    }
    private double f10(double[] pos) {
        // TODO Auto-generated method stub
        double value=0;
        double temp=0;
        double temp1=1;

        for(int i=0;i<dim;i++){
            temp+=Math.abs(pos[i]);
            temp1*=pos[i];
        }
        temp1=Math.abs(temp1);
        value=temp+temp1;
        return -1*value;
    }
    private double f11(double[] pos) {
        // TODO Auto-generated method stub

        double value=0;
        double temp=0;

        for(int i=0;i<dim;i++){
            temp=0;
            for(int j=0;j<i;j++){
                temp+=pos[j];
            }
            value+=temp*temp;
        }
        return -1*value;
    }

    private double f12(double[] pos) {
        // TODO Auto-generated method stub
        double value=0;
        double temp=0;
        for(int i=0;i<dim;i++){
            temp=pos[i]*pos[i];
            value+=temp*temp-16*temp+5*pos[i];
        }
        return -1*value;
    }
    private double f13(double[] pos) {
        // TODO Auto-generated method stub
        double value=0;
        for(int i=0;i<dim;i++){
            value+=pos[i]*Math.sin(Math.sqrt(Math.abs(pos[i])));
        }
        return value;
    }
    private double f14(double[] pos) {
        // TODO Auto-generated method stub
        double value=0;
        double temp1=0;
        double temp2=0;
        for(int i=0;i<dim;i++){
            temp1+=pos[i]*pos[i];
            temp2+=(i+1)*temp1;
        }
        value=temp1+Math.pow(0.5*temp2, 2)+Math.pow(0.5*temp2, 4);
        return -value;
    }
    private double f15(double[] pos) {
        // TODO Auto-generated method stub
        double value=0;
        double temp=0;
        for(int i=0;i<dim;i++){
            temp+=pos[i]*pos[i];
        }
        value=1-Math.cos(Math.PI*2*Math.sqrt(temp))+0.1*Math.sqrt(temp);
        return -value;
    }
    private double f16(double[] pos) {
        // TODO Auto-generated method stub
        double value=0;
        for(int i=0;i<dim;i++){
            value+=Math.abs(pos[i]*Math.sin(pos[i])+0.1*pos[i]);
        }

        return -value;
    }
    private double f17(double[] pos) {
        // TODO Auto-generated method stub
        double value=0;
        double temp=0;
        double temp1=1;

        for(int i=0;i<dim;i++){
            temp+=(pos[i]-i-1)*(pos[i]-i-1);
            temp1*=(pos[i]-i-1)*(pos[i]-i-1);
        }
        value=temp+temp1;
        return -1*value;
    }
}

