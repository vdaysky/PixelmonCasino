package vdaysky.pixelmoncasino;

import com.sun.istack.internal.NotNull;
import org.spongepowered.api.effect.sound.SoundTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;

public class Animation {


    public final Wheel wheel1;
    public final Wheel wheel2;
    public final Wheel wheel3;

    public CasinoOutcome outcome;
    public int length;

    public Animation(ArrayList<ItemType> icons, int length, CasinoOutcome outcome)
    {
        for (ItemType[] wheel : outcome.visible) {
            System.out.println(Arrays.toString(wheel));
        }

        this.outcome = outcome;
        wheel1 = new Wheel(0, cast(icons), 1, 4, 1, SoundTypes.BLOCK_NOTE_CHIME, length, outcome.visible[0]);
        wheel2 = new Wheel(1, cast(icons), 4, 4, 1, SoundTypes.BLOCK_NOTE_BELL, length, outcome.visible[1]);
        wheel3 = new Wheel(2, cast(icons), 7, 4, 1, SoundTypes.BLOCK_NOTE_SNARE, length, outcome.visible[2]);

        this.length = length;
    }

    private ItemType[] cast(ArrayList<ItemType> l) {
        ItemType[] data = new ItemType[l.size()];
        for (int i = 0; i < l.size(); i++) {
            data[i] = l.get(i);
        }
        return data;
    }

    public void tickWheel(Player player, Wheel wheel, @NotNull Inventory gui)
    {
        wheel.spin();

        // could be closed
        if (gui == null) {
            return;
        }
        wheel.display(player, gui);
    }

    public ItemStack getWinningItem() {

        if (wheel1.queryItem(wheel1.getHeight()/2).getType() == wheel2.queryItem(wheel2.getHeight()/2).getType()){
            return wheel1.queryItem(wheel1.getHeight()/2);
        }

        return wheel3.queryItem(wheel3.getHeight()/2);
    }
}
