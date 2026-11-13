package fr.particules.model;

/** A particle with ten times the standard mass. */
public final class HeavyParticle extends Particle {
    public HeavyParticle(double x, double y, double velocityX, double velocityY, double radius, int color) {
        super(x, y, velocityX, velocityY, radius, color);
    }

    @Override
    double getMass() {
        return 10.0;
    }
}
