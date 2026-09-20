package org.ney.moongps.config;

import org.bukkit.Sound;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.ney.moongps.MoonGPS;
import org.ney.moongps.config.message.Messages;
import org.ney.moongps.config.message.MoonTitle;
import org.ney.moongps.config.type.BossBarProgress;
import org.ney.moongps.config.type.DirectionMode;
import org.ney.moongps.config.type.SqlSettings;
import org.ney.moongps.config.type.StorageSettings;
import org.ney.moongps.config.type.StorageType;
import org.ney.moongps.config.type.DirectionSettings;
import org.ney.moongps.config.type.DisplaySettings;
import org.ney.moongps.config.type.ReachSettings;
import org.ney.moongps.util.HexColorUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Менеджер конфигурации плагина.
 * Загружает config.yml и кэширует все значения,
 * чтобы во время работы навигатора не читать файл повторно.
 */
public class ConfigManager implements MoonGPSConfig {

    private final MoonGPS plugin;
    private FileConfiguration config;

    private static final String PATH_ENABLED = "settings.enabled";
    private static final String PATH_PERMISSIONS_ENABLED = "settings.permissions.enabled";
    private static final String PATH_PERMISSION_USE = "settings.permissions.use";
    private static final String PATH_PERMISSION_LIST = "settings.permissions.list";
    private static final String PATH_PERMISSION_SET = "settings.permissions.set";
    private static final String PATH_PERMISSION_DELETE = "settings.permissions.delete";
    private static final String PATH_PERMISSION_RELOAD = "settings.permissions.reload";
    private static final String PATH_OP_BYPASS = "settings.permissions.op_bypass";
    private static final String PATH_STORAGE_TYPE = "settings.storage.type";
    private static final String PATH_SQL_HOST = "settings.storage.sql.host";
    private static final String PATH_SQL_PORT = "settings.storage.sql.port";
    private static final String PATH_SQL_DATABASE = "settings.storage.sql.database";
    private static final String PATH_SQL_TABLE = "settings.storage.sql.table";
    private static final String PATH_SQL_USER = "settings.storage.sql.user";
    private static final String PATH_SQL_PASSWORD = "settings.storage.sql.password";
    private static final String PATH_SQL_PROPERTIES = "settings.storage.sql.properties";
    private static final String PATH_CASE_SENSITIVE = "settings.marks.case_sensitive";
    private static final String PATH_OTHER_WORLD_RESTRICT = "settings.marks.other_world.restrict";
    private static final String PATH_OTHER_WORLD_PERMISSION = "settings.marks.other_world.permission";
    private static final String PATH_INTERVAL = "settings.navigation.interval";
    private static final String PATH_REACH_DISTANCE = "settings.navigation.reach_distance";
    private static final String PATH_STOP_ON_WORLD_CHANGE = "settings.navigation.stop_on_world_change";
    private static final String PATH_AUTO_START = "settings.navigation.auto_start";
    private static final String PATH_DIRECTION_MODE = "settings.direction.mode";
    private static final String PATH_DIRECTION_AHEAD = "settings.direction.ahead_threshold";
    private static final String PATH_DIRECTION_BEHIND = "settings.direction.behind_threshold";
    private static final String PATH_DIRECTION_FORMAT = "settings.direction.format";
    private static final String PATH_DIRECTION_COMPASS = "settings.direction.compass";
    private static final String PATH_TITLE_ENABLED = "settings.display.title.enabled";
    private static final String PATH_ACTION_BAR_ENABLED = "settings.display.action_bar.enabled";
    private static final String PATH_BOSS_BAR_ENABLED = "settings.display.boss_bar.enabled";
    private static final String PATH_CHAT_ENABLED = "settings.display.chat.enabled";
    private static final String PATH_BOSS_BAR_COLOR = "settings.display.boss_bar.color";
    private static final String PATH_BOSS_BAR_STYLE = "settings.display.boss_bar.style";
    private static final String PATH_BOSS_BAR_PROGRESS = "settings.display.boss_bar.progress";
    private static final String PATH_DISPLAY_FADE_IN = "settings.display.title.fade_in";
    private static final String PATH_DISPLAY_STAY = "settings.display.title.stay";
    private static final String PATH_DISPLAY_FADE_OUT = "settings.display.title.fade_out";
    private static final String PATH_REACH_BAR_COLOR = "settings.goal_reached.boss_bar.color";
    private static final String PATH_REACH_BAR_STYLE = "settings.goal_reached.boss_bar.style";
    private static final String PATH_REACH_BAR_SHOW_TIME = "settings.goal_reached.boss_bar.show_time";
    private static final String PATH_SOUND_ENABLED = "settings.goal_reached.sound.enabled";
    private static final String PATH_SOUND_NAME = "settings.goal_reached.sound.name";
    private static final String PATH_SOUND_VOLUME = "settings.goal_reached.sound.volume";
    private static final String PATH_SOUND_PITCH = "settings.goal_reached.sound.pitch";
    private static final String PATH_WORLDS_ENABLED = "settings.worlds.enabled";
    private static final String PATH_WORLDS_LIST = "settings.worlds.list";
    private static final String PATH_PREFIX = "messages.prefix";
    private static final String PATH_DIRECTION_RELATIVE = "messages.direction.relative";

