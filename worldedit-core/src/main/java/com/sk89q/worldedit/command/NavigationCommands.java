/*
 * WorldEdit, a Minecraft world manipulation toolkit
 * Copyright (C) sk89q <http://www.sk89q.com>
 * Copyright (C) WorldEdit team and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.sk89q.worldedit.command;

import com.boydti.fawe.config.Caption;
import com.sk89q.worldedit.LocalConfiguration;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.command.util.CommandPermissions;
import com.sk89q.worldedit.command.util.CommandPermissionsConditionGenerator;
import com.sk89q.worldedit.command.util.Logging;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import org.enginehub.piston.annotation.Command;
import org.enginehub.piston.annotation.CommandContainer;
import org.enginehub.piston.annotation.param.Arg;
import org.enginehub.piston.annotation.param.Switch;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.sk89q.worldedit.command.util.Logging.LogMode.POSITION;

/**
 * Commands for moving the player around.
 */
@CommandContainer(superTypes = CommandPermissionsConditionGenerator.Registration.class)
public class NavigationCommands {

    private final WorldEdit worldEdit;

    /**
     * Create a new instance.
     *
     * @param worldEdit reference to WorldEdit
     */
    public NavigationCommands(WorldEdit worldEdit) {
        checkNotNull(worldEdit);
        this.worldEdit = worldEdit;
    }

    @Command(
        name = "unstuck",
        aliases = { "!", "/unstuck" },
        desc = "Escape from being stuck inside a block"
    )
    @CommandPermissions("worldedit.navigation.unstuck")
    public void unstuck(Player player) throws WorldEditException {
        player.findFreePosition();
        player.print(Caption.of("worldedit.unstuck.moved"));
    }

    @Command(
        name = "ascend",
        aliases = { "asc", "/asc", "/ascend" },
        desc = "Go up a floor"
    )
    @CommandPermissions("worldedit.navigation.ascend")
    public void ascend(Player player,
                       @Arg(desc = "# of levels to ascend", def = "1")
                           int levels) throws WorldEditException {
        int ascentLevels = 0;
        while (player.ascendLevel()) {
            ++ascentLevels;
            if (levels == ascentLevels) {
                break;
            }
        }
        if (ascentLevels == 0) {
            player.print(Caption.of("worldedit.ascend.obstructed"));
        } else {
            player.print(Caption.of("worldedit.ascend.moved", TextComponent.of(ascentLevels)));
        }
    }

    @Command(
        name = "descend",
        aliases = { "desc", "/desc", "/descend" },
        desc = "Go down a floor"
    )
    @CommandPermissions("worldedit.navigation.descend")
    public void descend(Player player,
                        @Arg(desc = "# of levels to descend", def = "1")
                            int levels) throws WorldEditException {
        int descentLevels = 0;
        while (player.descendLevel()) {
            ++descentLevels;
            if (levels == descentLevels) {
                break;
            }
        }
        if (descentLevels == 0) {
            player.print(Caption.of("worldedit.descend.obstructed"));
        } else {
            player.print(Caption.of("worldedit.descend.moved", TextComponent.of(descentLevels)));
        }
    }

    @Command(
        name = "ceil",
        aliases = { "/ceil", "/ceiling" },
        desc = "Go to the ceiling"
    )
    @CommandPermissions("worldedit.navigation.ceiling")
    @Logging(POSITION)
    public void ceiling(Player player,
                        @Arg(desc = "# of blocks to leave above you", def = "0")
                            int clearance,
                        @Switch(name = 'f', desc = "Force using flight to keep you still")
                            boolean forceFlight,
                        @Switch(name = 'g', desc = "Force using glass to keep you still")
                            boolean forceGlass) throws WorldEditException {
        clearance = Math.max(0, clearance);

        boolean alwaysGlass = getAlwaysGlass(forceFlight, forceGlass);
        if (player.ascendToCeiling(clearance, alwaysGlass)) {
            player.print(Caption.of("worldedit.ceil.moved"));
        } else {
            player.print(Caption.of("worldedit.ceil.obstructed"));
        }
    }

    @Command(
        name = "thru",
        aliases = { "/thru" },
        desc = "Pass through walls"
    )
    @CommandPermissions("worldedit.navigation.thru.command")
    public void thru(Player player) throws WorldEditException {
        if (player.passThroughForwardWall(6)) {
            player.print(Caption.of("worldedit.thru.moved"));
        } else {
            player.print(Caption.of("worldedit.thru.obstructed"));
        }
    }

    @Command(
        name = "jumpto",
        aliases = { "j", "/jumpto", "/j" },
        desc = "Teleport to a location"
    )
    @CommandPermissions("worldedit.navigation.jumpto.command")
    public void jumpTo(Player player,
        @Arg(desc = "Location to jump to", def = "")
            Location pos,
        @Switch(name = 'f', desc = "force teleport")
            boolean force) throws WorldEditException {

        if (pos == null) {
            pos = player.getSolidBlockTrace(300);
        }
        if (pos != null) {
            player.findFreePosition(pos);
            player.print(Caption.of("worldedit.jumpto.moved"));
        } else {
            player.print(Caption.of("worldedit.jumpto.none"));
        }
    }

    @Command(
        name = "up",
        aliases = { "/up" },
        desc = "Go upwards some distance"
    )
    @CommandPermissions("worldedit.navigation.up")
    @Logging(POSITION)
    public void up(Player player,
                   @Arg(desc = "Distance to go upwards")
                       int distance,
                   @Switch(name = 'f', desc = "Force using flight to keep you still")
                       boolean forceFlight,
                   @Switch(name = 'g', desc = "Force using glass to keep you still")
                       boolean forceGlass) throws WorldEditException {
        boolean alwaysGlass = getAlwaysGlass(forceFlight, forceGlass);
        if (player.ascendUpwards(distance, alwaysGlass)) {
            player.print(Caption.of("worldedit.up.moved"));
        } else {
            player.print(Caption.of("worldedit.up.obstructed"));
        }
    }

    /**
     * Helper function for /up and /ceil.
     *
     * @param forceFlight if flight should be used, rather than the default config option
     * @param forceGlass if glass should always be placed, rather than the default config option
     * @return true, if glass should always be put under the player
     */
    private boolean getAlwaysGlass(boolean forceFlight, boolean forceGlass) {
        final LocalConfiguration config = worldEdit.getConfiguration();

        return forceGlass || (config.navigationUseGlass && !forceFlight);
    }
}
