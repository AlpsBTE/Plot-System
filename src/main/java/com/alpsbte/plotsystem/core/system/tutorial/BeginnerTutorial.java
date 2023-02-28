package com.alpsbte.plotsystem.core.system.tutorial;

import com.alpsbte.plotsystem.core.system.Builder;
import org.bukkit.entity.Player;

import java.util.Collections;

public class BeginnerTutorial extends AbstractTutorial {

    public BeginnerTutorial(Builder builder) {
        super(builder);
        stages = Collections.singletonList(
                new Stage1()
        );
    }

    private class Stage1 extends AbstractStage {
        @Override
        void performStage() {

        }

        @Override
        public void onPlayerCommandInputEvent(Player player, String command) {
            if (command.startsWith("")) {

            }
        }
    }
}
