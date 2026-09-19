package com.cappleapple.brokennotshattered.client;

import java.util.ArrayList;
import java.util.List;
import java.util.SplittableRandom;

/** Saved-seed crack paths shared by texture wear and the occasional complete model fracture. */
public record WearPattern(long seed, List<Crack> cracks, WearSettings settings) {
    public WearPattern {
        cracks = List.copyOf(cracks);
    }

    public static WearPattern create(long seed, WearSettings settings, String itemId) {
        SplittableRandom random = new SplittableRandom(seed);
        WearSettings.Range range = settings.rangeFor(itemId);
        int count = random.nextInt(range.min(), range.max() + 1);
        List<Crack> cracks = new ArrayList<>(count);
        float spacing = 1F / (count + 1);
        for (int i = 0; i < count; i++) {
            float center = (i + 1) * spacing;
            double extent = random.nextDouble();
            float bottom = (float) random.nextDouble(0.08, 0.5);
            float top = Math.min(0.94F, bottom + (float) random.nextDouble(0.28, 0.62));
            if (extent < 0.22) {
                bottom = 0;
                top = 1;
            } else if (extent < 0.6) {
                if (random.nextBoolean()) bottom = 0;
                else top = 1;
            }
            int steps = Math.max(5, Math.round((top - bottom) * 12));
            float phase = (float) random.nextDouble(0, Math.PI * 2);
            float bend = (float) random.nextDouble(0.8, 1.8);
            List<Point> points = new ArrayList<>(steps + 1);
            for (int step = 0; step <= steps; step++) {
                float progress = (float) step / steps;
                float turn = (float) Math.sin(phase + progress * Math.PI * bend) * 0.23F;
                float jag = (float) random.nextDouble(-0.19, 0.19);
                points.add(new Point(center + (turn + jag) * spacing, bottom + (top - bottom) * progress));
            }
            Path trunk = new Path(points);
            List<Path> branches = new ArrayList<>();
            int forks = random.nextInt(3);
            for (int fork = 0; fork < forks; fork++) {
                int junction = random.nextInt(1, points.size() - 1);
                Path branch = branch(random, points.get(junction), random.nextBoolean() ? -1 : 1,
                    (float) random.nextDouble(0.10, 0.24));
                branches.add(branch);
                // A secondary fork can turn and stop independently of its parent.
                if (random.nextDouble() < 0.4) {
                    int split = random.nextInt(1, branch.points().size() - 1);
                    branches.add(branch(random, branch.points().get(split),
                        branch.points().getLast().x() >= branch.points().getFirst().x() ? -1 : 1,
                        (float) random.nextDouble(0.06, 0.13)));
                }
            }
            cracks.add(new Crack(trunk, branches));
        }
        return new WearPattern(seed, cracks, settings);
    }

    private static Path branch(SplittableRandom random, Point start, int direction, float length) {
        int steps = random.nextInt(3, 6);
        double angle = (direction > 0 ? 0 : Math.PI) + random.nextDouble(-0.85, 0.85);
        List<Point> points = new ArrayList<>();
        points.add(start);
        Point previous = start;
        for (int step = 0; step < steps; step++) {
            angle += random.nextDouble(-0.8, 0.8);
            float stride = length / steps * (float) random.nextDouble(0.7, 1.3);
            Point next = new Point(
                Math.clamp(previous.x() + (float) Math.cos(angle) * stride, 0.035F, 0.965F),
                Math.clamp(previous.y() + (float) Math.sin(angle) * stride, 0.035F, 0.965F)
            );
            points.add(next);
            previous = next;
        }
        return new Path(points);
    }

    /** Only complete fractures separate pieces. Interior cracks and forks remain surface grooves. */
    public List<Path> seams() {
        return cracks.stream().map(Crack::trunk).filter(Path::through).toList();
    }

    public record Point(float x, float y) {}

    public record Crack(Path trunk, List<Path> branches) {
        public Crack {
            branches = List.copyOf(branches);
        }
    }

    public record Path(List<Point> points) {
        public Path {
            points = List.copyOf(points);
            if (points.size() < 2) throw new IllegalArgumentException("A crack needs at least two points");
        }

        public boolean through() {
            return points.getFirst().y() == 0 && points.getLast().y() == 1;
        }

