/*
 * The MIT License (MIT)
 *
 *  Copyright © 2021, Alps BTE <bte.atchli@gmail.com>
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

package github.BTEPlotSystem.core.config;

public abstract class ConfigPaths {

    public static final String SPAWN_WORLD = "spawn-world";
    public static final String CHECK_FOR_UPDATES = "check-for-updates";
    public static final String DEV_MODE = "dev-mode";


    // Database
    private static final String DATABASE = "database.";
    public static final String DATABASE_URL = DATABASE + "url";
    public static final String DATABASE_NAME = DATABASE + "dbname";
    public static final String DATABASE_USERNAME = DATABASE + "username";
    public static final String DATABASE_PASSWORD = DATABASE + "password";


    // Servers
    public static final String SERVERS = "servers.";
    public static final String SERVERS_SCHEMATIC_PATH = ".schematic-path";

    private static final String SERVERS_FTP = ".ftp.";
    public static final String SERVERS_FTP_ENABLED = SERVERS_FTP + "enabled";
    public static final String SERVERS_FTP_ADDRESS = SERVERS_FTP + "address";
    public static final String SERVERS_FTP_PORT = SERVERS_FTP + "port";
    public static final String SERVERS_FTP_USERNAME = SERVERS_FTP + "username";
    public static final String SERVERS_FTP_PASSWORD = SERVERS_FTP + "password";


    // Countries
    public static final String COUNTRIES = "countries.";
    public static final String COUNTRIES_HEAD_ID = ".head-id";
    public static final String COUNTRIES_SERVER = ".server";


    // Holograms
    public static final String HOLOGRAMS = "holograms.";
    public static final String HOLOGRAMS_ENABLED = ".enabled";
    public static final String HOLOGRAMS_X = ".x";
    public static final String HOLOGRAMS_Y = ".y";
    public static final String HOLOGRAMS_Z = ".z";

    private static final String DISPLAY_OPTIONS = "display-options.";
    public static final String DISPLAY_OPTIONS_INTERVAL = DISPLAY_OPTIONS + "interval";
    public static final String DISPLAY_OPTIONS_SHOW_DAILY = DISPLAY_OPTIONS + "show-daily";
    public static final String DISPLAY_OPTIONS_SHOW_WEEKLY = DISPLAY_OPTIONS + "show-weekly";
    public static final String DISPLAY_OPTIONS_SHOW_MONTHLY = DISPLAY_OPTIONS + "show-monthly";
    public static final String DISPLAY_OPTIONS_SHOW_YEARLY = DISPLAY_OPTIONS + "show-yearly";


    // Navigator
    private static final String NAVIGATOR = "navigator.";
    public static final String NAVIGATOR_ITEM = NAVIGATOR + "item";
    public static final String NAVIGATOR_NAME = NAVIGATOR + "name";
    public static final String NAVIGATOR_DESCRIPTION = NAVIGATOR + "description";
    public static final String NAVIGATOR_COMMAND = NAVIGATOR + "command";


    // FORMATTING
    public static final String MESSAGE_PREFIX = "message-prefix";
    public static final String MESSAGE_INFO_COLOR = "info-color";
    public static final String MESSAGE_ERROR_COLOR = "error-color";


    // CONFIG VERSION
    public static final String CONFIG_VERSION = "config-version";
}
