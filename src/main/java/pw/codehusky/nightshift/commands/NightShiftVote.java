package pw.codehusky.nightshift.commands;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import pw.codehusky.nightshift.NightShift;

import java.util.Optional;

/**
 * Created by lokio on 12/18/2016.
 */
public class NightShiftVote implements CommandExecutor {
    private NightShift ns;
    public NightShiftVote(NightShift ns){
        this.ns = ns;
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        if(!ns.voteActive){
            src.sendMessage(Text.of(TextColors.BLUE,"NightShift",TextColors.RESET,":"," No vote is currently active!"));
            return CommandResult.success();
        }
        if(!(src instanceof Player)){
            return CommandResult.empty();
        }
        Player votee = (Player) src;
        if(ns.voted.contains(votee.getUniqueId())){
            src.sendMessage(Text.of(TextColors.BLUE,"NightShift",TextColors.RESET,":"," You already voted!"));
            return CommandResult.success();
        }
        Optional<Object> pending = args.getOne(Text.of("choice"));
        if(pending.isPresent()) {
            String choice = pending.get().toString().toLowerCase();
            if(choice.equals("y")||choice.equals("yes")){
                ns.votes++;
                ns.voted.add(votee.getUniqueId());
            }else if(choice.equals("n")||choice.equals("no")){
                ns.votes--;
                ns.voted.add(votee.getUniqueId());
            }else{
                src.sendMessage(Text.of(TextColors.BLUE,"NightShift",TextColors.RESET,":"," Invalid argument. Valid: yes or no"));
                return CommandResult.success();
            }
            src.sendMessage(Text.of(TextColors.BLUE,"NightShift",TextColors.RESET,":"," Your vote has been counted!"));
            return CommandResult.success();
        }else{
            src.sendMessage(Text.of(TextColors.BLUE,"NightShift",TextColors.RESET,":"," Invalid argument. Valid: yes or no"));
        }
        return CommandResult.success();
    }
}
