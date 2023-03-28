import gurobi.GRBException;

import java.util.List;

public class Node implements Cloneable{
    Instance instance;
    CG cg;
    public int branchAt_i , branchAt_j;
    public double obj ;
    public double [][]  edges;
    public boolean feasible , dominated , allInt ;
    public boolean[][] isInt , isBranch;
    public Node(Instance instance){
        this.instance = instance;

        isBranch = new boolean[instance.numOrders+2][instance.numOrders+2];
    }

    // 计算当前节点下，从i到j的流量，用于后面选择哪个xij来分支
    private void calEdges(){
        edges = new double[instance.numOrders+2][instance.numOrders+2];
        for(int i = 0 ; i < cg.y_.length ; i++){
            if(!(cg.y_[i] > 1e-6)) continue;
            Route route = cg.routes.get(i);
            for(int j = 0 ; j < route.path.size()-1 ; j++){
                int from = route.path.get(j);
                int to = route.path.get(j+1);
                edges[from][to] += cg.y_[i];
            }
        }
    }

    // 选择xij最接近0.5的i和j
    private void searchEdges(){
        branchAt_i = -1;
        branchAt_j = -1;
        isInt = new boolean[edges.length][edges.length];
        allInt = true;
        double disWith01 , maxDis = 1;
        for (int i = 0; i < edges.length; i++) {
            for (int j = 0; j < edges.length; j++) {
                if(i==j || isBranch[i][j]) {
                    isInt[i][j] = true;
                    continue;
                }
                disWith01 = Math.abs(edges[i][j] - 0.5);
                if(Math.abs(disWith01 - 0.5) < 1e-4) isInt[i][j] = true;
                else {
                    isInt[i][j] = false;
                    allInt = false;
                    if(disWith01 < maxDis){
                        maxDis = disWith01;
                        branchAt_i = i;
                        branchAt_j = j;
                    }
                }
            }
        }
        return ;
    }

    public boolean solve() throws GRBException {
        cg = new CG(this);
        if(cg.MP()) {
            feasible = true;
            obj = cg.objVal;
            int numRoute = cg.routes.size();
            calEdges();  // 计算当前节点下，从i到j的流量，用于后面选择哪个xij来分支
            searchEdges(); // 选择xij最接近0.5的i和j
        }else feasible = false;
        return feasible;
    }

    public List<Route> finalSolve() throws GRBException {
        CG cg = new CG(this);
        cg.MP();
        return cg.solve_final_RMP_get_sol();
    }

    public Node clone() throws CloneNotSupportedException {
        Node newnode = (Node) super.clone();
        newnode.instance = instance.clone();

        newnode.isBranch = this.isBranch.clone();
        for (int i = 0; i < newnode.isBranch.length; i++) {
            newnode.isBranch[i] = this.isBranch[i].clone();
        }
        newnode.isInt = this.isInt.clone();
        for (int i = 0; i < newnode.isInt.length; i++) {
            newnode.isInt[i] = this.isInt[i].clone();
        }

        return newnode;
    }
}
