package com.getfriedpig.golem.commands;

import com.getfriedpig.golem.fileio.ReadFromFile;
import com.getfriedpig.golem.fileio.WriteToFile;
import com.getfriedpig.golem.lox.Lox;
import com.getfriedpig.golem.lox.PrintCatcher;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import java.util.ArrayList;

public class LoxTestCommand extends AbstractPlayerCommand {
    public LoxTestCommand(@NonNullDecl String name, @NonNullDecl String description) {
        super(name, description);
    }

    @Override
    protected void execute(@NonNullDecl CommandContext commandContext,
                           @NonNullDecl Store<EntityStore> store,
                           @NonNullDecl Ref<EntityStore> ref,
                           @NonNullDecl PlayerRef playerRef,
                           @NonNullDecl World world) {
        PrintCatcher.ClearLog();

        WriteToFile.saveStringToFile("test.txt", "class Cake {\n" +
                "  taste() {\n" +
                "    var adjective = \"delicious\";\n" +
                "    print \"The \" + this.flavor + \" cake is \" + adjective + \"!\";\n" +
                "  }\n" +
                "}\n" +
                "\n" +
                "var cake = Cake();\n" +
                "cake.flavor = \"chocolate\";\n" +
                "cake.taste(); // Prints \"The German chocolate cake is delicious!\".");

        Lox.run(ReadFromFile.readFileAsString("test.txt"));
        ArrayList<String> log = PrintCatcher.GetLog();
        for (String s : log) {
            playerRef.sendMessage(Message.raw(s));
        }
    }
}
