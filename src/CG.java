import gurobi.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CG {
    Instance instance;
    double objVal;
    double [][] distance;
    double [] duals; // duals of each constraint , length = numOrders+2
    double [] y_;
    Order[] orders ;
    ArrayList<GRBVar> theta;  // rlmp variables for each path
    ArrayList<GRBConstr> con; // rlmp's constraints
    ArrayList<Route> routes;

    GRBModel model ,RMP;

    public CG(Instance instance) {
        this.instance = instance;
        distance = instance.distance;
        orders = instance.orders;
        routes = new ArrayList<>();
        duals = new double[instance.numOrders+2];
    }
    public CG(Node node){
        this.instance = node.instance;
        orders = instance.orders;
        routes = new ArrayList<>();
        duals = new double[instance.numOrders+2];
    }

    public boolean MP() throws GRBException {
        initMP();  // initialize route
        buildRLMP(); // build RLMP
        if(!solve_RLMP_get_duals()) return false;
        ESPPRC espprc = new ESPPRC(instance  , duals );
        List<Route> newRoutes = espprc.main(routes);
        while (newRoutes.size() > 0){
            add_path_to_RLMP(newRoutes);
            solve_RLMP_get_duals();
            espprc = new ESPPRC(instance , duals);
            newRoutes = espprc.main(routes);
        }
        solve_final_RLMP_get_sol();
//        solve_final_RMP_get_sol();
        return true;
    }



    private void initMP(){
        for (int i = 1; i < instance.numOrders+1; i++) {
            Route route = new Route(instance);
            route.initAdd(orders[i]);
            routes.add(route);
        }
    }

    private void buildRLMP() throws GRBException {
        GRBEnv env = new GRBEnv();
        model = new GRBModel(env);
        theta = new ArrayList<>();
        con = new ArrayList<>();
        for (int i = 0; i < instance.numOrders; i++) {
            theta.add(model.addVar(0,1,routes.get(i).distance ,GRB.CONTINUOUS,"theta"+ i));
            con.add(model.addConstr(theta.get(i) , GRB.GREATER_EQUAL , 1 , "con"+i));
        }
        model.update();
    }

    private void add_path_to_RLMP(List<Route> routes) throws GRBException {
        for(Route route : routes) {
            int [] new_column = new int[instance.numOrders];
            Arrays.fill(new_column,0);
            for (int i = 1; i < route.path.size()-1; i++) {
                new_column[route.path.get(i)-1] = 1;
            }
            GRBColumn new_RLMP_col = new GRBColumn();
            for (int i = 0; i < instance.numOrders; i++) {
                new_RLMP_col.addTerm(new_column[i],model.getConstr(i));
            }
            theta.add(model.addVar(0,1,route.distance, GRB.CONTINUOUS,new_RLMP_col,"theta"+theta.size()));
            model.update();
//            System.out.println("add path " + route.toString() +" sigma "  + route.sigma + "  distance " + route.distance);
        }
    }

    private boolean solve_RLMP_get_duals() throws GRBException {
        model.set(GRB.IntParam.OutputFlag , 0);
        model.optimize();
        if(model.get(GRB.IntAttr.Status) == 2){
            int i = 1;
            for(GRBConstr constr : model.getConstrs()){
                duals[i] = constr.get(GRB.DoubleAttr.Pi);
                i++;
            }
            return true;
        }
        return false;
    }

    private void solve_final_RLMP_get_sol() throws GRBException {
        if(model.get(GRB.IntAttr.IsMIP) == 1){
            model = model.relax();
        }
        model.optimize();
        System.out.println(("RLMP objval (LB):" + model.get(GRB.DoubleAttr.ObjVal)));
        objVal = model.get(GRB.DoubleAttr.ObjVal);

        y_ = new double[routes.size()];
        for(int i = 0 ; i < routes.size() ; i++){
            y_[i] = theta.get(i).get(GRB.DoubleAttr.X);
        }

    }

    public List<Route> solve_final_RMP_get_sol() throws GRBException {
        model.update();
        RMP = new GRBModel(model);
        if(RMP.get(GRB.IntAttr.IsMIP) != 1){
            for(GRBVar var : RMP.getVars()){
                var.set(GRB.CharAttr.VType , GRB.BINARY);
            }
        }
        RMP.update();
        RMP.set(GRB.IntParam.OutputFlag , 0);
        RMP.optimize();
        System.out.println(("RMP objval (UB):" + RMP.get(GRB.DoubleAttr.ObjVal)));
        List<Route> solutions = new ArrayList<>();
        for(GRBVar var : RMP.getVars()){
            if(var.get(GRB.DoubleAttr.X) > 0.5) {
//                System.out.println(var.get(GRB.StringAttr.VarName) + "  " + var.get(GRB.DoubleAttr.X));
                String name = var.get(GRB.StringAttr.VarName);
                int index = Integer.parseInt(name.substring(5));
                System.out.println(routes.get(index).toString());
                solutions.add(routes.get(index));
            }
        }

        System.out.println(" finished");
        return solutions;
    }
}
