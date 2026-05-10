package com.getfriedpig.golem.commands;

import com.getfriedpig.golem.fileio.FileManager;
import com.getfriedpig.golem.lox.Interpreter;
import com.getfriedpig.golem.lox.Lox;
import com.getfriedpig.golem.lox.PrintCatcher;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import java.util.ArrayList;

public class LoxTestCommand extends AbstractPlayerCommand {
    public LoxTestCommand(@NonNullDecl String name, @NonNullDecl String description) {
        super(name, description);
    }
    RequiredArg<String> fileNameArg = this.withRequiredArg("fileName", "File to run", ArgTypes.STRING);
    @Override
    protected void execute(@NonNullDecl CommandContext ctx,
                           @NonNullDecl Store<EntityStore> store,
                           @NonNullDecl Ref<EntityStore> ref,
                           @NonNullDecl PlayerRef playerRef,
                           @NonNullDecl World world) {
        PrintCatcher.ClearLog();
        String path = FileManager.getFilePath(playerRef.getUuid(), fileNameArg.get(ctx));
        Player player = store.getComponent(ref, Player.getComponentType());
        Interpreter.InterpreterContext interpreterContext = new Interpreter.InterpreterContext(
                player,
                playerRef,
                store,
                ref,
                world
        );
        try {
            Lox.runFile(path, interpreterContext);
        } catch (Exception e) {
            e.printStackTrace();
        }
        ArrayList<String> log = PrintCatcher.GetLog();
        for (String s : log) {
            playerRef.sendMessage(Message.raw(s));
        }

    }
}
