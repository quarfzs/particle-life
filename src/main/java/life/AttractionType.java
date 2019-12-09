package life;

import java.util.HashMap;

class AttractionType {

    int color;

    HashMap<AttractionType, Float> attractionRules = new HashMap<>();

    AttractionType(int color) {
        this.color = color;
    }

    float getForce(float dist, float forceDistStart, float forceDistEnd, AttractionType otherAttractionType) {

        if (dist < forceDistStart) {

            return dist / forceDistStart - 1;
        }

        if (dist < forceDistEnd) {
            float f = 1 - Math.abs(2 * dist - forceDistStart - forceDistEnd) / (forceDistEnd - forceDistStart);
            return f * attractionRules.get(otherAttractionType);
        }

        return 0;
    }
}
