package logic;

public final class UpdaterLogic {

    public static float[] updateVelocity(Settings s, float[] positions, int[] types, int index, float vx, float vy) {

        int type = types[index];
        float x = positions[index * 2];
        float y = positions[index * 2 + 1];

        int typeIndex = 0;
        int positionIndex = 0;

        while (positionIndex < positions.length) {

            if (typeIndex != index) {

                float x2 = positions[positionIndex];
                float y2 = positions[positionIndex + 1];

                if (s.wrap && (x2 < s.rMax || x2 > s.rangeX - s.rMax || y2 < s.rMax || y2 > s.rangeY - s.rMax)) {
                    if (x2 > x) {
                        float wrappedX2 = x2 - s.rangeX;
                        if (x - wrappedX2 < x2 - x) {
                            x2 = wrappedX2;
                        }
                    } else {
                        float wrappedX2 = x2 + s.rangeX;
                        if (wrappedX2 - x < x - x2) {
                            x2 = wrappedX2;
                        }
                    }
                    if (y2 > y) {
                        float wrappedY2 = y2 - s.rangeY;
                        if (y - wrappedY2 < y2 - y) {
                            y2 = wrappedY2;
                        }
                    } else {
                        float wrappedY2 = y2 + s.rangeY;
                        if (wrappedY2 - y < y - y2) {
                            y2 = wrappedY2;
                        }
                    }
                }

                float dx = x2 - x;
                float dy = y2 - y;
                float distanceSquared = dx * dx + dy * dy;
                if (distanceSquared < s.rMaxSquared) {
                    float distance = (float) Math.sqrt(distanceSquared);

                    float force;

                    if (distance < s.rMin) {

                        force = distance / s.rMin - 1;

                    } else if (distance < s.rMax) {

                        int type2 = types[typeIndex];
                        float attraction = s.matrix.get(type, type2);
                        force = attraction * (1 - Math.abs(2 * distance - s.rMin - s.rMax) / (s.rMax - s.rMin));

                    } else {

                        force = 0;

                    }

                    float a = s.forceFactor * force;
                    float factor = a / distance * s.dt;
                    vx += dx * factor;
                    vy += dy * factor;
                }

            }

            positionIndex += 2;
            typeIndex += 1;
        }

        if (!s.wrap) {
            // stop particles at the boundaries

            // this is where it would be:
            x += vx * s.dt;
            y += vy * s.dt;

            if (x < 0) {
                vx = -vx;
            } else if (x > s.rangeX) {
                vx = -vx;
            }

            if (y < 0) {
                vy = -vy;
            } else if (y > s.rangeY) {
                vy = -vy;
            }
        }

        // friction force = -v * friction
        vx *= s.oneMinusFrictionDt;
        vy *= s.oneMinusFrictionDt;

        return new float[] {vx, vy};
    }

    public static float[] updatePosition(Settings s, float x, float y, float vx, float vy) {

        // add a little energy
        if (s.heat > 0) {
            vx += (2 * Math.random() - 1) * s.heat;
            vy += (2 * Math.random() - 1) * s.heat;
        }

        x += vx * s.dt;
        y += vy * s.dt;

        if (s.wrap) {

            x = modulo(x, s.rangeX);
            y = modulo(y, s.rangeY);

        } else {
            // stop particles at the boundaries

            if (x < 0) {
                x = 0;
            } else if (x > s.rangeX) {
                x = s.rangeX;
            }
            if (y < 0) {
                y = 0;
            } else if (y > s.rangeY) {
                y = s.rangeY;
            }
        }

        return new float[] {x, y};
    }

    private static float modulo(float a, float b) {
        return ((a % b) + b) % b;
    }
}
