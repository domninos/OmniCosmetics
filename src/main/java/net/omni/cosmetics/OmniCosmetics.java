package net.omni.cosmetics;

import net.omni.cosmetics.commands.CosmeticsCommand;
import net.omni.cosmetics.db.DatabaseManager;
import net.omni.cosmetics.listeners.PlayerListener;
import net.omni.cosmetics.managers.MessagesManager;
import net.omni.cosmetics.util.ChatRenderer;
import net.omni.cosmetics.util.PaperChatRenderer;
import net.omni.cosmetics.util.SpigotChatRenderer;
import net.omni.cosmetics.util.config.ConfigUtil;
import net.omni.cosmetics.util.config.MessageUtil;
import net.omni.cosmetics.util.config.OCConfig;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public final class OmniCosmetics extends JavaPlugin {

    private ChatRenderer chatRenderer;

    private DatabaseManager databaseManager;

    private OCConfig messagesConfig;
    private MessagesManager messagesManager;

    private ConfigUtil configUtil;

    @Override
    public void onDisable() {

        messagesManager.flush();

        // close pool AFTER everything
        databaseManager.closePool();

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

        // load classes
        this.databaseManager = new DatabaseManager(this);
        databaseManager.initDatabase();

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

    }

    private void registerCommands() {
        new CosmeticsCommand(this).register();
    }

    private void registerListeners() {
        new PlayerListener(this).register();
    }

    public void sendConsole(String message) {
        chatRenderer.sendMessage(Bukkit.getConsoleSender(), chatRenderer.color(message));
    }

    public void sendMessage(CommandSender sender, String message) {
        chatRenderer.sendMessage(sender, chatRenderer.color(message));
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
}
