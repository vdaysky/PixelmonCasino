package vdaysky.pixelmoncasino;

import com.google.inject.Inject;
import com.json.parsers.JSONParser;
import com.json.parsers.JsonParserFactory;
import com.json.serializers.MapSerializer;
import com.json.serializers.pojos.SerializerOptions;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameStoppedEvent;
import org.spongepowered.api.event.item.inventory.ClickInventoryEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.text.Text;

import java.io.*;
import java.nio.file.Path;
import java.util.*;

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
    public static HashMap<UUID, CasinoMachine> reg = new HashMap<>();
    private static HashMap<String, CasinoConfiguration> loadedPresets = new HashMap<>();
    private static HashMap<String,  ArrayList<HashMap<String, Integer>>> loadedInstances = new HashMap<>();

    public PixelmonCasino(){
        instance = this;
    }

    @Inject
    private Logger logger;

    @Inject
    @DefaultConfig(sharedRoot = true)
    private Path path;

    @Inject
    @DefaultConfig(sharedRoot = true)
    private ConfigurationLoader<CommentedConfigurationNode> loader;

    public static void registerPreset(String name, CasinoConfiguration config) {
        loadedPresets.put(name, config);
    }

    public static void registerCasino(String name, Integer x, Integer y, Integer z) {

        ArrayList<HashMap<String, Integer>> instances = loadedInstances.getOrDefault(name, new ArrayList<>());
        HashMap<String, Integer> loc = new HashMap<>();
        loc.put("x", x);
        loc.put("y", y);
        loc.put("z", z);
        instances.add(loc);
        loadedInstances.put(name, instances);
    }

    public static CasinoConfiguration getCasinoConfig(String name) {
        return loadedPresets.get(name);
    }

    public static Iterable<String> getPresetNames() {
        return loadedPresets.keySet();
    }

    public static ArrayList<HashMap<String, Integer>> getInstancesOfType(String name) {
        return loadedInstances.get(name);
    }

    public static void deleteCasino(String name, HashMap<String, Integer> loc) {
        loadedInstances.get(name).remove(loc);
    }

    @Listener
    public void onBlockInteract(final InteractBlockEvent event)
    {
        //If AIR or NONE then return
        if (!event.getTargetBlock().getLocation().isPresent())
            return;

        if (event.getTargetBlock().getState().getType() != BlockTypes.LEVER)
            return;

        Player player = event.getCause().first(Player.class).get();

        CasinoConfiguration cfg = getCasinoAt(
                event.getTargetBlock().getLocation().get().getBlockX(),
                event.getTargetBlock().getLocation().get().getBlockY(),
                event.getTargetBlock().getLocation().get().getBlockZ());

        if (cfg == null)
            return;

        CasinoMachine.create(player, cfg).showDialog();
        event.setCancelled(true);
    }

    public static CasinoConfiguration getCasinoAt(int x, int y, int z){
        for (String name : loadedInstances.keySet()) {
            for (HashMap<String, Integer> loc : loadedInstances.get(name)) {
                if (loc.get("x") == x && loc.get("y")  == y && loc.get("z")  == z) {
                   return loadedPresets.get(name);
                }
            }
        }
        return null;
    }

    @Listener
    public void startup(GameStoppedEvent e) {
        saveCasinos();
    }

    @Listener
    public void startup(GameStartedServerEvent e) throws IOException {

        loadCasinos();

        CommandSpec playCommandSpec = CommandSpec.builder()
                .description(Text.of("Play in casino command"))
                .permission("*").arguments(
                        GenericArguments.optional(
                                GenericArguments.integer(Text.of("amount")
                                )))
                .executor(new PlayCommand())
                .build();

        CommandSpec createCasinoCommandSpec = CommandSpec.builder()
                .description(Text.of("Register new casino"))
                .permission("pxc.casino.create").arguments(

                        GenericArguments.string(Text.of("preset")),

                        GenericArguments.integer(Text.of("x")),
                        GenericArguments.integer(Text.of("y")),
                        GenericArguments.integer(Text.of("z")),

                        GenericArguments.optional(
                                GenericArguments.integer(Text.of("fee"))
                                ))
                .executor(new CreateCasinoCommand())
                .build();

        CommandSpec reloadCasinos = CommandSpec.builder()
                .description(Text.of("Reload casino config"))
                .permission("pxc.casino.reload")
                .arguments()
                .executor(new RelaodCasinoConfigCommand())
                .build();

        CommandSpec saveCasinos = CommandSpec.builder()
                .description(Text.of("Save casino config"))
                .permission("pxc.casino.save")
                .arguments()
                .executor(new SaveCasinoConfigCommand())
                .build();

        CommandSpec listCasinos = CommandSpec.builder()
                .description(Text.of("List loaded casinos"))
                .permission("pxc.casino.list")
                .arguments()
                .executor(new ListCasinosCommand())
                .build();

        CommandSpec deleteCasino = CommandSpec.builder()
                .description(Text.of("Delete casino"))
                .permission("pxc.casino.delete")
                .arguments(
                        GenericArguments.integer(Text.of("x")),
                        GenericArguments.integer(Text.of("y")),
                        GenericArguments.integer(Text.of("z"))
                )
                .executor(new DeleteCasinoCommand())
                .build();

        Sponge.getCommandManager().register(this, playCommandSpec, "play");
        Sponge.getCommandManager().register(this, createCasinoCommandSpec, "casino-create", "ccreate");
        Sponge.getCommandManager().register(this, reloadCasinos, "casino-reload", "creload");
        Sponge.getCommandManager().register(this, saveCasinos, "casino-save", "csave");

        Sponge.getCommandManager().register(this, listCasinos, "casino-list", "clist");
        Sponge.getCommandManager().register(this, deleteCasino, "casino-delete", "cdel");
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

    public static void saveCasinos() {
        HashMap<String, Object> casinos = new HashMap<>();

        for (String name : loadedPresets.keySet()) {
            CasinoConfiguration config = loadedPresets.get(name);
            HashMap<String, Object> props = new HashMap<>();
            props.put("fee", config.fee);
            ArrayList<HashMap<String, Integer>> instances = new ArrayList<>();

            for (HashMap<String, Integer> x : loadedInstances.get(name)) {
                instances.add(new HashMap<>(x));
            }
            props.put("instances", instances);
            casinos.put(name, props);
        }

        MapSerializer ser = new MapSerializer();
        StringBuilder string = new StringBuilder();
        ser.serialize(casinos, string, new SerializerOptions());
        try {
            File config = new File("./mods/pixelmon-casino/", "config.json");
            if (!config.exists()) {
                config.createNewFile();
            }
            BufferedWriter bw = new BufferedWriter(new FileWriter(config));
            System.out.println("save config: " + string.toString());
            bw.write(string.toString());
            bw.close();
        } catch (Exception e){e.printStackTrace();}
    }

    static void loadCasinos() {
        File config = new File("./mods/pixelmon-casino/", "config.json");
        String config_data = null;
        if (!config.exists()) {
            try {
                config.createNewFile();
                BufferedWriter st = new BufferedWriter(new FileWriter(config));
                st.write("{}");
                st.close();
            } catch (Exception e) {e.printStackTrace();}
        }
        else {
            try {
            BufferedReader st = new BufferedReader(new FileReader(config));
            String line = st.readLine();
            StringBuilder json = new StringBuilder();
            while(line != null && !line.equals("")) {
                json.append(line);
                line = st.readLine();
            }
            config_data = json.toString();
            } catch (Exception e) {e.printStackTrace();}
        }

        JsonParserFactory factory = JsonParserFactory.getInstance();
        JSONParser parser = factory.newJsonParser();

        System.out.println("casino configuration:\n" + config_data);

        if (config_data == null)
            return;

        Map parsed = parser.parseJson(config_data);

        loadedPresets.clear();
        loadedInstances.clear();

        for (String name : (Set<String>) parsed.keySet()) {

            Map item = (Map) parsed.get(name);

            registerPreset(
                name,
                CasinoConfiguration.simple(Integer.parseInt((String) item.get("fee")))
            );

            for (HashMap<String, Object> coords : (List<HashMap<String, Object>>)item.get("instances"))

                registerCasino(
                        name,
                        Integer.parseInt((String) coords.get("x")),
                        Integer.parseInt((String) coords.get("y")),
                        Integer.parseInt((String) coords.get("z"))
                );
        }
    }
}
