package vdaysky.pixelmoncasino;

import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.math.BigInteger;
import java.util.Optional;

public class PlayCommand implements CommandExecutor {

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) {

        if (! (src instanceof Player) )
            return CommandResult.success();

        Player player = ((Player) src);

        int amount = 30;
        Optional<BigInteger> arg = args.getOne("amount");

        if (arg.isPresent()) {
            int rawValue = arg.get().intValue();
            if (rawValue < 0) {
                player.sendMessage(Text.of("Haha nt wouldve worked in earlier version"));
                return CommandResult.success();
            }

            if (rawValue == 0) {
                player.sendMessage(Text.of(TextColors.RED, "0$ is not a valid bet"));
                return CommandResult.success();
            }
            amount = rawValue;
        }

        CasinoMachine existing = PixelmonCasino.reg.get(player.getUniqueId());

        if (existing != null && existing.active)
        {
            player.sendMessage(Text.of(TextColors.GOLD, "[PixelmonCasino] ", TextColors.RED, "Wait for your previous spin to finish!"));
            return CommandResult.success();
        }

        CasinoMachine cas = CasinoMachine.create(player, CasinoMachine.defaultConfig);
        PixelmonCasino.reg.put(player.getUniqueId(), cas);
        cas.showDialog();

        return CommandResult.success();
    }
}
