package vdaysky.pixelmoncasino;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

public class RelaodCasinoConfigCommand implements CommandExecutor {
    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        PixelmonCasino.loadCasinos();
        src.sendMessage(Text.of(TextColors.GOLD, "[PixelmonCasino] ", TextColors.GREEN, "Config Reloaded Successfully."));
        return CommandResult.success();
    }
}
