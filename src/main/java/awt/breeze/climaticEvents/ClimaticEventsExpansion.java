package awt.breeze.climaticEvents;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ClimaticEventsExpansion extends PlaceholderExpansion {
    private final ClimaticEvents plugin;

    public ClimaticEventsExpansion(ClimaticEvents plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "climaticevents";
    }

    @Override
    public @NotNull String getAuthor() {
        return plugin.getDescription().getAuthors().toString();
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true; // This is required to ensure that your expansion will be loaded again after a reload/restart.
    }

    @Override
    public @Nullable String onPlaceholderRequest(Player player, @NotNull String params) {
        if (player == null) {
            return "";
        }

        // Define your custom placeholders here
        if (params.equalsIgnoreCase("next_event")) {
            long currentTime = System.currentTimeMillis();
            long timeRemaining = plugin.nextEventTime - currentTime;

            // Convertir el tiempo restante a días de Minecraft
            long ticksPerDay = 24000L; // Ticks en un día de Minecraft
            long ticksRemaining = timeRemaining / 50L; // Convertir milisegundos a ticks de Minecraft
            long daysRemaining = ticksRemaining / ticksPerDay; // Convertir ticks a días de Minecraft

            return String.format("%d", daysRemaining);
        }

        // Add more placeholders as needed
        return null;
    }

}

