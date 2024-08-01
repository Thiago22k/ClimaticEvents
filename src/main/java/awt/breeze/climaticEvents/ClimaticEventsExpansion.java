package awt.breeze.climaticEvents;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

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
        return true;
    }

    @Override
    public @Nullable String onPlaceholderRequest(Player player, @NotNull String params) {
        if (player == null) {
            return "";
        }

        if (params.equalsIgnoreCase("next_event")) {
            long currentTime = System.currentTimeMillis();
            long timeRemaining = plugin.nextEventTime - currentTime;

            long ticksPerDay = 24000L;
            long ticksRemaining = timeRemaining / 50L;
            long daysRemaining = ticksRemaining / ticksPerDay;

            return String.format("%d", daysRemaining);
        }

        if (params.equalsIgnoreCase("status")) {
            if(plugin.enabled){
                return "Enabled";
            }else {
                return "Disabled";
            }
        }

        if(params.equalsIgnoreCase("current_mode")) {
            if (Objects.equals(plugin.getConfig().getString("mode"), "easy")) {
                return "Easy";
            } else if (Objects.equals(plugin.getConfig().getString("mode"), "normal")) {
                return "Normal";
            } else if (Objects.equals(plugin.getConfig().getString("mode"), "hard")) {
                return "Hard";
            } else if (Objects.equals(plugin.getConfig().getString("mode"), "hardcore")) {
                return "Hardcore";
            }
        }

        return null;
    }

}

