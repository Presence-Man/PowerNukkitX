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

package xxAROX.PresenceMan.PowerNukkitX.utils;

import cn.nukkit.entity.data.Skin;
import cn.nukkit.utils.BinaryStream;

import javax.annotation.Nullable;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;

public final class SkinUtils {
    public static @Nullable String convertSkinToBased64File(Skin skin) {
        BufferedImage image = fromSkinToImage(skin);
        if (image == null) return null;

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(image, "png", baos);
            byte[] imageData = baos.toByteArray();
            return Base64.getEncoder().encodeToString(imageData);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static BufferedImage fromSkinToImage(Skin skin) {
        byte[] skinData = skin.getSkinData().data;
        int width, height;

        switch (skinData.length) {
            case 8192: {
                width = 64;
                height = 32;
                break;
           }
            case 16384: {
                width = 64;
                height = 64;
                break;
            }
            case 32768:  {
                width = 128;
                height = 64;
                break;
            }
            case 65536:  {
                width = 128;
                height = 128;
                break;
            }
            default:  {
                return null;
            }
        }

        BinaryStream stream = new BinaryStream(skinData);
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int b = stream.getByte() & 0xFF;
                int g = stream.getByte() & 0xFF;
                int r = stream.getByte() & 0xFF;
                int a = stream.getByte() & 0xFF;

                int argb = (a << 24) | (b << 16) | (g << 8) | r;
                image.setRGB(x, y, argb);
            }
        }
        return image;
    }
}