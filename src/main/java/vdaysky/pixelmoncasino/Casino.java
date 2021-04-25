package vdaysky.pixelmoncasino;

import com.pixelmonmod.pixelmon.Pixelmon;
import com.pixelmonmod.pixelmon.api.economy.IPixelmonBankAccount;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.DyeColors;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.item.inventory.ClickInventoryEvent;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.EmptyInventory;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.Slot;
import org.spongepowered.api.item.inventory.property.InventoryDimension;
import org.spongepowered.api.item.inventory.property.InventoryTitle;
import org.spongepowered.api.item.inventory.property.SlotPos;
import org.spongepowered.api.item.inventory.query.QueryOperationTypes;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColor;
import org.spongepowered.api.text.format.TextColors;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class Casino {

    private final Player _player;
    private Inventory _gui;
    private float winThreshold = 0.7f;

    private static final String GUI_NAME = "Casino";
    private static final String SPINNER_GUI_NAME = "Spinner thing";
    private static final String WINNINGS_TAB = "Winnings Table";
    private static final String[] ownedGUIs = {GUI_NAME, SPINNER_GUI_NAME, WINNINGS_TAB};
    public boolean active = false;

    public static final int COMMON = 2;
    public static final int RARE = 3;
    public static final int GOOD = 4;
    public static final int EPIC = 5;
    public static final int LEGEND = 6;
    public static final int JACKPOT = 10;

    public static final ItemType ICOMMON = Sponge.getRegistry().getType(ItemType.class, "pixelmon:poke_ball").get();
    public static final ItemType IRARE = Sponge.getRegistry().getType(ItemType.class, "pixelmon:great_ball").get();
    public static final ItemType IGOOD = Sponge.getRegistry().getType(ItemType.class, "pixelmon:premier_ball").get();
    public static final ItemType IEPIC = Sponge.getRegistry().getType(ItemType.class, "pixelmon:ultra_ball").get();
    public static final ItemType ILEGEND = Sponge.getRegistry().getType(ItemType.class, "pixelmon:love_ball").get();
    public static final ItemType IJACKPOT = Sponge.getRegistry().getType(ItemType.class, "pixelmon:master_ball").get();

    private static int casinoProfit = 0;

    public static final LinkedHashMap<ItemType, Integer> winningsTable = new LinkedHashMap<>();
    public static final HashMap<Integer, TextColor> colors = new HashMap<Integer, TextColor>();

    static {
        winningsTable.put(ICOMMON, COMMON);
        winningsTable.put(IRARE, RARE);
        winningsTable.put(IGOOD, GOOD);
        winningsTable.put(IEPIC, EPIC);
        winningsTable.put(ILEGEND, LEGEND);
        winningsTable.put(IJACKPOT, JACKPOT);

        colors.put(COMMON, TextColors.GREEN);
        colors.put(RARE, TextColors.DARK_GREEN);
        colors.put(GOOD, TextColors.YELLOW);
        colors.put(EPIC, TextColors.LIGHT_PURPLE);
        colors.put(LEGEND, TextColors.GOLD);
        colors.put(JACKPOT, TextColors.DARK_RED);
    }

    private static final ArrayList<ItemType> images = new ArrayList<ItemType>();

    static {
//        addX(ItemTypes.ANVIL, 9);
//        addX(ItemTypes.SAPLING, 6);
//        addX(ItemTypes.BOOK, 5);
//        addX(ItemTypes.BREAD, 3);
//        addX(ItemTypes.BRICK, 2);
//        addX(ItemTypes.GOLDEN_APPLE, 1);

        addX(ICOMMON, 9);
        addX(IRARE, 6);
        addX(IGOOD, 5);
        addX(IEPIC, 3);
        addX(ILEGEND, 2);
        addX(IJACKPOT, 1);

    }

    private final int fee;

    Slot confirmSlot;
    Slot cancelSlot;
    Slot winningsSlot;

    SlotPos confirmPos = SlotPos.of(2, 1);
    SlotPos cancelPos = SlotPos.of(6, 1);
    SlotPos winningsPos = SlotPos.of(4, 1);


    public Casino(Player player, int fee) {
        _player = player;
        this.fee = fee;
    }

    private static void addX(ItemType type, int count)
    {
        for (int i = 0; i < count; i++) {
            images.add(type);
        }
    }

    public void displayWinningTab() {
        Inventory tab = Inventory.builder()
                .property(InventoryDimension.of(9, 6))
                .property(InventoryTitle.of(Text.of(WINNINGS_TAB)))
                .build(PixelmonCasino.instance);

        ArrayList<ItemType> winningsIcons = new ArrayList<>(winningsTable.keySet());

        for (int i = 0; i < winningsTable.size(); i++) {
            int x = i%7 + 1;
            int y = i/7 + 1;

            int mod =  winningsTable.get(winningsIcons.get(i));
            Slot slot = tab.query(QueryOperationTypes.INVENTORY_PROPERTY.of(SlotPos.of(x, y)));
            ItemStack icon = ItemStack.of(winningsIcons.get(i));
            icon.offer(Keys.DISPLAY_NAME, Text.of( TextColors.DARK_BLUE, "Prize modifier: ", colors.get(mod),  "x" + mod));
            slot.set(icon);
        }

        Slot spin = tab.query(QueryOperationTypes.INVENTORY_PROPERTY.of(SlotPos.of(2, 5)));
        Slot close = tab.query(QueryOperationTypes.INVENTORY_PROPERTY.of(SlotPos.of(6, 5)));

        spin.set(createSpinButton());
        close.set(createCancelButton());

        _player.openInventory(tab);
    }

    private ItemStack createCancelButton()
    {
        ItemStack btn = ItemStack.of(ItemTypes.REDSTONE_BLOCK, 1);
        btn.offer(Keys.DISPLAY_NAME, Text.of(TextColors.DARK_RED, "Cancel"));
        return btn;
    }

    private int getBalance()
    {
        Optional<? extends IPixelmonBankAccount> account = Pixelmon.moneyManager.getBankAccount(_player.getUniqueId());
        return account.map(IPixelmonBankAccount::getMoney).orElse(0);
    }

    private ItemStack createSpinButton()
    {
        ItemStack btn = ItemStack.of(ItemTypes.EMERALD_BLOCK, 1);
        btn.offer(Keys.DISPLAY_NAME, Text.of(TextColors.DARK_GREEN, "Play (cost: ", TextColors.GOLD, fee, "$", TextColors.DARK_GREEN,
                ", balance: ", TextColors.GOLD, getBalance(), "$", TextColors.DARK_GREEN, ")"));
        return btn;
    }

    private ItemStack createTableWinningsButton()
    {
        ItemStack btn = ItemStack.of(ItemTypes.GOLD_BLOCK, 1);
        btn.offer(Keys.DISPLAY_NAME, Text.of(TextColors.GOLD, "Winnings Table"));
        return btn;
    }

    public void showDialog() {
        _gui = Inventory.builder()
                .property(InventoryDimension.of(9, 3))
                .property(InventoryTitle.of(Text.of(GUI_NAME)))
                .build(PixelmonCasino.instance);

        confirmSlot = _gui.query(QueryOperationTypes.INVENTORY_PROPERTY.of(confirmPos));
        cancelSlot = _gui.query(QueryOperationTypes.INVENTORY_PROPERTY.of(cancelPos));
        winningsSlot = _gui.query(QueryOperationTypes.INVENTORY_PROPERTY.of(winningsPos));

        ItemStack confirm = createSpinButton();
        ItemStack cancel = createCancelButton();
        ItemStack openWinnings = createTableWinningsButton();

        confirmSlot.set(confirm);
        cancelSlot.set(cancel);
        winningsSlot.set(openWinnings);

        _player.openInventory(_gui);
    }

    public void showAnimation(Runnable then, CasinoOutcome result) {
        Animation animation = new Animation(images, 40, result);

        Inventory gui = Inventory.builder()
                .property(InventoryDimension.of(9, 6))
                .property(InventoryTitle.of(Text.of(SPINNER_GUI_NAME)))
                .build(PixelmonCasino.instance);

        ItemStack placeholder = ItemStack.of(ItemTypes.STAINED_GLASS_PANE);
        placeholder.offer(Keys.DISPLAY_NAME, Text.of(" "));
        placeholder.offer(Keys.DYE_COLOR, DyeColors.YELLOW);

        gui.query(QueryOperationTypes.INVENTORY_PROPERTY.of(SlotPos.of(0, 3 ))).set(placeholder);
        gui.query(QueryOperationTypes.INVENTORY_PROPERTY.of(SlotPos.of(2, 3 ))).set(placeholder);
        gui.query(QueryOperationTypes.INVENTORY_PROPERTY.of(SlotPos.of(3, 3 ))).set(placeholder);
        gui.query(QueryOperationTypes.INVENTORY_PROPERTY.of(SlotPos.of(5, 3 ))).set(placeholder);
        gui.query(QueryOperationTypes.INVENTORY_PROPERTY.of(SlotPos.of(6, 3 ))).set(placeholder);
        gui.query(QueryOperationTypes.INVENTORY_PROPERTY.of(SlotPos.of(8, 3 ))).set(placeholder);

        _player.openInventory(gui);
        runWheels(animation, ()->{then.run(); finishAnimation(animation); givePrize(result);});
    }

    private void initWheel(Animation animation, Wheel wheel) {
        animation.tickWheel(_player, wheel, getActive(SPINNER_GUI_NAME));
    }

    private void spinWheel(Animation animation, Wheel wheel, Runnable then) {

        Inventory inventory = getActive(SPINNER_GUI_NAME);

        if (inventory == null || !active) {
            active = false;
            then.run();
            return;
        }

        if (wheel.spin < animation.length) {
            Task.builder().delay(wheel.delay, TimeUnit.MILLISECONDS).execute(() -> {
                animation.tickWheel(_player, wheel, inventory);
                spinWheel(animation, wheel, then);
            }).submit(PixelmonCasino.instance);
        } else {
            then.run();
        }
    }

    private void runWheels(Animation animation, Runnable then) {

        Task.builder().delay(100, TimeUnit.MILLISECONDS).execute(()->{

            spinWheel(animation, animation.wheel1, ()->{});
            initWheel(animation, animation.wheel2);
            initWheel(animation, animation.wheel3);

        }).submit(PixelmonCasino.instance);


        Task.builder().delay(2000, TimeUnit.MILLISECONDS).execute(() -> {
                    spinWheel(animation, animation.wheel2, () -> {});
                }).submit(PixelmonCasino.instance);

        Task.builder().delay(3000, TimeUnit.MILLISECONDS).execute(() -> {
            spinWheel(animation, animation.wheel3, then);
        }).submit(PixelmonCasino.instance);
    }

    private void finishAnimation(Animation animation) {
        Inventory inventory = getActive(SPINNER_GUI_NAME);

        if (inventory == null)
            return;

        Slot btn1 = inventory.query(QueryOperationTypes.INVENTORY_PROPERTY.of(SlotPos.of(2, 5 )));
        btn1.set(createSpinButton());

        Slot btn2 = inventory.query(QueryOperationTypes.INVENTORY_PROPERTY.of(SlotPos.of(4, 5 )));
        btn2.set(createTableWinningsButton());

        Slot btn3 = inventory.query(QueryOperationTypes.INVENTORY_PROPERTY.of(SlotPos.of(6, 5 )));
        btn3.set(createCancelButton());
    }


    public Inventory getActive(String name){
        if (_player.getOpenInventory().isPresent())
        {
            if (_player.getOpenInventory().get().first() instanceof EmptyInventory)
                return null;

            if (_player.getOpenInventory().get().first().getName().get().equalsIgnoreCase(name))
                return _player.getOpenInventory().get().first();
        }
        return null;
    }

    public void play() {

        Optional<? extends IPixelmonBankAccount> account = Pixelmon.moneyManager.getBankAccount(_player.getUniqueId());
        if (!account.isPresent()) {
            _player.sendMessage(Text.of(TextColors.RED, "You don't have Pixelmon bank account"));
            return;
        }
        int money = account.get().getMoney();

        if (money < fee) {
            _player.sendMessage(Text.of(TextColors.GOLD, "[PixelmonCasino] ", TextColors.RED, "You don't have enough money! You need " + fee + "$ to play!"));
            return;
        }
        casinoProfit += fee;
        active = true;

        account.get().setMoney(money - fee);
        _player.sendMessage(Text.of(TextColors.GOLD, "[PixelmonCasino] ", TextColors.DARK_AQUA, fee, "$ was taken from your account"));

        CasinoOutcome outcome = determineResult();

        showAnimation( ()->active=false, outcome );
    }

    private ItemType[][] createCasinoView(ItemType winning, ArrayList<ItemType> icons) {
        ItemType[][] view = new ItemType[3][4];

        for (int y = 0; y < 4; y++) {
            Pair<ItemType, Integer> item = getRandomItemIcon(icons);

            int count = item.getValue();
            ItemType type = item.getKey();

            for(int wheel = 0; wheel < 3; wheel++) {
                if (Math.random() < 0.4) {
                    view[wheel][y] = type;
                }
                else {
                    view[wheel][y] = getRandomItemIcon(icons).getKey();
                }
            }
        }

        if (winning != null) {
            _player.sendMessage(Text.of("set win item to " + winning));
            view[0][2] = winning;
            view[1][2] = winning;
            view[2][2] = winning;
        } else {

            // while there are all duplicates in win line
            while (new HashSet<>(Arrays.asList(view[0][2], view[1][2], view[2][2])).size() == 1) {
                ItemType type = getRandomItemIcon( icons ).getKey();
                int wheel = new Random().nextInt(3);
                view[wheel][2] = type;
            }
        }
        return view;
    }

    private Pair<ItemType, Integer> getRandomItemIcon(ArrayList<ItemType> icons) {
        int i = new Random().nextInt(icons.size());
        ItemType type = icons.get(i);
        int c = 0;
        for (int b = 0; b < icons.size(); b++){
            if (images.get(b) == type)
                c++;
        }

        return new Pair<>(type, c);
    }

    private ItemType getWinningType(int mod) {
        for (ItemType key : winningsTable.keySet()) {
            if (winningsTable.get(key) == mod)
                return key;
        }
        return null;
    }

    private CasinoOutcome determineResult()
    {
        _player.sendMessage(Text.of("Casino balance: " + casinoProfit));

        final CasinoOutcome fail = new CasinoOutcome( createCasinoView(null, images), null, 0, 0);

        if (casinoProfit < 0) {
            return fail;
        }

        int casinoProfitModifier = casinoProfit / fee;

        int safeMod = (int) Math.min( (casinoProfitModifier * 0.8), 10);
        _player.sendMessage(Text.of("safe mod: ", safeMod));

        if (safeMod >= 2) {

            int winningMod = 2 + new Random().nextInt( Math.max(safeMod-2, 0) );
            _player.sendMessage(Text.of("winningMod: ", winningMod));
            winningMod = (winningMod > 6) ? ( (winningMod < 10) ? 6 : 10 ) : winningMod;
            ItemType winning = getWinningType(winningMod);

            if (Math.random() > winThreshold) {
                _player.sendMessage(Text.of("Win: ", winningMod));

                return new CasinoOutcome(createCasinoView(winning, images), winning, winningMod, fee * winningMod);
            }
        }
        return fail;
    }

    private void givePrize(CasinoOutcome result)
    {
        if (result.prize == 0) {
            _player.sendMessage(Text.builder().append(Text.of(TextColors.GOLD, "[PixelmonCasino] ", TextColors.GRAY, "You won nothing! Congrats!")).build());
            return;
        }

        _player.sendMessage(Text.of(TextColors.GOLD, "[PixelmonCasino] ", TextColors.GREEN, "You won " + result.prize + "$!"));
        casinoProfit -= result.prize;

        Optional<? extends IPixelmonBankAccount> account = Pixelmon.moneyManager.getBankAccount(_player.getUniqueId());
        account.get().setMoney(account.get().getMoney() + result.prize);
    }

    public void close() {
        _player.closeInventory();
    }

    public void passEvent(ClickInventoryEvent e) {
        Inventory gui = e.getTargetInventory().first();

        boolean owned = false;
        for (String name : ownedGUIs)
        {
            if (gui.getName().get().equalsIgnoreCase(name)) {
                owned = true;
                break;
            }
        }

        if (owned) {
            e.setCancelled(true);
        }
        else {
            return;
        }

        if (!e.getSlot().isPresent())
        {
            return;
        }

        ItemType clickedType = e.getCursorTransaction().getFinal().getType();

        if (clickedType == ItemTypes.EMERALD_BLOCK) {
            play();
        }

        if (clickedType == ItemTypes.REDSTONE_BLOCK) {
            close();
        }

        if (clickedType == ItemTypes.GOLD_BLOCK) {
            displayWinningTab();
        }
    }
}