    private static final long DEFAULT_INTERVAL = 8L;
    private static final double DEFAULT_REACH_DISTANCE = 2.0D;
    private static final double DEFAULT_AHEAD_THRESHOLD = 0.85D;
    private static final int DEFAULT_LIST_PER_PAGE = 10;
    private static final double DEFAULT_BEHIND_THRESHOLD = -0.85D;

    private boolean navigatorEnabled;
    private boolean permissionsEnabled;
    private boolean opBypassEnabled;
    private boolean caseSensitive;
    private boolean otherWorldRestricted;
    private boolean stopOnWorldChange;
    private boolean worldsRestricted;

    private long navigationInterval;
    private double reachDistance;

    private String autoStartGoal;

    private List<String> allowedWorlds;

    private String otherWorldPermission;

    private String permissionUse;
    private String permissionList;
    private String permissionSet;
    private String permissionDelete;
    private String permissionReload;

    private StorageSettings storageSettings;
    private DirectionSettings directionSettings;
    private DisplaySettings displaySettings;
    private ReachSettings reachSettings;

    private String prefix;

    private Messages onlyPlayersMessage;
    private Messages noPermissionMessage;
    private Messages usageMessage;
    private Messages unknownCommandMessage;
    private Messages markNotFoundMessage;
    private Messages navigationDisabledMessage;
    private Messages invalidWorldMessage;
    private Messages markWorldNotLoadedMessage;
    private Messages alreadyHasGoalMessage;
    private Messages enabledMessage;
    private Messages disabledMessage;
    private Messages stoppedWorldChangedMessage;
    private Messages listHeader;
    private Messages listFooter;
    private Messages listEmptyMessage;
    private Messages setSuccessMessage;
    private Messages setUpdatedMessage;
    private Messages setInvalidNameMessage;
    private Messages deleteSuccessMessage;
    private Messages reloadSuccessMessage;

    private Messages listEntryFormat;
    private Messages listOtherWorldEntryFormat;

    private int listPerPage;

    public ConfigManager(@NotNull MoonGPS plugin) {

        this.plugin = plugin;
        saveDefaultConfig();

        loadConfig();
        cacheConfigValues();

    }

    private void saveDefaultConfig() {
        plugin.saveDefaultConfig();
    }

    private void loadConfig() {

        File configFile = new File(plugin.getDataFolder(), "config.yml");
        config = YamlConfiguration.loadConfiguration(configFile);

    }

