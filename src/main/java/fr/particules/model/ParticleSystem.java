package fr.particules.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/** Owns particles and applies all movement, wall, and particle collision rules. */
public final class ParticleSystem {
    private static final int PARTICLE_COUNT = 150;
    private static final double PARTICLE_RADIUS = 8.0;
    private static final double INITIAL_SPEED = 150.0;
    private static final double MAX_STEP_SECONDS = 1.0 / 120.0;
    private static final int PLACEMENT_ATTEMPTS = 100_000;
    private static final double CELL_SIZE = 32.0;
    private static final int HEAVY_COUNT = 12;
    private static final int GHOST_COUNT = 12;
    private static final int STICKY_COUNT = 6;

    private final double width;
    private final double height;
    private final List<Particle> particles;
    private final Random random;

    public ParticleSystem(double width, double height) {
        if (width < PARTICLE_RADIUS * 2 || height < PARTICLE_RADIUS * 2) {
            throw new IllegalArgumentException("The area must fit a particle.");
        }
        this.width = width;
        this.height = height;
        random = new Random();
        particles = new ArrayList<>(PARTICLE_COUNT);
        createParticles();
    }

    public List<Particle> getParticles() {
        return Collections.unmodifiableList(particles);
    }

    public int step(double seconds) {
        if (!Double.isFinite(seconds) || seconds < 0.0) {
            throw new IllegalArgumentException("Elapsed time must be finite and non-negative.");
        }
        int collisions = 0;
        int steps = Math.max(1, (int) Math.ceil(seconds / MAX_STEP_SECONDS));
        double stepSeconds = seconds / steps;
        for (int step = 0; step < steps; step++) {
            particles.forEach(particle -> particle.move(stepSeconds));
            particles.forEach(particle -> particle.bounceOffWalls(width, height));
            collisions += resolveCollisions();
            particles.forEach(particle -> particle.bounceOffWalls(width, height));
        }
        return collisions;
    }

    private void createParticles() {
        for (int index = 0; index < PARTICLE_COUNT; index++) {
            boolean placed = false;
            for (int attempt = 0; attempt < PLACEMENT_ATTEMPTS && !placed; attempt++) {
                double x = random.nextDouble(PARTICLE_RADIUS, width - PARTICLE_RADIUS);
                double y = random.nextDouble(PARTICLE_RADIUS, height - PARTICLE_RADIUS);
                if (isPositionFree(x, y)) {
                    double angle = random.nextDouble(0.0, Math.PI * 2.0);
                    double velocityX = Math.cos(angle) * INITIAL_SPEED;
                    double velocityY = Math.sin(angle) * INITIAL_SPEED;
                    particles.add(createParticle(index, x, y, velocityX, velocityY));
                    placed = true;
                }
            }
            if (!placed) {
                throw new IllegalArgumentException("The area is too small for 150 separate particles.");
            }
        }
    }

    private Particle createParticle(int index, double x, double y, double velocityX, double velocityY) {
        int normalCount = PARTICLE_COUNT - HEAVY_COUNT - GHOST_COUNT - STICKY_COUNT;
        int color = random.nextInt(0x1000000);
        if (index >= normalCount && index < normalCount + HEAVY_COUNT) {
            return new HeavyParticle(x, y, velocityX, velocityY, PARTICLE_RADIUS, color);
        }
        if (index >= normalCount + HEAVY_COUNT && index < normalCount + HEAVY_COUNT + GHOST_COUNT) {
            return new GhostParticle(x, y, velocityX, velocityY, PARTICLE_RADIUS, color);
        }
        if (index >= normalCount + HEAVY_COUNT + GHOST_COUNT) {
            return new StickyParticle(x, y, velocityX, velocityY, PARTICLE_RADIUS, color);
        }
        return new Particle(x, y, velocityX, velocityY, PARTICLE_RADIUS, color);
    }

    private boolean isPositionFree(double x, double y) {
        double minimumDistanceSquared = PARTICLE_RADIUS * PARTICLE_RADIUS * 4.0;
        for (Particle particle : particles) {
            double differenceX = particle.getX() - x;
            double differenceY = particle.getY() - y;
            if (differenceX * differenceX + differenceY * differenceY < minimumDistanceSquared) {
                return false;
            }
        }
        return true;
    }

    private int resolveCollisions() {
        Map<Cell, List<Particle>> cells = new HashMap<>();
        for (Particle particle : particles) {
            Cell cell = cellFor(particle);
            cells.computeIfAbsent(cell, ignored -> new ArrayList<>()).add(particle);
        }

        Set<Particle> absorbed = new HashSet<>();
        int collisions = 0;
        for (Map.Entry<Cell, List<Particle>> entry : cells.entrySet()) {
            Cell cell = entry.getKey();
            List<Particle> contents = entry.getValue();
            for (int offsetX = -1; offsetX <= 1; offsetX++) {
                for (int offsetY = -1; offsetY <= 1; offsetY++) {
                    Cell neighbor = new Cell(cell.x() + offsetX, cell.y() + offsetY);
                    if (compareCells(cell, neighbor) > 0) {
                        continue;
                    }
                    List<Particle> neighbors = cells.get(neighbor);
                    if (neighbors == null) {
                        continue;
                    }
                    for (int first = 0; first < contents.size(); first++) {
                        int start = cell.equals(neighbor) ? first + 1 : 0;
                        for (int second = start; second < neighbors.size(); second++) {
                            Particle left = contents.get(first);
                            Particle right = neighbors.get(second);
                            if (absorbed.contains(left) || absorbed.contains(right) || !left.overlaps(right)
                                    || left.ignoresCollisions() || right.ignoresCollisions()) {
                                continue;
                            }
                            if (left.absorbsOnContact()) {
                                absorbed.add(right);
                            } else if (right.absorbsOnContact()) {
                                absorbed.add(left);
                            } else {
                                if (left.resolveElasticCollision(right)) {
                                    collisions++;
                                }
                                continue;
                            }
                            collisions++;
                        }
                    }
                }
            }
        }
        particles.removeAll(absorbed);
        return collisions;
    }

    private Cell cellFor(Particle particle) {
        return new Cell((int) Math.floor(particle.getX() / CELL_SIZE),
                (int) Math.floor(particle.getY() / CELL_SIZE));
    }

    private int compareCells(Cell first, Cell second) {
        int xOrder = Integer.compare(first.x(), second.x());
        return xOrder == 0 ? Integer.compare(first.y(), second.y()) : xOrder;
    }

    private record Cell(int x, int y) { }
}
