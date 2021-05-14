package vdaysky.pixelmoncasino;

public class CasinoMachine {

    public Casino casino;
    public Integer profit = 0;

    public CasinoStructure(String name, CasinoConfiguration config, int x, int y, int z) {
        this.name = name;
        this.x = x;
        this.y = y;
        this.z = z;
        this.config = config;
    }
}
