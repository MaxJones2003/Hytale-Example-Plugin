package com.getfriedpig.golem;

import com.getfriedpig.golem.commands.LoxTestCommand;
import com.getfriedpig.golem.commands.LoxTextEditorCommand;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class GolemPlugin extends JavaPlugin {
    public GolemPlugin(@NonNullDecl JavaPluginInit init) {
        super(init);
    }

    @Override
    public void setup() {
        super.setup();
        var commandRegistry = getCommandRegistry();
        commandRegistry.registerCommand(new LoxTestCommand("loxtest", "Tests the parser/interpreter"));
        commandRegistry.registerCommand(new LoxTextEditorCommand());

        /*0OpenCustomUIInteraction.registerCustomPageSupplier(this, EditorPage.EditorPageEventData.class, "EditorPage",
                (PlayerRef playerRef, ) -> {
                    // Create and return the page instance
                    // Return null to cancel opening the page
                    return new EditorPage(playerRef);
                });*/
    }
}