        /** Trunks advance in Y; branches may turn in any direction and are never model seams. */
        public float xAt(float y) {
            for (int i = 1; i < points.size(); i++) {
                Point a = points.get(i - 1);
                Point b = points.get(i);
                if (y <= b.y() || i == points.size() - 1) {
                    return a.x() + (b.x() - a.x()) * ((y - a.y()) / (b.y() - a.y()));
                }
            }
            throw new IllegalStateException("Empty crack");
        }

        double distanceSquared(double x, double y, double closest) {
            for (int i = 1; i < points.size(); i++) {
                Point a = points.get(i - 1);
                Point b = points.get(i);
                double boxX = Math.max(Math.max(a.x(), b.x()) < x ? x - Math.max(a.x(), b.x())
                    : Math.min(a.x(), b.x()) - x, 0);
                double boxY = Math.max(Math.max(a.y(), b.y()) < y ? y - Math.max(a.y(), b.y())
                    : Math.min(a.y(), b.y()) - y, 0);
                if (boxX * boxX + boxY * boxY >= closest) continue;
                double dx = b.x() - a.x();
                double dy = b.y() - a.y();
                double lengthSquared = dx * dx + dy * dy;
                double t = lengthSquared == 0 ? 0
                    : Math.clamp(((x - a.x()) * dx + (y - a.y()) * dy) / lengthSquared, 0, 1);
                double distanceX = x - a.x() - t * dx;
                double distanceY = y - a.y() - t * dy;
                closest = Math.min(closest, distanceX * distanceX + distanceY * distanceY);
            }
            return closest;
        }
    }

    /** NativeImage and baked vertex colors both use ABGR. Alpha is deliberately preserved. */
    public int modify(int abgr, int x, int y, int width, int height) {
        int alpha = abgr >>> 24;
        if (alpha == 0) return abgr;
        double u = (x + 0.5) / width;
        double v = (y + 0.5) / height;
        // Evaluate on a normalized grid so HD packs retain coherent worn patches.
        int nx = (int) (u * 64);
        int ny = (int) (v * 32);
        double noise = noise(nx, ny);
        double crackWidth = Math.max(0.010, 0.36 / width);
        double highlightDistanceSquared = Math.pow(crackWidth + 0.018, 2);
        double distanceSquared = highlightDistanceSquared;
        for (Crack crack : cracks) {
            distanceSquared = crack.trunk().distanceSquared(u, 1 - v, distanceSquared);
            for (Path branch : crack.branches()) {
                // Finer branches retain the same finite endpoints, including at HD resolutions.
                double branchLimit = distanceSquared * 0.49;
                double branchDistance = branch.distanceSquared(u, 1 - v, branchLimit);
                if (branchDistance < branchLimit) distanceSquared = branchDistance / 0.49;
            }
        }
        double red = abgr & 255;
        double green = (abgr >>> 8) & 255;
        double blue = (abgr >>> 16) & 255;
        double grey = red * 0.2126 + green * 0.7152 + blue * 0.0722;
        double fade = settings.fading();
        double shade = 1 - settings.darkening();
        double highlight = 0;
        if (noise < settings.scuffing()) {
            shade *= 0.65 + noise(nx / 3, ny / 2) * 0.3;
            highlight = 18 * noise(nx + 17, ny);
        }
        if (distanceSquared < crackWidth * crackWidth) {
            shade *= 0.18;
            highlight = 0;
        } else if (distanceSquared < highlightDistanceSquared) {
            highlight += 24 + 18 * noise;
        }
        return alpha << 24
            | channel((red + (grey - red) * fade) * shade + highlight)
            | channel((green + (grey - green) * fade) * shade + highlight) << 8
            | channel((blue + (grey - blue) * fade) * shade + highlight) << 16;
    }

    private double noise(int x, int y) {
        long z = seed ^ (x * 0x9E3779B97F4A7C15L) ^ (y * 0xD1B54A32D192ED03L);
        z = (z ^ (z >>> 30)) * 0xBF58476D1CE4E5B9L;
        z = (z ^ (z >>> 27)) * 0x94D049BB133111EBL;
        return ((z ^ (z >>> 31)) >>> 11) * 0x1.0p-53;
    }

    private static int channel(double value) {
        return Math.clamp((int) Math.round(value), 0, 255);
    }
}
