package vdaysky.pixelmoncasino;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.text.format.TextColor;
import org.spongepowered.api.text.format.TextColors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class CasinoConfiguration {
    public final float winThreshold;

    public final int COMMON;
    public final int RARE;
    public final int GOOD;
    public final int EPIC;
    public final int LEGEND;
    public final int JACKPOT;

    public final String GUI_NAME = "Casino";
    public final String SPINNER_GUI_NAME = "Spinner thing";
    public final String WINNINGS_TAB = "Winnings Table";
    public final String[] ownedGUIs = {GUI_NAME, SPINNER_GUI_NAME, WINNINGS_TAB};

    public final ItemType ICOMMON;
    public final ItemType IRARE;
    public final ItemType IGOOD;
    public final ItemType IEPIC;
    public final ItemType ILEGEND;
    public final ItemType IJACKPOT;

    public final LinkedHashMap<ItemType, Integer> winningsTable = new LinkedHashMap<>();
    public final HashMap<Integer, TextColor> colors = new HashMap<Integer, TextColor>();
    public final ArrayList<ItemType> images = new ArrayList<ItemType>();


    public CasinoConfiguration(int commonMul, int rareMul, int goodMul, int epicMul, int legendMul, int jackpotMul,
                               ItemType com, ItemType rare, ItemType good, ItemType epic, ItemType legend, ItemType jackpot,
                               TextColor commonCol, TextColor rareCol, TextColor goodCol, TextColor epicCol, TextColor legendCol, TextColor jackpotCol,
                               int commonCount, int rareCount, int goodCount, int epicCount, int legendCount, int jackpotCount,
                               float threshold)
    {
        this.winThreshold = threshold;

        COMMON = commonMul;
        RARE = rareMul;
        GOOD = goodMul;
        EPIC = epicMul;
        LEGEND = legendMul;
        JACKPOT = jackpotMul;

        ICOMMON = com;
        IRARE = rare;
        IGOOD = good;
        IEPIC = epic;
        ILEGEND = legend;
        IJACKPOT = jackpot;

        addX(ICOMMON, commonCount);
        addX(IRARE, rareCount);
        addX(IGOOD, goodCount);
        addX(IEPIC, epicCount);
        addX(ILEGEND, legendCount);
        addX(IJACKPOT, jackpotCount);

        winningsTable.put(ICOMMON, COMMON);
        winningsTable.put(IRARE, RARE);
        winningsTable.put(IGOOD, GOOD);
        winningsTable.put(IEPIC, EPIC);
        winningsTable.put(ILEGEND, LEGEND);
        winningsTable.put(IJACKPOT, JACKPOT);

        colors.put(COMMON, commonCol);
        colors.put(RARE, rareCol);
        colors.put(GOOD, goodCol);
        colors.put(EPIC, epicCol);
        colors.put(LEGEND, legendCol);
        colors.put(JACKPOT, jackpotCol);

    }

    private void addX(ItemType type, int count)
    {
        for (int i = 0; i < count; i++) {
            images.add(type);
        }
    }

}
