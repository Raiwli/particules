package fr.particules.model;

/** A particle that absorbs any non-ghost particle it touches. */
public final class StickyParticle extends Particle {
    public StickyParticle(double x, double y, double velocityX, double velocityY, double radius, int color) {
        super(x, y, velocityX, velocityY, radius, color);
    }

    @Override
    boolean absorbsOnContact() {
        return true;
    }
}
