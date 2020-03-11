public enum Direction {
    UP,
    DOWN,
    NONE;

    public static Direction getDirection(int currentFloor, int destinationFloor) {
        int direction = destinationFloor - currentFloor;

        if (direction > 0)
            return UP;
        else if (direction < 0)
            return DOWN;

        return NONE;
    }
}
