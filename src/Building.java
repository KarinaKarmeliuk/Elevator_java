public class Building {

    public static int numFloors;

    public static void main(String[] args) {

        numFloors = RandomGenerator.getRandomNumberInRange(5, 20);
        Elevator elevator = new Elevator();
        ElevatorController elevatorController = new ElevatorController(elevator);
        Floor floors [] = new Floor[numFloors];

        for (int i = 0; i < floors.length; i++) {
            floors[i] = new Floor(i+1, elevatorController);
        }
        elevator.setFloors(floors);

        elevatorController.initiateElevatorWork();
    }
}
