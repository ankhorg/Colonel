package pers.neige.colonel.coordinates;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

@Getter
@AllArgsConstructor
public class Coordinate {
    private final @NonNull LocationType type;
    private final double value;

    public double get(double offset) {
        switch (type) {
            case ABSOLUTE:
                return value;
            case RELATIVE:
            case LOCAL:
                return value + offset;
            default:
                return 0;
        }
    }
}
