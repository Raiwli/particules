package fr.particules.model;

/** A moving disk, independent of any graphical toolkit. */
public class Particle {
    private final double radius;
    private final int color;
    private double x;
    private double y;
    private double velocityX;
    private double velocityY;

    public Particle(double x, double y, double velocityX, double velocityY, double radius, int color) {
        this.x = x;
        this.y = y;
        this.velocityX = velocityX;
        this.velocityY = velocityY;
        this.radius = radius;
        this.color = color;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getRadius() {
        return radius;
    }

    public int getColor() {
        return color;
    }

    double getMass() {
        return 1.0;
    }

    boolean ignoresCollisions() {
        return false;
    }

    boolean absorbsOnContact() {
        return false;
    }

    void move(double seconds) {
        x += velocityX * seconds;
        y += velocityY * seconds;
    }

    void bounceOffWalls(double width, double height) {
        if (x < radius) {
            x = radius;
            velocityX = Math.abs(velocityX);
        } else if (x > width - radius) {
            x = width - radius;
            velocityX = -Math.abs(velocityX);
        }
        if (y < radius) {
            y = radius;
            velocityY = Math.abs(velocityY);
        } else if (y > height - radius) {
            y = height - radius;
            velocityY = -Math.abs(velocityY);
        }
    }

    boolean overlaps(Particle other) {
        double differenceX = other.x - x;
        double differenceY = other.y - y;
        double distanceSquared = differenceX * differenceX + differenceY * differenceY;
        double minimumDistance = radius + other.radius;
        return distanceSquared <= minimumDistance * minimumDistance;
    }

    boolean resolveElasticCollision(Particle other) {
        double differenceX = other.x - x;
        double differenceY = other.y - y;
        double distanceSquared = differenceX * differenceX + differenceY * differenceY;
        double minimumDistance = radius + other.radius;
        if (distanceSquared > minimumDistance * minimumDistance) {
            return false;
        }

        double distance = Math.sqrt(distanceSquared);
        double normalX;
        double normalY;
        if (distance < 1.0e-9) {
            normalX = 1.0;
            normalY = 0.0;
            distance = 0.0;
        } else {
            normalX = differenceX / distance;
            normalY = differenceY / distance;
        }

        double overlap = Math.max(0.0, minimumDistance - distance);
        x -= normalX * overlap * 0.5;
        y -= normalY * overlap * 0.5;
        other.x += normalX * overlap * 0.5;
        other.y += normalY * overlap * 0.5;

        double relativeVelocityX = other.velocityX - velocityX;
        double relativeVelocityY = other.velocityY - velocityY;
        double approachSpeed = relativeVelocityX * normalX + relativeVelocityY * normalY;
        if (approachSpeed < 0.0) {
            double firstMass = getMass();
            double secondMass = other.getMass();
            double impulse = -(2.0 * approachSpeed) / (1.0 / firstMass + 1.0 / secondMass);
            velocityX -= impulse * normalX / firstMass;
            velocityY -= impulse * normalY / firstMass;
            other.velocityX += impulse * normalX / secondMass;
            other.velocityY += impulse * normalY / secondMass;
            return true;
        }
        return false;
    }
}
