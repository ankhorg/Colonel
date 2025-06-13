package pers.neige.colonel.coordinates;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;

@Getter
@AllArgsConstructor
public class CoordinatesContainer {
    private final @NonNull String text;
    private final @Nullable Coordinates result;
}
