package com.limeshulkerbox.fabricord.discord;

import com.limeshulkerbox.fabricord.api.v1.API;
import com.limeshulkerbox.fabricord.minecraft.ServerInitializer;
import me.lucko.spark.api.Spark;
import me.lucko.spark.api.SparkProvider;
import me.lucko.spark.api.statistic.StatisticWindow;
import me.lucko.spark.api.statistic.types.DoubleStatistic;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.CommandOutput;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.security.auth.login.LoginException;
import java.awt.*;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

import static com.limeshulkerbox.fabricord.minecraft.ServerInitializer.config;

public class ChatThroughDiscord extends ListenerAdapter {

    static String content;

    public ChatThroughDiscord() {
    }

    @Override
    public void onReady(@Nonnull ReadyEvent event) {
        ServerInitializer.jdaReady = true;
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        super.onMessageReceived(event);

        //Makes sure the Author is not a bot
        if (event.getAuthor().isBot()) return;

        Message message = event.getMessage();
        content = message.getContentRaw();

        if (content.startsWith("/")) {
            //Check correct channel
            if (!(event.getChannel().equals(event.getGuild().getTextChannelById(config.getChatChannelID())) && config.isCommandsInChatChannel() || event.getChannel().equals(event.getGuild().getTextChannelById(config.getConsoleChannelID())))) {
                event.getChannel().sendMessage("Sorry <@" + event.getMember().getId() + "> this is not the console channel or commands are not enabled here.").queue();
                return;
            }

            switch (content.toLowerCase(Locale.ROOT)) {
                case "/list" -> {
                    API.sendMessageToDiscord(getList(), event.getChannel());
                    return;
                }
                case "/tps" -> {
                    API.sendMessageToDiscord(getTps(), event.getChannel());
                    return;
                }
                case "/uptime" -> {
                    API.sendMessageToDiscord(API.getUpTime().toString(), event.getChannel());
                    return;
                }
            }

            if (!Objects.requireNonNull(event.getMember()).getRoles().contains(event.getGuild().getRoleById(config.getCommandsAccessRoleID()))) {
                event.getChannel().sendMessage("Sorry <@" + event.getMember().getId() + "> you don't have access to the console. If you believe you should have access, contact an Admin of this discord server.").queue();
                return;
            }

            runCommand(event);

            if (API.checkIfSomethingIsPresent(config.getCommandsForEveryone(), String.valueOf(event.getMessage()))) {
                runCommand(event);
            }
        } else {
            if (!event.getChannel().equals(event.getGuild().getTextChannelById(config.getChatChannelID())))
                return;
            //Send message to Minecraft chat
            API.sendMessage("[" + Objects.requireNonNull(event.getMember()).getUser().getName() + "] " + content, false, false, true);
        }
    }

    //Prompts
    public static void serverStartingMethod() {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle(config.getServerStartingPrompt());
        eb.setColor(Color.TRANSLUCENT);
        if (config.isOnlyWebhooks()) {
            if (config.isWebhooksEnabled()) {
                API.sendEmbedToDiscordChat(eb);
            } else if (config.isChatEnabled()) {
                API.sendMessage(config.getServerStartingPrompt(), false, true, false);
            }
        } else {
            if (config.isWebhooksEnabled()) {
                API.sendEmbedToDiscordChat(eb);
            }
            if (config.isChatEnabled()) {
                API.sendMessage(config.getServerStartingPrompt(), false, true, false);
            }
        }
    }

    public static void serverStartedMethod() {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle(config.getServerStartedPrompt());
        eb.setColor(Color.TRANSLUCENT);
        if (config.isOnlyWebhooks()) {
            if (config.isWebhooksEnabled()) {
                API.sendEmbedToDiscordChat(eb);
            } else if (config.isChatEnabled()) {
                API.sendMessage(config.getServerStartedPrompt(), false, true, false);
            }
        } else {
            if (config.isWebhooksEnabled()) {
                API.sendEmbedToDiscordChat(eb);
            }
            if (config.isChatEnabled()) {
                API.sendMessage(config.getServerStartedPrompt(), false, true, false);
            }
        }
    }

    public static void serverStoppingMethod() {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle(config.getServerStoppingPrompt());
        eb.setColor(Color.TRANSLUCENT);
        if (config.isOnlyWebhooks()) {
            if (config.isWebhooksEnabled()) {
                API.sendEmbedToDiscordChat(eb);
            } else if (config.isChatEnabled()) {
                API.sendMessage(config.getServerStoppingPrompt(), false, true, false);
            }
        } else {
            if (config.isWebhooksEnabled()) {
                API.sendEmbedToDiscordChat(eb);
            }
            if (config.isChatEnabled()) {
                API.sendMessage(config.getServerStoppingPrompt(), false, true, false);
            }
        }
    }

