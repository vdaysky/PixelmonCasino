package vdaysky.pixelmoncasino;

import com.google.inject.Inject;
import net.minecraftforge.fml.common.event.FMLServerStartedEvent;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.command.SendCommandEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.item.inventory.ClickInventoryEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.Slot;
import org.spongepowered.api.item.inventory.property.InventoryDimension;
import org.spongepowered.api.item.inventory.property.SlotPos;
import org.spongepowered.api.item.inventory.query.QueryOperationTypes;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.text.Text;

import java.text.ParseException;
import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;
// todo  /play amount cancel and spin again buttons spin sound winnings tab
@Plugin(
        id = "pixelmon-casino",
        name = "Pixelmon Casino",
        description = "A plugin that adds casino to pixelmon mod",
        url = "https://example.com",
        authors = {
                "vdaysky"
        }
)
public class PixelmonCasino {

    public static PixelmonCasino instance;
    public static HashMap<UUID, Casino> reg = new HashMap<>();

    public PixelmonCasino(){
        instance = this;
    }

    @Inject
    private Logger logger;


    @Listener
    public void startup(GameStartedServerEvent e){

        CommandSpec myCommandSpec = CommandSpec.builder()
                .description(Text.of("Play in casino command"))
                .permission("*").arguments(
                        GenericArguments.optional(
                                GenericArguments.bigInteger(Text.of("amount")
                                )))
                .executor(new PlayCommand())
                .build();

        Sponge.getCommandManager().register(this, myCommandSpec, "play");
    }

    @Listener
    public void playerleft(ClientConnectionEvent.Disconnect e)
    {
        Optional<Player> player = e.getCause().first(Player.class);
        reg.remove(player.get());
    }

    @Listener
    public void click(ClickInventoryEvent e)
    {
        Optional<Player> player = e.getCause().first(Player.class);

        if (reg.containsKey(player.get().getUniqueId()))
        {
            reg.get(player.get().getUniqueId()).passEvent(e);
        }
    }
}
