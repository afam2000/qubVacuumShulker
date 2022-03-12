package qub.vacuumshulkerbox;

public class Util {
    public static float getRandomNumber(float min, float max) {
        return (float) ((Math.random() * (max - min)) + min);
    }
}
