package pw.codehusky.nightshift;

import com.google.inject.Inject;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.scheduler.Scheduler;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.World;
import pw.codehusky.nightshift.commands.NightShiftVote;

import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Created by lokio on 12/18/2016.
 */
@Plugin(id="nightshift", name="NightShift",version = "1.0-SNAPSHOT",description = "Vote to skip to day!")
public class NightShift {
    @Inject
    private Logger logger;

    public ArrayList<UUID> voted = new ArrayList<>();
    public int votes = 0;
    public boolean voteActive = false;
    public long voteTime = -1;
    public long lastCheck = -1;
    public boolean voteChecked = false;
    @Listener
    public void onStartup(GameStartedServerEvent event){
        logger.info("Initalized NightShift.");
        CommandSpec voteSpec = CommandSpec.builder()
                .arguments(GenericArguments.string(Text.of("choice")))
                .description(Text.of("Vote for NightShift!"))
                .permission("nightshift.vote")
                .executor(new NightShiftVote(this))
                .build();

        Sponge.getCommandManager().register(this,voteSpec,"nightshiftvote","nsvote");
        Scheduler scheduler = Sponge.getScheduler();
        Task.Builder taskBuilder = scheduler.createTaskBuilder();
        taskBuilder.execute(new Runnable() {
            @Override
            public void run() {
                if(voteActive)
                    return;

                World w = (World )Sponge.getGame().getServer().getWorlds().toArray()[0];
                if(w.getProperties().getWorldTime()%24000 < lastCheck%24000){
                    voteChecked = false;
                }
                if(w.getProperties().getWorldTime()%24000 > 12541 && !voteChecked){ // bed use time
                    voteTime = new Date().getTime();
                    voteActive = true;
                    votes = 0;
                    voted = new ArrayList<UUID>();
                    Scheduler scheduler = Sponge.getScheduler();
                    Task.Builder upcoming = scheduler.createTaskBuilder();
                    upcoming.execute(new Runnable() {
                        @Override
                        public void run() {
                            World w = (World )Sponge.getGame().getServer().getWorlds().toArray()[0];
                            voteTime = -1;
                            voteActive = false;
                            if(votes > 0 && voted.size() > 0){
                                long t = w.getProperties().getWorldTime();
                                w.getProperties().setWorldTime(0);
                                Sponge.getServer().getBroadcastChannel().send(Text.of(TextColors.BLUE,"NightShift",TextColors.RESET,":"," You all have voted to skip night!"));
                            }else{
                                if(voted.size() < 1){
                                    Sponge.getServer().getBroadcastChannel().send(Text.of(TextColors.BLUE,"NightShift",TextColors.RESET,":"," Nobody voted! Continuing as normal."));
                                }else{
                                    Sponge.getServer().getBroadcastChannel().send(Text.of(TextColors.BLUE,"NightShift",TextColors.RESET,":"," You all have voted to continue into the night!"));
                                }
                            }
                            voteChecked = true;
                        }
                    }).delay(30,TimeUnit.SECONDS).name("Vote Timer").submit(Sponge.getPluginManager().getPlugin("nightshift").get());
                    Sponge.getServer().getBroadcastChannel().send(Text.of(TextColors.BLUE,"NightShift",TextColors.RESET,":", " NightShift now in effect! Type /nsvote yes or /nsvote no to vote to skip night!"));
                }
                lastCheck = w.getProperties().getWorldTime();
            }
        }).interval(5, TimeUnit.SECONDS).name("Time Scheduler").submit(this);
    }
}
