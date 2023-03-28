import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class ReadData {
    public static Instance readData(String filename , int numOrder) throws IOException {
        // Read the data from the file
        // Return an instance of the class Instance
        Instance instance= new Instance(numOrder);
        try(BufferedReader br = Files.newBufferedReader(Paths.get(filename))){
            for(int i = 0 ; i <4 ; i++){
                br.readLine();
            }
            String[] line = br.readLine().trim().split("\\s+");
            instance.vechileNum = Integer.parseInt(line[0]);
            instance.capacity = Integer.parseInt(line[1]);
            for(int i = 0 ; i < 4 ; i++){
                br.readLine();
            }
            for(int i = 0 ; i < instance.numOrders+1 ; i++){
                line = br.readLine().trim().split("\\s+");
                instance.orders[i] = new Order();
                instance.orders[i].NO = Integer.parseInt(line[0]);
                instance.orders[i].demand = Integer.parseInt(line[3]);
                instance.orders[i].X = Integer.parseInt(line[1]);
                instance.orders[i].Y = Integer.parseInt(line[2]);
                instance.orders[i].readyTime = Integer.parseInt(line[4]);
                instance.orders[i].dueDate = Integer.parseInt(line[5]);
                instance.orders[i].serviceTime = Integer.parseInt(line[6]);
            }
            instance.orders[instance.numOrders+1] = new Order();
            instance.orders[instance.numOrders+1].NO = instance.numOrders+1;
            instance.orders[instance.numOrders+1].X = instance.orders[0].X;
            instance.orders[instance.numOrders+1].Y = instance.orders[0].Y;
            instance.orders[instance.numOrders+1].demand = instance.orders[0].demand;
            instance.orders[instance.numOrders+1].readyTime = instance.orders[0].readyTime;
            instance.orders[instance.numOrders+1].dueDate = instance.orders[0].dueDate;
            instance.orders[instance.numOrders+1].serviceTime = instance.orders[0].serviceTime;
            for(int i = 0 ; i < instance.numOrders+2 ; i++){
                for(int j = i+1 ; j < instance.numOrders+2 ; j++){
                    instance.distance[i][j] = Math.round(Math.sqrt(Math.pow(instance.orders[i].X - instance.orders[j].X, 2) + Math.pow(instance.orders[i].Y - instance.orders[j].Y, 2))*100)/100.0;
                    instance.distance[j][i] = instance.distance[i][j];
                }
            }

            for(int i = 0 ; i < instance.numOrders+2 ; i++){
                for(int j = 0 ; j < instance.numOrders+2 ; j++){
                    if(i!=j){
                        if(instance.orders[i].readyTime+instance.orders[i].serviceTime+instance.distance[i][j] > instance.orders[j].dueDate){
                            instance.canNotVisitBitSet[i].set(j);
                        }
                    }
                }
            }

        }
        return instance;
    }
}
