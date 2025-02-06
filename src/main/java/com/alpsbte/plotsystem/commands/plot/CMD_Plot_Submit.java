/*
 * The MIT License (MIT)
 *
 *  Copyright © 2025, Alps BTE <bte.atchli@gmail.com>
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

package com.alpsbte.plotsystem.commands.plot;

import com.alpsbte.alpslib.utils.AlpsUtils;
import com.alpsbte.plotsystem.PlotSystem;
import com.alpsbte.plotsystem.commands.BaseCommand;
import com.alpsbte.plotsystem.commands.SubCommand;
import com.alpsbte.plotsystem.core.system.Builder;
import com.alpsbte.plotsystem.core.system.plot.AbstractPlot;
import com.alpsbte.plotsystem.core.system.plot.Plot;
import com.alpsbte.plotsystem.core.system.plot.utils.PlotUtils;
import com.alpsbte.plotsystem.utils.Utils;
import com.alpsbte.plotsystem.utils.enums.Status;
import com.alpsbte.plotsystem.utils.io.LangPaths;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;
import java.util.Objects;

import static net.kyori.adventure.text.Component.text;

public class CMD_Plot_Submit extends SubCommand {

    public CMD_Plot_Submit(BaseCommand baseCommand) {
        super(baseCommand);
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        try {
            Plot plot;
            @Nullable Player player = getPlayer(sender);
            if (args.length > 0 && AlpsUtils.tryParseInt(args[0]) != null) {
                int plotID = Integer.parseInt(args[0]);
                if (PlotUtils.plotExists(plotID)) {
                    plot = new Plot(plotID);
                } else {
                    sender.sendMessage(Utils.ChatUtils.getAlertFormat(langUtil.get(sender, LangPaths.Message.Error.PLOT_DOES_NOT_EXIST)));
                    return;
                }
            } else if (player != null && PlotUtils.isPlotWorld(player.getWorld())) {
                AbstractPlot p = PlotUtils.getCurrentPlot(Builder.byUUID(player.getUniqueId()));
                if (p instanceof Plot) {
                    plot = (Plot) p;
                } else {
                    sendInfo(sender);
                    return;
                }
            } else {
                sendInfo(sender);
                return;
            }

            if (Utils.isOwnerOrReviewer(sender, player, plot)) {
                if (Objects.requireNonNull(plot).getStatus() == Status.unfinished) {
                    PlotUtils.Actions.submitPlot(plot);

                    if (plot.getPlotMembers().isEmpty()) {
                        // Plot was made alone
                        langUtil.broadcast(LangPaths.Message.Info.FINISHED_PLOT, String.valueOf(plot.getID()), plot.getPlotOwner().getName());
                    } else {
                        // Plot was made in a group
                        StringBuilder sb = new StringBuilder(plot.getPlotOwner().getName() + ", ");

                        for (int i = 0; i < plot.getPlotMembers().size(); i++) {
                            sb.append(i == plot.getPlotMembers().size() - 1 ?
                                    plot.getPlotMembers().get(i).getName() :
                                    plot.getPlotMembers().get(i).getName() + ", ");
                        }
                        langUtil.broadcast(LangPaths.Message.Info.FINISHED_PLOT, String.valueOf(plot.getID()), sb.toString());
                    }

                    if (player != null) player.playSound(player.getLocation(), Utils.SoundUtils.FINISH_PLOT_SOUND, 1, 1);
                } else {
                    sender.sendMessage(Utils.ChatUtils.getAlertFormat(langUtil.get(sender, LangPaths.Message.Error.CAN_ONLY_SUBMIT_UNFINISHED_PLOTS)));
                }
            }
        } catch (SQLException ex) {
            sender.sendMessage(Utils.ChatUtils.getAlertFormat(langUtil.get(sender, LangPaths.Message.Error.ERROR_OCCURRED)));
            PlotSystem.getPlugin().getComponentLogger().error(text("A SQL error occurred!"), ex);
        }
    }

    @Override
    public String[] getNames() {
        return new String[]{"submit"};
    }

    @Override
    public String getDescription() {
        return "Submit a unfinished plot.";
    }

    @Override
    public String[] getParameter() {
        return new String[]{"ID"};
    }

    @Override
    public String getPermission() {
        return "plotsystem.plot.submit";
    }
}
