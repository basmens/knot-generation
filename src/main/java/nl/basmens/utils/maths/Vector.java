package nl.basmens.utils.maths;

import java.util.Locale;

public class Vector {
    private double x;
    private double y;

    // =================================================================================================================
    // Constructor
    // =================================================================================================================
    
    public Vector() {
        this(0, 0);
    }
    
      public Vector(double xy) {
        this.x = xy;
        this.y = xy;
    }

    public Vector(double x, double y) {
        this.x = x;
        this.y = y;
    }
    
    public static Vector fromAngle(double angle) {
        return new Vector(Math.cos(angle), Math.sin(angle));
    }
    
    // =================================================================================================================
    // Setters
    // =================================================================================================================
    
    public Vector set(Vector a) {
        this.x = a.getX();
        this.y = a.getY();
        return this;
    }
    
    public static Vector setX(Vector a, double x) {
        return new Vector(x, a.getY());
    }
    
    public Vector setX(double x) {
        this.x = x;
        return this;
    }

    public static Vector setY(Vector a, double y) {
        return new Vector(a.getX(), y);
    }
    
    public Vector setY(double y) {
        this.y = y;
        return this;
    }
    
    public Vector set(double x, double y) {
        this.x = x;
        this.y = y;
        return this;
    }
    
    public static Vector setAngle(Vector a, double angle) {
        double mag = a.mag();
        return new Vector(Math.cos(angle) * mag, Math.sin(angle) * mag);
    }
    
    public Vector setAngle(double angle) {
        double mag = mag();
        x = Math.cos(angle) * mag;
        y = Math.sin(angle) * mag;
        return this;
    }
    
    // =================================================================================================================
    // Getters
    // =================================================================================================================

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getAngle() {
        return Math.atan2(y, x);
    }

    // =================================================================================================================
    // Math
    // =================================================================================================================
    
    // add -------------------------------------------------------------------------------------------------------------

    public static Vector add(Vector a, Vector b) {
        return new Vector(a.x + b.x, a.y + b.y);
    }
    
    public Vector add(Vector a) {
        x += a.x;
        y += a.y;
        return this;
    }

    public static Vector add(Vector a, double x, double y) {
        return new Vector(a.x + x, a.y + y);
    }
    
    public Vector add(double x, double y) {
        this.x += x;
        this.y += y;
        return this;
    }
    
    // sub -------------------------------------------------------------------------------------------------------------
    
    public static Vector sub(Vector a, Vector b) {
        return new Vector(a.x - b.x, a.y - b.y);
    }
    
    public Vector sub(Vector a) {
        x -= a.x;
        y -= a.y;
        return this;
    }
    
    public static Vector sub(Vector a, double x, double y) {
        return new Vector(a.x - x, a.y - y);
    }
    
    public Vector sub(double x, double y) {
        this.x -= x;
        this.y -= y;
        return this;
    }
    
    // mult ------------------------------------------------------------------------------------------------------------

    public static Vector mult(Vector a, Vector b) {
        return new Vector(a.x * b.getX(), a.y * b.getY());
    }
    
    public Vector mult(Vector a) {
        x *= a.getX();
        y *= a.getY();
        return this;
    }

    public static Vector mult(Vector a, double factor) {
        return new Vector(a.x * factor, a.y * factor);
    }
    
    public Vector mult(double factor) {
        x *= factor;
        y *= factor;
        return this;
    }

    public static Vector mult(Vector a, double factorX, double factorY) {
        return new Vector(a.x * factorX, a.y * factorY);
    }
    
    public Vector mult(double factorX, double factorY) {
        x *= factorX;
        y *= factorY;
        return this;
    }
    
    // div -------------------------------------------------------------------------------------------------------------
    
    public static Vector div(Vector a, Vector b) {
        return new Vector(a.x / b.getX(), a.y / b.getY());
    }
    
    public Vector div(Vector a) {
        x /= a.getX();
        y /= a.getY();
        return this;
    }
    
    public static Vector div(Vector a, double factor) {
        return new Vector(a.x / factor, a.y / factor);
    }
    
    public Vector div(double factor) {
        x /= factor;
        y /= factor;
        return this;
    }

    public static Vector div(Vector a, double factorX, double factorY) {
        return new Vector(a.x / factorX, a.y / factorY);
    }
    
    public Vector div(double factorX, double factorY) {
        x /= factorX;
        y /= factorY;
        return this;
    }
    
