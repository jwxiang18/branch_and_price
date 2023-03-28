import gurobi.GRBException;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException, GRBException, CloneNotSupportedException {
        double start = System.currentTimeMillis();
        Instance instance = ReadData.readData("R101.txt" , 100);
//        CG cg = new CG(instance);
//        cg.MP();

        BP bp = new BP(instance);
        bp.main();
        double end = System.currentTimeMillis();
        System.out.println(" time " + (end-start)/1000.0);
    }
}