    public static void serverStoppedMethod() {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle(config.getServerStoppedPrompt());
        eb.setColor(Color.TRANSLUCENT);
        if (config.isOnlyWebhooks()) {
            if (config.isWebhooksEnabled()) {
                API.sendEmbedToDiscordChat(eb);
            } else if (config.isChatEnabled()) {
                API.sendMessage(config.getServerStoppedPrompt(), false, true, false);
            }
        } else {
            if (config.isWebhooksEnabled()) {
                API.sendEmbedToDiscordChat(eb);
            }
            if (config.isChatEnabled()) {
                API.sendMessage(config.getServerStoppedPrompt(), false, true, false);
            }
        }
    }

    public static void runCommand(@NotNull MessageReceivedEvent event) {
        //Register a new CommandManager
        CommandManager command = new CommandManager(CommandManager.RegistrationEnvironment.DEDICATED);
        try {
            //Attempt to send command
            CommandOutput cmdoutput = new CommandOutput() {
                @Override
                public void sendSystemMessage(Text message, UUID sender) {
                    API.sendMessageToDiscord(message.getString(), event.getChannel());
                }

                @Override
                public boolean shouldReceiveFeedback() {
                    return true;
                }

                @Override
                public boolean shouldTrackOutput() {
                    return true;
                }

                @Override
                public boolean shouldBroadcastConsoleToOps() {
                    return true;
                }
            };
            ServerCommandSource cmdsrc = new ServerCommandSource(cmdoutput, new Vec3d(0, 0, 0), new Vec2f(0, 0), API.getServerVariable().getOverworld(), 4, Objects.requireNonNull(event.getMember()).getNickname() + "on Discord", Text.of(Objects.requireNonNull(event.getMember()).getNickname() + "on Discord"), API.getServerVariable(), null);
            command.execute(cmdsrc, event.getMessage().getContentRaw());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void registerDiscordCommands() throws LoginException {
        //ServerInitializer.getDiscordApi().upsertCommand("updateconfigs", "Updates the configs for the MC server.").queue();
        //ServerInitializer.getDiscordApi().upsertCommand("list", "Shows who is on the MC server.").queue();
        //ServerInitializer.getDiscordApi().upsertCommand("tps", "Shows the TPS of the server.").queue();
        //ServerInitializer.getDiscordApi().upsertCommand("uptime", "Shows how long the server has been up for.").queue();
    }

    @Override
    public void onSlashCommand(SlashCommandEvent event) {
        if (!event.getName().equals("updateconfigs")) {
            ServerInitializer.updateConfigs();
            event.reply("The config has been updated successfully!").setEphemeral(true).queue(); // Queue both reply and edit
        }
        if (!event.getName().equals("list")) {
            event.reply(getList()).setEphemeral(true).queue();
        }
        if (!event.getName().equals("tps")) {
            event.reply(getTps()).setEphemeral(true).queue(); // Queue both reply and edit
        }
        if (!event.getName().equals("uptime")) {
            event.reply(API.getUpTime().toString()).setEphemeral(true).queue(); // Queue both reply and edit
        }
    }

    private String getList() {
        String[] playerNames = API.getServerVariable().getPlayerManager().getPlayerNames();
        StringBuilder formattedString = new StringBuilder("");
        if (API.getServerVariable().getCurrentPlayerCount() != 0) {
            formattedString.append("Players online are: ");
            for (int i = 0; i < playerNames.length - 1; i++) {
                formattedString.append(playerNames[i]).append(":").append(Objects.requireNonNull(API.getServerVariable().getPlayerManager().getPlayer(playerNames[i]))).append(" in ").append(Objects.requireNonNull(API.getServerVariable().getPlayerManager().getPlayer(playerNames[i])).getEntityWorld().getRegistryKey().getValue().toString()).append(", ");
            }
            formattedString.append(playerNames[playerNames.length - 1]).append(" in ").append(Objects.requireNonNull(API.getServerVariable().getPlayerManager().getPlayer(playerNames[playerNames.length - 1])).getEntityWorld().getRegistryKey().getValue().toString());
        }
        formattedString.append("\nThere are ");
        formattedString.append(API.getServerVariable().getCurrentPlayerCount());
        formattedString.append(" players out of ");
        formattedString.append(API.getServerVariable().getMaxPlayerCount());
        formattedString.append(" players.");
        return formattedString.toString();
    }

    private String getTps() {
        Spark spark = SparkProvider.get();
        if (spark != null) {
            DoubleStatistic<StatisticWindow.TicksPerSecond> tps = spark.tps();
            assert tps != null;
            double tpsLast10Secs = tps.poll(StatisticWindow.TicksPerSecond.SECONDS_10);
            double tpsLast5Mins = tps.poll(StatisticWindow.TicksPerSecond.MINUTES_5);
            return "The tps from the last 10 seconds: " + ((int) tpsLast10Secs) + "\nThe tps from the last 5 minutes: " + ((int) tpsLast5Mins);
        }
        return "";
    }
}