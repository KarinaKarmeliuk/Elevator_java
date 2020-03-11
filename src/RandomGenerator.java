import java.util.Random;

public class RandomGenerator {

    public static int getRandomNumberInRange(int min, int max) { // generate value inclusive min and max

        if(min >= max)
            throw new IllegalArgumentException("Max value must be greater than min value!");
        Random r = new Random();
        return r.nextInt((max - min) + 1) + min;
    }
}
