package vdaysky.pixelmoncasino;

import com.sun.istack.internal.NotNull;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.effect.sound.SoundType;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.Slot;
import org.spongepowered.api.item.inventory.property.SlotPos;
import org.spongepowered.api.item.inventory.query.QueryOperationTypes;
import org.spongepowered.api.text.Text;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class Wheel
{
    public int index;
    private ItemType[] icons;
    public int spin = 0;
    private int x;
    private int height;
    private int wheelYOffset;
    private SoundType sound;

    private int delay_delta = 5;
    public int delay = 80;


    public Wheel(int idx, ItemType[] icons, int x, int height, int wheelYOffset, SoundType sound, int spins, ItemType[] visible)
    {
        this.icons = new ItemType[icons.length];

        ArrayList<ItemType> iclist = new ArrayList<>(Arrays.asList(icons));
        Collections.shuffle(iclist);

        int afterSpinOffset = spins % icons.length;

        // set all icons randomly
        for (int i = 0; i < icons.length; i++)
        {
                setItem(i, iclist.get(i));
        }

        // set actual screen
        for (int i = 0; i < visible.length; i++) {
            System.out.println("set screen: " + (i+afterSpinOffset) + " with " + visible[i] );
                setItem(afterSpinOffset+i, visible[i]);
        }

        index = idx;
        this.x = x;
        this.height = height;
        this.wheelYOffset = wheelYOffset;
        this.sound = sound;
    }

    public int getX()
    {
        return x;
    }

    public void spin()
    {
        delay += delay_delta;
        spin++;
    }

    public void setItem(int smartIdx, ItemType type) {

        smartIdx %= icons.length;
        if (smartIdx < 0 ) {
            setItem(icons.length - smartIdx, type);
        }
        else {
            icons[smartIdx] = type;
        }
    }

    public ItemStack queryItem(int i)
    {
        ItemType type = icons[ (spin + i) % icons.length ];
        ItemStack icon = ItemStack.of(type);
        icon.offer(Keys.DISPLAY_NAME, Text.of(Casino.config.colors.get(Casino.config.winningsTable.get(type)), "x", Casino.config.winningsTable.get(type)));
        return icon;
    }

    public void display(Player player, @NotNull Inventory gui) {

        player.playSound(sound, player.getPosition(), 0.1);

        for (int i = 0; i < height; i++)
        {
            Slot slot = gui.query(QueryOperationTypes.INVENTORY_PROPERTY.of(SlotPos.of(x, wheelYOffset + i )));
            slot.set(queryItem(i));
        }
    }

    public int getHeight() {
        return height;
    }
}
