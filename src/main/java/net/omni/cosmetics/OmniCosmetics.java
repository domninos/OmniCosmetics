package net.omni.cosmetics;

import net.omni.cosmetics.commands.CosmeticsCommand;
import net.omni.cosmetics.commands.TrailCommand;
import net.omni.cosmetics.db.DatabaseManager;
import net.omni.cosmetics.listeners.BlockBreakListener;
import net.omni.cosmetics.listeners.GUIListener;
import net.omni.cosmetics.listeners.PlayerListener;
import net.omni.cosmetics.managers.*;
import net.omni.cosmetics.player.CosmeticsPlayer;
import net.omni.cosmetics.tasks.TrailTask;
import net.omni.cosmetics.util.ChatRenderer;
import net.omni.cosmetics.util.PaperChatRenderer;
import net.omni.cosmetics.util.SpigotChatRenderer;
import net.omni.cosmetics.util.config.ConfigUtil;
import net.omni.cosmetics.util.config.MessageUtil;
import net.omni.cosmetics.util.config.OCConfig;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class OmniCosmetics extends JavaPlugin {

    private ChatRenderer chatRenderer;

    private DatabaseManager databaseManager;
    private CosmeticsManager cosmeticsManager;
    private GUIManager guiManager;
    private BlockTrailManager blockTrailManager;
    private BenchmarkManager benchmarkManager;
    private PlayerManager playerManager;

    private OCConfig messagesConfig;
    private MessagesManager messagesManager;

    private ConfigUtil configUtil;

    private TrailTask trailTask;

    @Override
    public void onDisable() {
        benchmarkManager.stopAll();
        benchmarkManager.flush();

        trailTask.stop();
        blockTrailManager.stop();
        cosmeticsManager.flush();

        playerManager.saveAll();
        playerManager.flush();

        messagesManager.flush();

        databaseManager.closePool();

        guiManager.flush();

        sendConsole("<red>Successfully disabled.</red>");
    }

    @Override
    public void onEnable() {
        saveDefaultConfig();
        initChatRenderer();

        this.configUtil = new ConfigUtil(this);
        configUtil.load();

        this.messagesConfig = new OCConfig(this, "messages.yml");
        this.messagesManager = new MessagesManager(this);
        messagesManager.loadMessages();

        this.databaseManager = new DatabaseManager(this);
        databaseManager.initDatabase();

        this.cosmeticsManager = new CosmeticsManager(this);
        cosmeticsManager.initDirectories();
        cosmeticsManager.loadCosmetics();

        this.guiManager = new GUIManager(this);

        this.blockTrailManager = new BlockTrailManager(this);
        this.playerManager = new PlayerManager(this);

        CosmeticsAPI.init(this);

        this.trailTask = new TrailTask(this);
        trailTask.start();

        this.benchmarkManager = new BenchmarkManager(this);
        blockTrailManager.start();

        registerHooks();

        registerCommands();

        registerListeners();

        sendConsole("<green>Successfully started " + getDescription().getName() + "-v" + getDescription().getVersion() + " </green>");
    }

    private void initChatRenderer() {
        try {
            Class.forName("net.kyori.adventure.text.Component");
            this.chatRenderer = new PaperChatRenderer();
            sendConsole("<green>PaperMC detected. Using PaperChatRenderer.</green>");
        } catch (ClassNotFoundException e) {
            this.chatRenderer = new SpigotChatRenderer();
            sendConsole("<gray>Spigot detected. Using SpigotChatRenderer.</gray>");
        }

        MessageUtil.init(chatRenderer);
    }

    private void registerHooks() {
        // TODO worldguard
    }

    private void registerCommands() {
        new CosmeticsCommand(this).register();
        new TrailCommand(this).register();
    }

    private void registerListeners() {
        new PlayerListener(this).register();
        new GUIListener(this).register();
        new BlockBreakListener(this).register();
    }

    public void sendConsole(String message) {
        chatRenderer.sendMessage(Bukkit.getConsoleSender(), message);
    }

    public void reload() {
        configUtil.reloadConfig();
        messagesManager.loadMessages();
        cosmeticsManager.reloadCosmetics();
        benchmarkManager.rebuildPool();
        playerManager.saveAll();

        for (Player online : Bukkit.getOnlinePlayers()) {
            CosmeticsPlayer cp = playerManager.getPlayer(online.getUniqueId());

            if (cp == null) continue;

            if (cp.getActiveParticleTrail() != null)
                cp.setActiveParticleTrail(cosmeticsManager.getParticleTrail(cp.getActiveParticleTrail().getName()));

            if (cp.getActiveBlockTrail() != null)
                cp.setActiveBlockTrail(cosmeticsManager.getBlockTrail(cp.getActiveBlockTrail().getName()));

            if (cp.getActiveTag() != null)
                cp.setActiveTag(cosmeticsManager.getTag(cp.getActiveTag().getName()));

            if (cp.getActivePin() != null)
                cp.setActivePin(cosmeticsManager.getPin(cp.getActivePin().getName()));

            if (cp.getActiveChatColor() != null)
                cp.setActiveChatColor(cosmeticsManager.getChatColor(cp.getActiveChatColor().getName()));
        }

        trailTask.restart();
        blockTrailManager.restart();
        sendConsole("<green>Successfully reloaded.</green>");
    }

    public void sendMessage(CommandSender sender, String message) {
        chatRenderer.sendMessage(sender, message);
    }

    public OCConfig getMessagesConfig() {
        return messagesConfig;
    }

    public ConfigUtil getConfigUtil() {
        return configUtil;
    }

    public MessagesManager getMessagesManager() {
        return messagesManager;
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public CosmeticsManager getCosmeticsManager() {
        return cosmeticsManager;
    }

    public GUIManager getGuiManager() {
        return guiManager;
    }

    public BlockTrailManager getBlockTrailManager() {
        return blockTrailManager;
    }

    public BenchmarkManager getBenchmarkManager() {
        return benchmarkManager;
    }

    public PlayerManager getPlayerManager() {
        return playerManager;
    }

    public TrailTask getTrailTask() {
        return trailTask;
    }
}
