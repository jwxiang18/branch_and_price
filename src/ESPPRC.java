import java.util.*;

public class ESPPRC {
    Instance instance;
    Order[] orders;
    double[] duals;
    Queue<Route> routes;  // list of routes / labels for ESPPRC
    List<Route> addRoutes;
    List<List<Route>> candidateRoutes; // list of candidate routes for ESPPRC

    ESPPRC(Instance instance , double[] duals){
        this.instance = instance;
        orders = instance.orders;
        this.duals = duals;

//        routes = new LinkedList<>();

        routes = new PriorityQueue<>((o1, o2) -> o1.sigma > o2.sigma ? 1 : -1);

        candidateRoutes = new LinkedList<>();
        for(int i = 0 ; i < instance.numOrders+2 ; i++){
            candidateRoutes.add(new LinkedList<>());
        }
    }

    public List<Route> main(List<Route> Allroutes){
        routes.offer(new Route(instance));
        addRoutes = new LinkedList<>();

        //增加停止的条件
        while(!routes.isEmpty() && candidateRoutes.get(instance.numOrders+1).size() <200){
            searchNextPoint(routes.poll());
        }
        for(int i = 0 ; i < candidateRoutes.get(instance.numOrders+1).size() ; i++){
            if(candidateRoutes.get(instance.numOrders+1).get(i).sigma > -1e-6) continue;
            addRoutes.add(candidateRoutes.get(instance.numOrders+1).get(i));
            Allroutes.add(candidateRoutes.get(instance.numOrders+1).get(i));
        }
        return  addRoutes;
    }

    private void searchNextPoint(Route route){
        int lastNo = route.lastOrder.NO;
        if(lastNo == instance.numOrders+1) return;
        for(int i = 1 ; i < instance.numOrders+2 ; i++){
            Route newRoute = route.add(orders[i] ,duals);
            if(newRoute != null && !beDominated(newRoute)){
                routes.offer(newRoute);
            }
        }
    }

    private boolean beDominated(Route route){
        List<Route> competitors = candidateRoutes.get(route.lastOrder.NO);
        int i = 0;
        if(route.lastOrder.NO == instance.numOrders+1 && route.sigma > -1e-6) return true;
        while(i < competitors.size()){
            if(route.sigma >= competitors.get(i).sigma &&
                    route.weight >= competitors.get(i).weight &&
                    route.time >= competitors.get(i).time
                    && contains(competitors.get(i).hasVisitBitSet , route.hasVisitBitSet)
            ){
                return true;
            }
            if(route.sigma <= competitors.get(i).sigma &&
                    route.weight <= competitors.get(i).weight &&
                    route.time <= competitors.get(i).time
                    && contains( route.hasVisitBitSet ,competitors.get(i).hasVisitBitSet )
            ){
                competitors.remove(i);
            }else{
                i++;
            }
        }
        candidateRoutes.get(route.lastOrder.NO).add(route);
        return false;
    }

    private boolean contains(BitSet big , BitSet small){
        BitSet bigClone = (BitSet) big.clone();
        bigClone.or(small);
        return bigClone.equals(big);
    }
}
