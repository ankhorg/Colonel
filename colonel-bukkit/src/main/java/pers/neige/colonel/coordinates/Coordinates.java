package pers.neige.colonel.coordinates;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.val;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;
import pers.neige.colonel.reader.StringReader;

@Getter
@AllArgsConstructor
public class Coordinates {
    private final @NonNull Coordinate x;
    private final @NonNull Coordinate y;
    private final @NonNull Coordinate z;

    public static @NonNull CoordinatesContainer parse(@NonNull StringReader reader) {
        if (!reader.canRead()) {
            return new CoordinatesContainer("", null);
        }
        val start = reader.getOffset();
        val result = readCoordinates(reader);
        val textLength = reader.getOffset() - start;
        if (result == null) {
            return new CoordinatesContainer(reader.readPrevious(textLength), null);
        }
        return new CoordinatesContainer(reader.peekPrevious(textLength), result);
    }

    public static @Nullable Coordinates readCoordinates(@NonNull StringReader reader) {
        val x = readCoordinate(reader);
        if (x == null) {
            return null;
        }
        val locationType = x.getType();
        reader.skipSeparator();
        val y = readCoordinate(reader);
        if (y == null || locationType != y.getType()) {
            return null;
        }
        reader.skipSeparator();
        val z = readCoordinate(reader);
        if (z == null || locationType != y.getType()) {
            return null;
        }
        return new Coordinates(x, y, z);
    }

    public static @NonNull LocationType readLocationType(@NonNull StringReader reader) {
        val current = reader.current();
        if (current == '~') {
            reader.skip();
            return LocationType.RELATIVE;
        } else if (current == '^') {
            reader.skip();
            return LocationType.LOCAL;
        } else {
            return LocationType.ABSOLUTE;
        }
    }

    public static @Nullable Coordinate readCoordinate(@NonNull StringReader reader) {
        if (!reader.canRead()) {
            return null;
        }
        LocationType type = readLocationType(reader);
        if (!reader.canRead() || reader.isSeparator(reader.current())) {
            return new Coordinate(type, 0);
        }
        Double value = reader.readDouble();
        if (value == null) {
            reader.readString();
            return null;
        }
        return new Coordinate(type, value);
    }

    public @NonNull Location getLocation(@Nullable World world, @Nullable LivingEntity source) {
        if (source == null) {
            return new Location(world, x.get(0), y.get(0), z.get(0));
        }
        if (x.getType() == LocationType.LOCAL) {
            val location = source.getEyeLocation();
            val leftwards = x.getValue();
            val upwards = y.getValue();
            val forwards = z.getValue();

            val rotation = location.getDirection();
            val anchor = location.toVector();

            val forward = rotation.clone().normalize();
            val left = forward.clone().crossProduct(new Vector(0, -1, 0)).normalize();
            val up = left.clone().crossProduct(forward).normalize();

            val position = forward.multiply(forwards)
                .add(up.multiply(-upwards))
                .add(left.multiply(leftwards));

            position.add(anchor);

            return position.toLocation(source.getWorld());
        } else {
            val location = source.getLocation();
            return new Location(source.getWorld(), x.get(location.getX()), y.get(location.getY()), z.get(location.getZ()));
        }
    }
}
