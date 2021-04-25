package vdaysky.pixelmoncasino;

import org.spongepowered.api.item.ItemType;

public class CasinoOutcome {

    public ItemType[][] visible;
    public ItemType winningItem;
    public int modifier;
    public int prize;

    public CasinoOutcome(ItemType[][] visible, ItemType winningItem, int modifier, int prize) {
        this.visible = visible;
        this.winningItem = winningItem;
        this.modifier = modifier;
        this.prize = prize;
    }
}
