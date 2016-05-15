package be.olivierdeckers.eddystone;

/**
 * Created by olivierdeckers on 15/05/16.
 */
public class Position {

    public final float x;
    public final float y;

    public Position(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public Position() {
        this.x = 0;
        this.y = 0;
    }

    public Position scale(float weight) {
        float x = this.x * weight;
        float y = this.y * weight;
        return new Position(x, y);
    }

    public Position add(Position other) {
        float x = this.x + other.x;
        float y = this.y + other.y;
        return new Position(x, y);
    }

    @Override
    public String toString() {
        return "Position{" +
                "x=" + x +
                ", y=" + y +
                '}';
    }
}
