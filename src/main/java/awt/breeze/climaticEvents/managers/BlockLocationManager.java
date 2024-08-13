package awt.breeze.climaticEvents.managers;

import org.bukkit.Location;

import java.util.HashSet;
import java.util.Set;

public class BlockLocationManager {
    private static final Set<Location> powderSnowLocations = new HashSet<>();

    public static void addPowderSnowLocation(Location location) {
        powderSnowLocations.add(location);
    }

    public static Set<Location> getPowderSnowLocations() {
        return new HashSet<>(powderSnowLocations);
    }

    public static void clearLocations() {
        powderSnowLocations.clear();
    }
}

