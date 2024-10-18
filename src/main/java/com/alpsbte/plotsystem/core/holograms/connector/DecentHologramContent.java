/*
 * The MIT License (MIT)
 *
 *  Copyright © 2023, ASEAN Build The Earth <bteasean@gmail.com>
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */

package com.alpsbte.plotsystem.core.holograms.connector;

import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.UUID;

/**
 * Display contents the hologram will be using.<br/>
 * [1] ItemStack - Display item at the top of hologram.<br/>
 * [2] Title - Header message atop the hologram.<br/>
 * [3] Content - the main content to display.<br/>
 * [4] Footer - footer line, this is default as a separator line.
 */
public interface DecentHologramContent {
    /**
     * Display entity as minecraft item.
     * This will be display at the top of hologram
     *
     * @return Minecraft ItemStack
     */
    ItemStack getItem();

    /**
     * Title message as String.
     *
     * @param playerUUID Focused player.
     * @return The message.
     */
    String getTitle(UUID playerUUID);

    /**
     * Header message as DataLine.
     * By default, this is using the value from getTitle as a header with a separator line
     *
     * @param playerUUID Focused player.
     * @return The DataLine.
     */
    List<DecentHologramDisplay.DataLine<?>> getHeader(UUID playerUUID);

    /**
     * Main content to be written in the hologram
     *
     * @param playerUUID Focused player.
     * @return The DataLine.
     */
    List<DecentHologramDisplay.DataLine<?>> getContent(UUID playerUUID);

    /**
     * The footer line at the bottom of the hologram.
     * By default, this is a separator line.
     *
     * @param playerUUID Focused player.
     * @return The DataLine.
     */
    List<DecentHologramDisplay.DataLine<?>> getFooter(UUID playerUUID);
}