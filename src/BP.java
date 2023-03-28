import gurobi.GRBException;
import java.util.*;

public class BP {
    private static final double bigM = 1e7;
    private Instance instance;

    private double UB,LB;
    private Node bestNode;
    private Queue<Node> nodes;
    public List<Route> solution;

    BP(Instance instance){
        this.instance = instance;
        UB = 1e6;
        LB = 0;

        nodes = new PriorityQueue<>((o1, o2) -> (o1.obj - o2.obj>0) ? 1 : -1);
    }


    //BP主模型，返回True表示发现最优解，返回False表示未发现无解或超时

    public boolean main () throws GRBException, CloneNotSupportedException {
        long start = System.currentTimeMillis();
        nodes.offer(new Node(instance ));
        bestNode = nodes.peek();
        bestNode.solve();
        if (bestNode.allInt){
            solution = bestNode.finalSolve();
            return true;
        }
        while(UB - LB > 1e-6){
            Node curNode = nodes.poll();
            solve_update_add_node(curNode);


            update_bound();

            long end = System.currentTimeMillis();
            if(UB - LB < 1e-6){
                solution = bestNode.finalSolve();
                return true;
            }
            if(end - start > 1000*60*5){
                solution = bestNode.finalSolve();
                System.out.println("超时，给出当前最优解");
                return true;
            }
            if(nodes.isEmpty()){
                System.out.println("模型无解");
                return false;
            }
        }
        return true;
    }

    private void update_bound(){
        LB = bestNode.obj;
        System.out.println("LB = " + LB + " UB = " + UB);
    }

    //计算当前节点的上下界 ， 并且更新UB LB，返回True表示发现最优解，返回False表示未发现最优解
    private void solve_update_add_node(Node node) throws GRBException, CloneNotSupportedException {
        if(node.allInt){
            if(node.obj < UB){
                UB = node.obj;
                bestNode = node;
                System.out.println("当前为整数解且优于UB ：" + UB);
            }
            if(UB - LB < 1e-6) return ; // 发现最优解，结束

        }else if(node.obj < UB){
            Node node0 = branchLeft(node);
            Node node1 = branchRight(node);
            if(node0.solve()) nodes.offer(node0);
            if(node1.solve()) nodes.offer(node1);
        }else {
            node.dominated = true; // 比UB差，不加入队列，该节点被支配
        }
    }

    //分左支，令xij=1；也即就是令i到其他点的距离为无穷大
    private Node branchLeft(Node curNode ) throws CloneNotSupportedException {
        Node newNode = curNode.clone();
        for(int i = 0 ; i < instance.numOrders + 2 ; i++){
            if(newNode.branchAt_i != 0 && i != newNode.branchAt_j){
                newNode.instance.distance[newNode.branchAt_i][i] = bigM;
                newNode.isBranch[newNode.branchAt_i][i] = true;
            }if(newNode.branchAt_j != instance.numOrders + 1 && i != newNode.branchAt_i){
                newNode.instance.distance[i][newNode.branchAt_j] = bigM;
                newNode.isBranch[i][newNode.branchAt_j] = true;
            }
        }
        newNode.dominated = false;
        newNode.isBranch[newNode.branchAt_i][newNode.branchAt_j] = true;
        return newNode;
    }
    //分右支，令xij=0；也即就是令i到j的距离为无穷大
    private Node branchRight(Node curNode ) throws CloneNotSupportedException {
        Node newNode = curNode.clone();
        newNode.instance.distance[newNode.branchAt_i][newNode.branchAt_j] = bigM;
        newNode.isBranch[newNode.branchAt_i][newNode.branchAt_j] = true;
        newNode.dominated = false;
        return newNode;
    }
}
