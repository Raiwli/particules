package fr.particules.model;

/** A particle that passes through all other particles. */
public final class GhostParticle extends Particle {
    public GhostParticle(double x, double y, double velocityX, double velocityY, double radius, int color) {
        super(x, y, velocityX, velocityY, radius, color);
    }

    @Override
    boolean ignoresCollisions() {
        return true;
    }
}