    private void cacheConfigValues() {

        navigatorEnabled = config.getBoolean(PATH_ENABLED, true);
        permissionsEnabled = config.getBoolean(PATH_PERMISSIONS_ENABLED, false);
        opBypassEnabled = config.getBoolean(PATH_OP_BYPASS, false);
        caseSensitive = config.getBoolean(PATH_CASE_SENSITIVE, false);
        otherWorldRestricted = config.getBoolean(PATH_OTHER_WORLD_RESTRICT, true);
        otherWorldPermission = config.getString(PATH_OTHER_WORLD_PERMISSION, "moongps.mark.other_world");
        stopOnWorldChange = config.getBoolean(PATH_STOP_ON_WORLD_CHANGE, true);
        worldsRestricted = config.getBoolean(PATH_WORLDS_ENABLED, true);

        navigationInterval = Math.max(1L, config.getLong(PATH_INTERVAL, DEFAULT_INTERVAL));
        reachDistance = Math.max(0.1D, config.getDouble(PATH_REACH_DISTANCE, DEFAULT_REACH_DISTANCE));

        autoStartGoal = config.getString(PATH_AUTO_START, "").trim();
        allowedWorlds = coloredList(config.getStringList(PATH_WORLDS_LIST));

        permissionUse = config.getString(PATH_PERMISSION_USE, "moongps.use");
        permissionList = config.getString(PATH_PERMISSION_LIST, "moongps.list");
        permissionSet = config.getString(PATH_PERMISSION_SET, "moongps.set");
        permissionDelete = config.getString(PATH_PERMISSION_DELETE, "moongps.delete");
        permissionReload = config.getString(PATH_PERMISSION_RELOAD, "moongps.reload");

        prefix = HexColorUtil.color(config.getString(PATH_PREFIX, ""));

        storageSettings = cacheStorageSettings();
        directionSettings = cacheDirectionSettings();
        displaySettings = cacheDisplaySettings();
        reachSettings = cacheReachSettings();

        onlyPlayersMessage = messages("messages.only_players");
        noPermissionMessage = messages("messages.no_permission");
        usageMessage = messages("messages.usage");
        unknownCommandMessage = messages("messages.unknown_command");
        markNotFoundMessage = messages("messages.mark_not_found");
        navigationDisabledMessage = messages("messages.navigation_disabled");
        invalidWorldMessage = messages("messages.invalid_world");
        markWorldNotLoadedMessage = messages("messages.mark_world_not_loaded");
        alreadyHasGoalMessage = messages("messages.already_has_goal");
        enabledMessage = messages("messages.enabled");
        disabledMessage = messages("messages.disabled");
        stoppedWorldChangedMessage = messages("messages.stopped_world_changed");
        listHeader = messages("messages.list.header");
        listFooter = messages("messages.list.footer");
        listEmptyMessage = messages("messages.list.empty");
        setSuccessMessage = messages("messages.set.success");
        setUpdatedMessage = messages("messages.set.updated");
        setInvalidNameMessage = messages("messages.set.invalid_name");
        deleteSuccessMessage = messages("messages.delete.success");
        reloadSuccessMessage = messages("messages.reload.success");

        listPerPage = Math.max(1, config.getInt("messages.list.per_page", DEFAULT_LIST_PER_PAGE));
        listEntryFormat = messages("messages.list.entry");
        listOtherWorldEntryFormat = messages("messages.list.entry_other_world");

    }

    private StorageSettings cacheStorageSettings() {

        SqlSettings sqlSettings = new SqlSettings(
                config.getString(PATH_SQL_HOST, "localhost"),
                config.getInt(PATH_SQL_PORT, 3306),
                config.getString(PATH_SQL_DATABASE, "moongps"),
                config.getString(PATH_SQL_TABLE, "moongps_marks"),
                config.getString(PATH_SQL_USER, "root"),
                config.getString(PATH_SQL_PASSWORD, ""),
                stringMap(config.getConfigurationSection(PATH_SQL_PROPERTIES))
        );
        return new StorageSettings(StorageType.of(config.getString(PATH_STORAGE_TYPE), StorageType.YAML), sqlSettings);

    }

    private @NotNull Map<String, String> stringMap(@Nullable ConfigurationSection section) {

        if (section == null) return Map.of();

        Map<String, String> result = new HashMap<>();

        section.getKeys(false).forEach(key ->
                result.put(key, section.getString(key, ""))
        );

        return Map.copyOf(result);

    }

    private DirectionSettings cacheDirectionSettings() {

        double aheadThreshold = config.getDouble(PATH_DIRECTION_AHEAD, DEFAULT_AHEAD_THRESHOLD);
        double behindThreshold = config.getDouble(PATH_DIRECTION_BEHIND, DEFAULT_BEHIND_THRESHOLD);

        return new DirectionSettings(
                DirectionMode.of(config.getString(PATH_DIRECTION_MODE), DirectionMode.RELATIVE),
                clampThreshold(aheadThreshold),
                clampThreshold(behindThreshold),
                config.getString(PATH_DIRECTION_FORMAT, "{arrow} {name}"),
                coloredMap(config.getConfigurationSection(PATH_DIRECTION_RELATIVE)),
                coloredMap(config.getConfigurationSection(PATH_DIRECTION_COMPASS))
        );

    }

    private DisplaySettings cacheDisplaySettings() {

        return new DisplaySettings(
                config.getBoolean(PATH_TITLE_ENABLED, true),
                config.getBoolean(PATH_ACTION_BAR_ENABLED, false),
                config.getBoolean(PATH_BOSS_BAR_ENABLED, false),
                config.getBoolean(PATH_CHAT_ENABLED, false),
                title("messages.navigation.title"),
                messages("messages.navigation.action_bar"),
                messages("messages.navigation.boss_bar"),
                messages("messages.navigation.text"),
                barColor(),
                barStyle(),
                BossBarProgress.of(config.getString(PATH_BOSS_BAR_PROGRESS), BossBarProgress.DISTANCE)
        );

    }

