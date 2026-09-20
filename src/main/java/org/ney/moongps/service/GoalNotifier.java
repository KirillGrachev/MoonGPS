package org.ney.moongps.service;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.ney.moongps.config.ConfigManager;
import org.ney.moongps.config.type.DisplaySettings;
import org.ney.moongps.config.type.ReachSettings;
import org.ney.moongps.model.Direction;
import org.ney.moongps.model.GPSGoal;
import org.ney.moongps.util.Placeholders;

/**
 * Сервис оповещения игрока о ходе навигации и о достижении метки.
 * Все методы вызываются из главного потока.
 */
public class GoalNotifier {

    private final ConfigManager configManager;
    private final MessageService messageService;
    private final DirectionService directionService;
    private final BossBarService bossBarService;

    public GoalNotifier(@NotNull ConfigManager configManager,
                        @NotNull MessageService messageService,
                        @NotNull DirectionService directionService,
                        @NotNull BossBarService bossBarService) {

        this.configManager = configManager;
        this.messageService = messageService;
        this.directionService = directionService;
        this.bossBarService = bossBarService;

    }

    /**
     * Показывает игроку направление до метки и расстояние
     * во все включённые каналы вывода.
     *
     * @param player    игрок
     * @param goal      метка
     * @param direction рассчитанное направление
     * @param distance  расстояние до метки
     */
    public void notifyNavigation(@NotNull Player player,
                                 @NotNull GPSGoal goal,
                                 @NotNull Direction direction,
                                 double distance) {

        DisplaySettings settings = configManager.getDisplaySettings();
        Placeholders placeholders = navigationPlaceholders(goal, direction, distance);

        if (settings.titleEnabled()) {
            messageService.sendTitle(player, settings.title(), placeholders);
        }

        if (settings.actionBarEnabled()) {
            messageService.sendActionBar(player, settings.actionBar(), placeholders);
        }

        if (settings.bossBarEnabled() && !settings.bossBar().isEmpty()) {

            bossBarService.update(player, placeholders.apply(settings.bossBar().values().get(0)), distance);
        }

        if (settings.chatEnabled()) {
            messageService.send(player, settings.chat(), placeholders);
        }

    }

    /**
     * Оповещает игрока о достижении метки.
     *
     * @param player игрок
     * @param goal   достигнутая метка
     */
    public void notifyGoalReached(@NotNull Player player, @NotNull GPSGoal goal) {

        ReachSettings settings = configManager.getReachSettings();
        DisplaySettings channels = configManager.getDisplaySettings();
        Placeholders placeholders = goalPlaceholders(goal);

        // Мастер-тумблер канала в settings.display гасит канал для любого вывода,
        // тумблер сообщения в messages.goal_reached - точечно для этого события
        if (channels.titleEnabled()) {
            messageService.sendTitle(player, settings.title(), placeholders);
        }

        if (channels.actionBarEnabled()) {
            messageService.sendActionBar(player, settings.actionBar(), placeholders);
        }

        if (channels.chatEnabled()) {
            messageService.send(player, settings.messages(), placeholders);
        }

        if (channels.bossBarEnabled() && !settings.bossBar().isEmpty()) {

            bossBarService.flash(player, placeholders.apply(settings.bossBar().values().get(0)),
                    settings.barColor(), settings.barStyle(), settings.showTime()
            );
        }

        playReachSound(player, settings);

    }

    private void playReachSound(@NotNull Player player, @NotNull ReachSettings settings) {

        ReachSettings.SoundEffect soundEffect = settings.sound();
        if (soundEffect == null) return;

        player.playSound(
                player.getLocation(),
                soundEffect.sound(),
                soundEffect.volume(),
                soundEffect.pitch()
        );

    }

    private @NotNull Placeholders navigationPlaceholders(@NotNull GPSGoal goal,
                                                         @NotNull Direction direction,
                                                         double distance) {

        return goalPlaceholders(goal)
                .add("direction", directionService.format(direction))
                .add("distance", String.valueOf((int) distance))
                .add("soliddist", String.valueOf((int) distance))
                .add("exact", String.format("%.1f", distance));

    }

    private @NotNull Placeholders goalPlaceholders(@NotNull GPSGoal goal) {

        return Placeholders.create()
                .add("gps", goal.name())
                .add("mark", goal.name())
                .add("world", goal.world())
                .add("x", String.valueOf((int) goal.x()))
                .add("y", String.valueOf((int) goal.y()))
                .add("z", String.valueOf((int) goal.z()));

    }
}
