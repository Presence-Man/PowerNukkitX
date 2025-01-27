/*
 * Copyright (c) 2024. By Jan-Michael Sohn also known as @xxAROX.
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package xxAROX.PresenceMan.PowerNukkitX.entity;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum ActivityType {
    COMPETING("COMPETING"),
    LISTENING("LISTENING"),
    PLAYING("PLAYING"),
    STREAMING("STREAMING"),
    UNUSED("UNUSED"),
    ;
    private String value;
    @Override
    public String toString() {
        return value;
    }
}