    private @NotNull BarColor barColor() {

        String configValue = config.getString(PATH_BOSS_BAR_COLOR, "BLUE");
        try {
            return BarColor.valueOf(configValue.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {

            plugin.getLogger().warning("Invalid boss_bar.color: '" + configValue + "'. Using default: BLUE.");
            return BarColor.BLUE;

        }

    }

    private @NotNull BarStyle barStyle() {

        String configValue = config.getString(PATH_BOSS_BAR_STYLE, "SOLID");
        try {
            return BarStyle.valueOf(configValue.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {

            plugin.getLogger().warning("Invalid boss_bar.style: '" + configValue + "'. Using default: SOLID.");
            return BarStyle.SOLID;

        }

    }

    private ReachSettings cacheReachSettings() {

        return new ReachSettings(
                reachDistance,
                title("messages.goal_reached.title"),
                messages("messages.goal_reached.text"),
                messages("messages.goal_reached.action_bar"),
                messages("messages.goal_reached.boss_bar"),
                reachBarColor(),
                reachBarStyle(),
                Math.max(1L, config.getLong(PATH_REACH_BAR_SHOW_TIME, 40L)),
                soundEffect()
        );

    }

    private @NotNull BarColor reachBarColor() {

        String configValue = config.getString(PATH_REACH_BAR_COLOR, "GREEN");
        try {
            return BarColor.valueOf(configValue.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {

            plugin.getLogger().warning("Invalid goal_reached boss_bar.color: '" + configValue + "'. Using default: GREEN.");
            return BarColor.GREEN;

        }

    }

    private @NotNull BarStyle reachBarStyle() {

        String configValue = config.getString(PATH_REACH_BAR_STYLE, "SOLID");
        try {
            return BarStyle.valueOf(configValue.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {

            plugin.getLogger().warning("Invalid goal_reached boss_bar.style: '" + configValue + "'. Using default: SOLID.");
            return BarStyle.SOLID;

        }

    }

    private @Nullable ReachSettings.SoundEffect soundEffect() {

        if (!config.getBoolean(PATH_SOUND_ENABLED, false)) return null;

        Sound sound = parseSound(config.getString(PATH_SOUND_NAME));
        if (sound == null) return null;

        float volume = (float) config.getDouble(PATH_SOUND_VOLUME, 1.0D);
        float pitch = (float) config.getDouble(PATH_SOUND_PITCH, 1.0D);

        return new ReachSettings.SoundEffect(sound, volume, pitch);

    }

    private @Nullable Sound parseSound(@Nullable String soundName) {

        if (soundName == null || soundName.isBlank()) return null;
        try {
            return Sound.valueOf(soundName.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {

            plugin.getLogger().warning("Unknown sound in config.yml: '" + soundName + "'. The sound has been disabled.");
            return null;

        }

    }

    private double clampThreshold(double threshold) {
        return Math.max(-1.0D, Math.min(1.0D, threshold));
    }

    /** Вспомогательные методы чтения конфига */

    private @NotNull Messages messages(@NotNull String sectionPath) {

        ConfigurationSection section = config.getConfigurationSection(sectionPath);
        if (section == null) return Messages.disabled();

        boolean enabled = section.getBoolean("enabled", true);
        if (!enabled) return Messages.disabled();

        return new Messages(coloredList(section.getStringList("text")), true);

    }

    private @NotNull MoonTitle title(@NotNull String sectionPath) {

        ConfigurationSection section = config.getConfigurationSection(sectionPath);
        if (section == null) return MoonTitle.disabled();

        boolean enabled = section.getBoolean("enabled", true);
        String titleText = section.getString("title");

        if (!enabled || titleText == null) return MoonTitle.disabled();

        return new MoonTitle(
                HexColorUtil.color(titleText),
                HexColorUtil.color(section.getString("subtitle", "")),
                section.getInt("fade_in", config.getInt(PATH_DISPLAY_FADE_IN, 0)),
                section.getInt("stay", config.getInt(PATH_DISPLAY_STAY, 40)),
                section.getInt("fade_out", config.getInt(PATH_DISPLAY_FADE_OUT, 10))
        );

    }

    private @NotNull List<String> coloredList(@Nullable List<String> values) {

        if (values == null || values.isEmpty()) return List.of();

        List<String> result = new ArrayList<>(values.size());
        values.forEach(value -> result.add(HexColorUtil.color(value)));

        return List.copyOf(result);

    }

    private @NotNull Map<String, String> coloredMap(@Nullable ConfigurationSection section) {

        if (section == null) return Map.of();

        Map<String, String> result = new HashMap<>();

        section.getKeys(false).forEach(key ->
                result.put(key.toUpperCase(Locale.ROOT), HexColorUtil.color(section.getString(key, "")))
        );

        return Map.copyOf(result);

    }

    /** Реализация контракта конфигурации */

    @Override
    public boolean isNavigatorEnabled() {
        return navigatorEnabled;
    }

    @Override
    public boolean arePermissionsEnabled() {
        return permissionsEnabled;
    }

    @Override
    public boolean isOpBypassEnabled() {
        return opBypassEnabled;
    }

    @Override
    public boolean isCaseSensitive() {
        return caseSensitive;
    }

    @Override
    public boolean isOtherWorldRestricted() {
        return otherWorldRestricted;
    }

    @Override
    public String getOtherWorldPermission() {
        return otherWorldPermission;
    }

    @Override
    public long getNavigationInterval() {
        return navigationInterval;
    }

    @Override
    public double getReachDistance() {
        return reachDistance;
    }

    @Override
    public boolean shouldStopOnWorldChange() {
        return stopOnWorldChange;
    }

    @Override
    public String getAutoStartGoal() {
        return autoStartGoal;
    }

    @Override
    public boolean areWorldsRestricted() {
        return worldsRestricted;
    }

    @Override
    public List<String> getAllowedWorlds() {
        return allowedWorlds;
    }

    @Override
    public StorageSettings getStorageSettings() {
        return storageSettings;
    }

    @Override
    public DirectionSettings getDirectionSettings() {
        return directionSettings;
    }

    @Override
    public DisplaySettings getDisplaySettings() {
        return displaySettings;
    }

    @Override
    public ReachSettings getReachSettings() {
        return reachSettings;
    }

    @Override
    public String getPermissionUse() {
        return permissionUse;
    }

    @Override
    public String getPermissionList() {
        return permissionList;
    }

    @Override
    public String getPermissionSet() {
        return permissionSet;
    }

    @Override
    public String getPermissionDelete() {
        return permissionDelete;
    }

    @Override
    public String getPermissionReload() {
        return permissionReload;
    }

    @Override
    public String getPrefix() {
        return prefix;
    }

    @Override
    public Messages getOnlyPlayersMessage() {
        return onlyPlayersMessage;
    }

    @Override
    public Messages getNoPermissionMessage() {
        return noPermissionMessage;
    }

    @Override
    public Messages getUsageMessage() {
        return usageMessage;
    }

    @Override
    public Messages getUnknownCommandMessage() {
        return unknownCommandMessage;
    }

    @Override
    public Messages getMarkNotFoundMessage() {
        return markNotFoundMessage;
    }

    @Override
    public Messages getNavigationDisabledMessage() {
        return navigationDisabledMessage;
    }

    @Override
    public Messages getInvalidWorldMessage() {
        return invalidWorldMessage;
    }

    @Override
    public Messages getMarkWorldNotLoadedMessage() {
        return markWorldNotLoadedMessage;
    }

    @Override
    public Messages getAlreadyHasGoalMessage() {
        return alreadyHasGoalMessage;
    }

    @Override
    public Messages getEnabledMessage() {
        return enabledMessage;
    }

    @Override
    public Messages getDisabledMessage() {
        return disabledMessage;
    }

    @Override
    public Messages getStoppedWorldChangedMessage() {
        return stoppedWorldChangedMessage;
    }

    @Override
    public Messages getListHeader() {
        return listHeader;
    }

    @Override
    public Messages getListEntryFormat() {
        return listEntryFormat;
    }

    @Override
    public Messages getListOtherWorldEntryFormat() {
        return listOtherWorldEntryFormat;
    }

    public int getListPerPage() {
        return listPerPage;
    }

    @Override
    public Messages getListFooter() {
        return listFooter;
    }

    @Override
    public Messages getListEmptyMessage() {
        return listEmptyMessage;
    }

    @Override
    public Messages getSetSuccessMessage() {
        return setSuccessMessage;
    }

    @Override
    public Messages getSetUpdatedMessage() {
        return setUpdatedMessage;
    }

    @Override
    public Messages getSetInvalidNameMessage() {
        return setInvalidNameMessage;
    }

    @Override
    public Messages getDeleteSuccessMessage() {
        return deleteSuccessMessage;
    }

    @Override
    public Messages getReloadSuccessMessage() {
        return reloadSuccessMessage;
    }

    /**
     * Перезагружает конфигурацию из файла и обновляет кэш значений.
     */
    public void reload() {

        plugin.reloadConfig();
        loadConfig();
        cacheConfigValues();

    }
}
