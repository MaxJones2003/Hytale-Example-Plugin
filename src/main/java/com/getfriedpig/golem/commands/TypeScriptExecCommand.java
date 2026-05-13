package com.getfriedpig.golem.commands;

import com.getfriedpig.golem.bytecode.BytecodeVM;
import com.getfriedpig.golem.bytecode.VMResult;
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
 * Command to compile and execute TypeScript scripts as bytecode.
 * Usage: /tsexec <fileName>
 * 
 * Reads a TypeScript file, compiles to bytecode, and executes it in the VM.
 */
public class TypeScriptExecCommand extends AbstractPlayerCommand {
    public TypeScriptExecCommand(@NonNullDecl String name, @NonNullDecl String description) {
        super(name, description);
    }

    RequiredArg<String> fileNameArg = this.withRequiredArg("fileName", "TypeScript file to execute", ArgTypes.STRING);

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
            playerRef.sendMessage(Message.raw("§7Compiling..."));
            JsonObject bytecode = TypeScript.compile(fileContent);
            
            if (bytecode == null) {
                playerRef.sendMessage(Message.raw("§cCompilation failed"));
                return;
            }
            
            playerRef.sendMessage(Message.raw("§aCompilation successful!"));
            
            // Load bytecode into VM
            playerRef.sendMessage(Message.raw("§7Loading bytecode..."));
            BytecodeVM vm = BytecodeVM.load(bytecode);
            vm.setMaxInstructionsPerFrame(10000);
            
            // Execute bytecode
            playerRef.sendMessage(Message.raw("§7Executing..."));
            VMResult result = vm.execute();
            
            switch (result.status) {
                case COMPLETED:
                    playerRef.sendMessage(Message.raw("§a✓ Execution completed"));
                    if (result.result != null) {
                        playerRef.sendMessage(Message.raw("§7Result: " + result.result));
                    }
                    break;
                    
                case PAUSED:
                    playerRef.sendMessage(Message.raw("§6⊕ Paused at checkpoint: " + result.checkpointId));
                    playerRef.sendMessage(Message.raw("§7State: " + result.state));
                    break;
                    
                case ERROR:
                    playerRef.sendMessage(Message.raw("§c✗ Execution error: " + result.errorMessage));
                    break;
                    
                default:
                    playerRef.sendMessage(Message.raw("§c✗ Unknown execution status"));
            }
            
        } catch (Exception e) {
            playerRef.sendMessage(Message.raw("§cError: " + e.getMessage()));
            e.printStackTrace();
        }
    }
}
