import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.concurrent.*;

public class ElevatorController {

    private Elevator elevator;
    //private Map<Integer, Floor> callingFloorsTable;
    private LinkedHashMap<Integer, Floor> callingFloorsTable;
    private ExecutorService executorService;
    private FutureTask<Void> futureTask;

    public ElevatorController(Elevator elevator) {
        this.elevator = elevator;
        //callingFloorsTable = Collections.synchronizedMap(new LinkedHashMap<>(Building.numFloors + 10));
        callingFloorsTable = new LinkedHashMap<>(Building.numFloors + 10);
        executorService = Executors.newCachedThreadPool();
    }

    public LinkedHashMap<Integer, Floor> getCallingFloorsTable() {
        return callingFloorsTable;
    }

    public void addCallToCFTable(Floor callingFloor) {
        callingFloorsTable.put(callingFloor.getFloorIndex(), callingFloor);
    }

    public void initiateElevatorWork(Floor callingFloor) {
        if (elevator.getState() == Elevator.State.IDLE) {

            elevator.setCallingFloorsTable(callingFloorsTable);
            elevator.setDirection(Direction.getDirection(elevator.getCurrentFloor(), callingFloor.getFloorIndex()));
            elevator.setDestinationFloor(callingFloor.getFloorIndex());

            futureTask = new FutureTask<>(elevator, null);
            executorService.submit(futureTask);

            while (true) {
                if (futureTask.isDone()) {
                    break;
                }
                if (elevator.step.get() >= 50) {
                    futureTask.cancel(true);
                    executorService.shutdown();
                    return;
                }
            }
            // begin recursion of this method till the elevator makes 50 or little more steps
            if (callingFloorsTable.containsKey(elevator.getCurrentFloor()))
                initiateElevatorWork(callingFloorsTable.get(elevator.getCurrentFloor()));
            else {
                Set entrySet = callingFloorsTable.keySet();
                Iterator iterator = entrySet.iterator();
                int nextFloor = 0;
                if (iterator.hasNext())
                    nextFloor = (Integer) iterator.next();
                if (nextFloor != 0)
                    initiateElevatorWork(callingFloorsTable.get(nextFloor));
            }
        }
    }
}
