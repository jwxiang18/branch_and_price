import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

public class Route {
    public Instance instance;
    public Order lastOrder;

    public List<Integer> path;
    public BitSet hasVisitBitSet;
    public double  distance , time , sigma ,dual ;
    public double weight ;

    public Route(Instance instance){
        this.instance = instance;
        path = new ArrayList<>();
        path.add(instance.orders[0].NO);

        hasVisitBitSet = new BitSet(instance.orders.length);
        hasVisitBitSet.set(0);

        lastOrder = instance.orders[0];
        weight = 0;
        sigma = 0;
        dual = 0;
        time = instance.orders[0].readyTime + instance.orders[0].serviceTime;

    }
    public void initAdd(Order order){
        weight += order.demand;
        hasVisitBitSet.set(order.NO);
        hasVisitBitSet.set(instance.orders[instance.orders.length-1].NO);
        path.add(order.NO);
        path.add(instance.orders[instance.orders.length-1].NO);
        distance = instance.distance[0][order.NO] + instance.distance[order.NO][instance.orders.length -1];
        time += distance ;

    }

    public Route add(Order order , double []duals){
        BitSet tmp = (BitSet)hasVisitBitSet.clone();
        tmp.or(instance.canNotVisitBitSet[order.NO]);
        if(tmp.get(order.NO)) return null;
        if(weight + order.demand > instance.capacity) return null;
        double newTime = Math.max(time + instance.distance[lastOrder.NO][order.NO] , order.readyTime) ;
        if(newTime <= order.dueDate){
            Route newRoute = new Route(instance);
            newRoute.path = new ArrayList<>(path);
            newRoute.path.add(order.NO);
            newRoute.hasVisitBitSet = (BitSet) tmp.clone();
            newRoute.hasVisitBitSet.set(order.NO);
            newRoute.weight = weight + order.demand;
            newRoute.lastOrder = order;
            newRoute.time = newTime + order.serviceTime;
            newRoute.distance = distance + instance.distance[lastOrder.NO][order.NO];
            newRoute.dual = dual + duals[order.NO];
            newRoute.sigma = newRoute.distance - newRoute.dual;
            return newRoute;
        }
        return null;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < path.size(); i++) {
            sb.append(path.get(i));
            sb.append(" ");
        }
        return sb.toString();
    }
}
