package vdaysky.pixelmoncasino;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColor;
import org.spongepowered.api.text.format.TextColors;

import java.math.BigInteger;
import java.util.Optional;

public class CreateCasinoCommand implements CommandExecutor {
    @Override
    public CommandResult execute(CommandSource src, CommandContext args) {

        if (! (src instanceof Player) )
            return CommandResult.success();

        Player player = ((Player) src);

        Optional<String> preset = args.getOne("preset");
        Optional<Integer> fee = args.getOne("fee");
        Optional<Integer> x = args.getOne("x");
        Optional<Integer> y = args.getOne("y");
        Optional<Integer> z = args.getOne("z");

        if (PixelmonCasino.getCasinoAt(x.get(), y.get(), z.get()) != null) {
            player.sendMessage(Text.of(TextColors.GOLD, "[PixelmonCasino] ", TextColors.RED, "Casino at that location already exists!"));
            return CommandResult.success();
        }

        if (PixelmonCasino.getCasinoConfig(preset.get()) == null) {
            if (!fee.isPresent()) {
                player.sendMessage(Text.of(TextColors.RED, "Preset ", preset.get(), " does not exist! specify optional params to create one."));
                return CommandResult.success();
            }

            PixelmonCasino.registerPreset(preset.get(), CasinoConfiguration.simple(fee.get()));
            player.sendMessage(Text.of(TextColors.GOLD, "[PixelmonCasino] ", TextColors.GREEN, "Preset ", TextColors.YELLOW, preset.get(), TextColors.GREEN, " successfully added"));
        }

        PixelmonCasino.registerCasino(preset.get(), x.get(), y.get(), z.get());

        player.sendMessage(Text.of(TextColors.GOLD, "[PixelmonCasino] ", TextColors.GREEN, "New casino at ",
                TextColors.YELLOW, x.get() + " "  + y.get() + " " + z.get(),
                TextColors.GREEN, " with preset ",
                TextColors.YELLOW, preset.get(),
                TextColors.GREEN, " successfully added")
        );

        return CommandResult.success();
    }
}
