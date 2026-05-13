package com.getfriedpig.golem.commands;

import com.getfriedpig.golem.fileio.FileManager;
import com.getfriedpig.golem.typescript.TypeScript;
import com.google.gson.JsonObject;
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

/**
 * Command to compile and test TypeScript scripts.
 * Usage: /tstest <fileName>
 * 
 * Reads a TypeScript file from the player's script folder,
 * compiles it to bytecode, and displays the result.
 */
public class TypeScriptTestCommand extends AbstractPlayerCommand {
    public TypeScriptTestCommand(@NonNullDecl String name, @NonNullDecl String description) {
        super(name, description);
    }

    RequiredArg<String> fileNameArg = this.withRequiredArg("fileName", "TypeScript file to compile", ArgTypes.STRING);

    @Override
    protected void execute(@NonNullDecl CommandContext ctx,
                           @NonNullDecl Store<EntityStore> store,
                           @NonNullDecl Ref<EntityStore> ref,
                           @NonNullDecl PlayerRef playerRef,
                           @NonNullDecl World world) {
        
        String fileName = fileNameArg.get(ctx);
        String fileContent = FileManager.FileReadRequest(playerRef.getUuid(), fileName);
        
        if (fileContent == null) {
            playerRef.sendMessage(Message.raw("§c✗ File not found: " + fileName));
            return;
        }

        try {
            // Compile TypeScript to bytecode
            JsonObject bytecode = TypeScript.compile(fileContent);
            
            if (bytecode == null) {
                playerRef.sendMessage(Message.raw("§cCompilation failed"));
                return;
            }
            
            playerRef.sendMessage(Message.raw("§a✓ Compilation successful!"));
            // Don't spam the entire bytecode to chat - it's too long
            // playerRef.sendMessage(Message.raw("§7" + bytecode.toString()));
            
        } catch (Exception e) {
            playerRef.sendMessage(Message.raw("§cError: " + e.getMessage()));
            e.printStackTrace();
        }
    }
}
