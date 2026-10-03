package org.ney.moongps.config;

import org.ney.moongps.config.message.Messages;
import org.ney.moongps.config.type.DirectionSettings;
import org.ney.moongps.config.type.DisplaySettings;
import org.ney.moongps.config.type.ReachSettings;
import org.ney.moongps.config.type.StorageSettings;

import java.util.List;

/**
 * Контракт конфигурации плагина.
 * Все значения кэшируются при загрузке и перезагрузке конфига.
 */
public interface MoonGPSConfig {

    boolean isNavigatorEnabled();

    boolean arePermissionsEnabled();

    boolean isOpBypassEnabled();

    boolean isCaseSensitive();

    boolean isOtherWorldRestricted();

    String getOtherWorldPermission();

    long getNavigationInterval();

    double getReachDistance();

    boolean shouldStopOnWorldChange();

    String getAutoStartGoal();

    boolean areWorldsRestricted();

    List<String> getAllowedWorlds();

    boolean isDownloadLibrariesEnabled();

    StorageSettings getStorageSettings();

    DirectionSettings getDirectionSettings();

    DisplaySettings getDisplaySettings();

    ReachSettings getReachSettings();

    String getPermissionUse();

    String getPermissionList();

    String getPermissionSet();

    String getPermissionDelete();

    String getPermissionReload();

    String getPrefix();

    Messages getOnlyPlayersMessage();

    Messages getNoPermissionMessage();

    Messages getUsageMessage();

    Messages getMarkNotFoundMessage();

    Messages getNavigationDisabledMessage();

    Messages getInvalidWorldMessage();

    Messages getMarkWorldNotLoadedMessage();

    Messages getAlreadyHasGoalMessage();

    Messages getAlreadyAtMarkMessage();

    Messages getEnabledMessage();

    Messages getDisabledMessage();

    Messages getStoppedWorldChangedMessage();

    Messages getListHeader();

    Messages getListEntryFormat();

    Messages getListOtherWorldEntryFormat();

    Messages getListFooter();

    Messages getListEmptyMessage();

    Messages getSetSuccessMessage();

    Messages getSetUpdatedMessage();

    Messages getSetInvalidNameMessage();

    Messages getDeleteSuccessMessage();

    Messages getReloadSuccessMessage();

}
