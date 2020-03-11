import java.util.LinkedHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

public class ElevatorController {

    private Elevator elevator;
    //private Map<Integer, Floor> callingFloorsTable;
    private LinkedHashMap<Integer, Floor> callingFloorsTable;
    private ExecutorService executorService;
    private FutureTask<Void> futureTask;
    private int reachDestinationCounter; // to make program finish

    public ElevatorController(Elevator elevator) {
        this.elevator = elevator;
        //callingFloorsTable = Collections.synchronizedMap(new LinkedHashMap<>(Building.numFloors + 10));
        callingFloorsTable = new LinkedHashMap<>(Building.numFloors + 10);
        executorService = Executors.newCachedThreadPool();
        reachDestinationCounter = 0;
    }

    public void addCallToCFTable(Floor callingFloor) {
        callingFloorsTable.put(callingFloor.getFloorIndex(), callingFloor);
    }

    public void initiateElevatorWork(Floor callingFloor) {
        if (elevator.getState() == Elevator.State.IDLE) {
            elevator.setDirection(callingFloor.determineDirection());
            if (elevator.getDirection() == Direction.UP)
                elevator.setDestinationFloor(callingFloor.findMaxFloorDestination());
            else
                elevator.setDestinationFloor(callingFloor.findMinFloorDestination());
            elevator.setCallingFloorsTable(callingFloorsTable);
            futureTask = new FutureTask<>(elevator, null);
            executorService.submit(futureTask);
            while (true) {
                if (futureTask.isDone()) {
                    reachDestinationCounter++;
                    break;
                }
            }
            // begin recursion of this method till the elevator reach its destination 2 times
            if (reachDestinationCounter == 2) {
                executorService.shutdown();
                return;
            } else if (callingFloorsTable.containsKey(elevator.getCurrentFloor()))
                initiateElevatorWork(callingFloorsTable.get(elevator.getCurrentFloor()));
            else {
                for (int i = 1; i <= Building.numFloors; i++) {
                    if (callingFloorsTable.containsKey(i))
                        initiateElevatorWork(callingFloorsTable.get(i));
                }
            }
        }
    }
}
