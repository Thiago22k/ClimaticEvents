package awt.breeze.climaticEvents;

import org.bukkit.entity.Player;

public class PanelPlayer {

    private Player player;
    private PanelMain section;

    public PanelPlayer(Player player) {
        this.player = player;
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public PanelMain getSection() {
        return section;
    }

    public void setSection(PanelMain section) {
        this.section = section;
    }
}
