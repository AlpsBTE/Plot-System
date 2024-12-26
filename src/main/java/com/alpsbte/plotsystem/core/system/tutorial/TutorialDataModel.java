/*
 * The MIT License (MIT)
 *
 *  Copyright © 2023, Alps BTE <bte.atchli@gmail.com>
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

package com.alpsbte.plotsystem.core.system.tutorial;

import java.sql.SQLException;
import java.util.Date;
import java.util.UUID;

public interface TutorialDataModel {
    /**
     * Gets the ID of the individual tutorial from your storage system (database or config).
     *
     * @return id, negative if not set
     */
    int getID();

    /**
     * Gets the UUID of the player who started the tutorial. The UUID is 36 characters long and contains 4 dashes.
     *
     * @return uuid of the player
     */
    UUID getPlayerUUID() throws SQLException;

    /**
     * The ID which is assigned to the tutorial. This ID is used to identify the tutorial in the system.
     *
     * @return tutorial id, negative if not set
     */
    int getTutorialID() throws SQLException;

    /**
     * Gets the highest stage id which the player has completed. The stage id starts at 0.
     *
     * @return stage id, negative if not set
     */
    int getStageID() throws SQLException;

    /**
     * Checks if the player has completed all stages of the tutorial.
     *
     * @return true if the player has completed the tutorial otherwise false
     */
    boolean isCompleted() throws SQLException;

    /**
     * Gets the date when the player created the tutorial.
     *
     * @return create date
     */
    Date getCreationDate() throws SQLException;

    /**
     * Gets the date when the player last completed a stage.
     *
     * @return last stage completion date
     */
    Date getLastStageCompletionDate() throws SQLException;

    /**
     * Gets the date when the player completed the tutorial.
     *
     * @return completion date
     */
    Date getCompletionDate() throws SQLException;
}
