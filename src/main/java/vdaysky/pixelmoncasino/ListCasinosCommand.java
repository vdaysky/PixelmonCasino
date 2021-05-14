package vdaysky.pixelmoncasino;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColor;
import org.spongepowered.api.text.format.TextColors;

import java.util.HashMap;

public class ListCasinosCommand implements CommandExecutor {
    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {

        for (String preset : PixelmonCasino.getPresetNames()) {
            src.sendMessage(Text.of(TextColors.GREEN, "    ", preset));
            int c = 1;
            for (HashMap<String, Integer> loc : PixelmonCasino.getInstancesOfType(preset)) {
                src.sendMessage(Text.of(TextColors.YELLOW, c++, ". x: ", loc.get("x"), " y:" , loc.get("y"), " z: ", loc.get("z")));
            }
        }
        return null;
    }
}
