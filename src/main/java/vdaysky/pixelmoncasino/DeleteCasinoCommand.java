package vdaysky.pixelmoncasino;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.HashMap;

public class DeleteCasinoCommand implements CommandExecutor {
    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {

        Integer x = (Integer) args.getOne(Text.of("x")).get();
        Integer y = (Integer) args.getOne(Text.of("y")).get();
        Integer z = (Integer) args.getOne(Text.of("z")).get();

        HashMap<String, Integer> to_delete = null;
        String name_to_upd = null;

        for (String name : PixelmonCasino.getPresetNames()) {
            name_to_upd = name;

            for (HashMap<String, Integer> loc : PixelmonCasino.getInstancesOfType(name)) {

                if (loc.get("x").equals(x) && loc.get("y").equals(y) && loc.get("z").equals(z)) {
                    to_delete = loc;
                    break;
                }
            }
        }

        if (to_delete != null) {
            PixelmonCasino.deleteCasino(name_to_upd, to_delete);
            src.sendMessage(Text.of(TextColors.GOLD, "[PixelmonCasino] ", TextColors.GREEN, "Casino Deleted Successfully"));
            return CommandResult.success();
        }
        src.sendMessage(Text.of(TextColors.GOLD, "[PixelmonCasino] ", TextColors.RED, "Casino Does Not Exist"));

        return null;
    }
}