    // mag -------------------------------------------------------------------------------------------------------------
    
    public static double mag(Vector a) {
        return Math.sqrt(a.x * a.x + a.y * a.y);
    }
    
    public double mag() {
        return Math.sqrt(x * x + y * y);
    }
    
    // normalize -------------------------------------------------------------------------------------------------------
    
    public static Vector normalize(Vector a) {
        double mag = mag(a);
        return mag == 0 ? a.set(0, 0) : div(a, mag);
    }
    
    public Vector normalize() {
        double mag = mag();
        return mag == 0 ? set(0, 0) : div(mag);
    }
    
    // dist ------------------------------------------------------------------------------------------------------------
    
    public static double dist(Vector a, Vector b) {
        double dx = a.x - b.x;
        double dy = a.y - b.y;
        return Math.sqrt(dx * dx + dy * dy);
    }

    public static double dist(Vector a, double x, double y) {
        double dx = a.x - x;
        double dy = a.y - y;
        return Math.sqrt(dx * dx + dy * dy);
    }
    
    // dot -------------------------------------------------------------------------------------------------------------

    public static double dot(Vector a, Vector b) {
        return a.x * b.x + a.y * b.y;
    }
    
    public double dot(Vector a) {
        return x * a.x + y * a.y;
    }
    
    // rotate ----------------------------------------------------------------------------------------------------------

    public static Vector rotate(Vector a, double angle) {
        double x = a.getX() * Math.cos(angle) - a.getY() * Math.sin(angle);
        double y = a.getX() * Math.sin(angle) + a.getY() * Math.cos(angle);
        return new Vector(x, y);
    }
    
    public Vector rotate(double angle) {
        double newX = x * Math.cos(angle) - y * Math.sin(angle);
        y = x * Math.sin(angle) + y * Math.cos(angle);
        x = newX;
        return this;
    }
    
    // abs -------------------------------------------------------------------------------------------------------------

    public static Vector abs(Vector a) {
        return new Vector(Math.abs(a.getX()), Math.abs(a.getY()));
    }
    
    public Vector abs() {
        x = Math.abs(x);
        y = Math.abs(y);
        return this;
    }
    
    // min -------------------------------------------------------------------------------------------------------------
    
    public static Vector min(Vector a, Vector b) {
        return new Vector(Math.min(a.getX(), b.getX()), Math.min(a.getY(), b.getY()));
    }
    
    public Vector min(Vector a) {
        x = Math.min(x, a.getX());
        y = Math.min(y, a.getY());
        return this;
    }

    public Vector min(double x, double y) {
        this.x = Math.min(this.x, x);
        this.y = Math.min(this.y, y);
        return this;
    }
    
    // max -------------------------------------------------------------------------------------------------------------

    public static Vector max(Vector a, Vector b) {
        return new Vector(Math.max(a.getX(), b.getX()), Math.max(a.getY(), b.getY()));
    }
    
    public Vector max(Vector a) {
        x = Math.max(x, a.getX());
        y = Math.max(y, a.getY());
        return this;
    }

    public Vector max(double x, double y) {
        this.x = Math.max(this.x, x);
        this.y = Math.max(this.y, y);
        return this;
    }

    // floor -------------------------------------------------------------------------------------------------------------

    public static Vector floor(Vector a) {
        return new Vector(Math.floor(a.getX()), Math.floor(a.getY()));
    }
    
    public Vector floor() {
        x = Math.floor(x);
        y = Math.floor(y);
        return this;
    }

    // ceil -------------------------------------------------------------------------------------------------------------

    public static Vector ceil(Vector a) {
        return new Vector(Math.ceil(a.getX()), Math.ceil(a.getY()));
    }
    
    public Vector ceil() {
        x = Math.ceil(x);
        y = Math.ceil(y);
        return this;
    }

    // swap -------------------------------------------------------------------------------------------------------------

    public static Vector swap(Vector a) {
        return new Vector(a.getY(), a.getX());
    }
    
    public Vector swap() {
        double temp = x;
        x = y;
        y = temp;
        return this;
    }

    // =================================================================================================================
    // Misc
    // =================================================================================================================
    
    public String toString() {
        return String.format(Locale.US, "%.3f", x) + ", " + String.format(Locale.US, "%.3f", y);
    }

    public Vector copy() {
        return new Vector(x, y);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
          return false;
        }
        Vector a = (Vector) o;

        return a.getX() == x && a.getY() == y;
    }
}
