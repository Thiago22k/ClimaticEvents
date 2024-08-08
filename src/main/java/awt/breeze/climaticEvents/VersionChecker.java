package awt.breeze.climaticEvents;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class VersionChecker {
    private final JavaPlugin plugin;
    private final String currentVersion;
    private final String modrinthProjectId;

    public VersionChecker(JavaPlugin plugin, String currentVersion, String modrinthProjectId) {
        this.plugin = plugin;
        this.currentVersion = currentVersion;
        this.modrinthProjectId = modrinthProjectId;
    }

    public void checkForUpdates() {
        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    String apiUrl = "https://api.modrinth.com/v2/project/" + modrinthProjectId + "/version";
                    HttpURLConnection connection = (HttpURLConnection) new URL(apiUrl).openConnection();
                    connection.setRequestMethod("GET");
                    connection.setConnectTimeout(5000);
                    connection.setReadTimeout(5000);

                    BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = in.readLine()) != null) {
                        response.append(line);
                    }
                    in.close();

                    String responseString = response.toString();
                    String latestVersion = parseLatestVersion(responseString);

                    if (!currentVersion.equals(latestVersion)) {
                        Bukkit.getScheduler().runTask(plugin, () -> {
                            for (Player player : Bukkit.getOnlinePlayers()) {
                                if (player.hasPermission("climaticevents.admin")) {
                                    player.sendMessage(ChatColor.RED + "A new version of " + plugin.getName() + " is available: " + latestVersion + ". You are running version: " + currentVersion);
                                }
                            }
                            plugin.getLogger().warning("A new version of " + plugin.getName() + " is available: " + latestVersion + ". You are running version: " + currentVersion);
                        });
                    }
                } catch (Exception e) {
                    plugin.getLogger().warning("Failed to check for updates: " + e.getMessage());
                }
            }
        }.runTaskAsynchronously(plugin);
    }

    private String parseLatestVersion(String responseString) {
        if (responseString.startsWith("[") && responseString.endsWith("]")) {
            String trimmedResponse = responseString.substring(1, responseString.length() - 1);

            int startIndex = trimmedResponse.indexOf("{");
            int endIndex = trimmedResponse.indexOf("}", startIndex);

            if (startIndex != -1 && endIndex != -1) {
                String jsonObject = trimmedResponse.substring(startIndex, endIndex + 1);

                String versionKey = "\"version_number\":\"";
                int versionStartIndex = jsonObject.indexOf(versionKey);

                if (versionStartIndex != -1) {
                    versionStartIndex += versionKey.length();
                    int versionEndIndex = jsonObject.indexOf("\"", versionStartIndex);

                    if (versionEndIndex != -1) {
                        return jsonObject.substring(versionStartIndex, versionEndIndex);
                    }
                }
            }
        }

        return "unknown";
    }


}
