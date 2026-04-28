package com.getfriedpig.golem.ui;

import com.getfriedpig.golem.fileio.FileManager;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.protocol.packets.interface_.Page;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import java.util.ArrayList;

public class EditorPage extends InteractiveCustomUIPage<EditorPage.EditorEventData> {

    public static class EditorEventData {
        public String action;
        public String fileName;
        public String code;
        public int index;

        public static final BuilderCodec<EditorEventData> CODEC =
                BuilderCodec.builder(EditorEventData.class, EditorEventData::new)
                        .append(
                                new KeyedCodec<>("Action", Codec.STRING),
                                (obj, val) -> obj.action = val,
                                obj -> obj.action).add()
                        .append(
                                new KeyedCodec<>("@FileName", Codec.STRING),
                                (obj, val) -> obj.fileName = val,
                                obj -> obj.fileName).add()
                        .append(
                                new KeyedCodec<>("FileName", Codec.STRING),
                                (obj, val) -> obj.fileName = val,
                                obj -> obj.fileName).add()
                        .append(
                                new KeyedCodec<>("@Code", Codec.STRING),
                                (obj, val) -> obj.code = val,
                                obj -> obj.code).add()

                        .build();
    }

    PlayerRef playerRef;
    public EditorPage(PlayerRef playerRef) {
        super(playerRef, CustomPageLifetime.CanDismissOrCloseThroughInteraction, EditorEventData.CODEC);
        this.playerRef = playerRef;
    }

    @Override
    public void build(
            Ref<EntityStore> ref,
            UICommandBuilder cmd,
            UIEventBuilder events,
            Store<EntityStore> store
    ) {
        // 1. LOAD YOUR UI (this must match your UIBuilder asset path)
        cmd.append("Pages/EditorPage.ui");

        events.addEventBinding(
                CustomUIEventBindingType.Activating,
                "#SaveButton",
                new EventData().append("Action", "save").append("@FileName", "#FileNameInput.Value").append("@Code", "#EditorField.Value")
        );


        // 4. POPULATE FILE LIST (optional but important)
        java.util.UUID id = playerRef.getUuid();
        ArrayList<String> files = FileManager.getPlayerFiles(id);
        cmd.clear("#FileList");
        for (int i = 0; i < files.size(); i++) {

            String fileName = files.get(i);

            // Add a button from a template UI file
            cmd.append("#FileList", "Components/FileButton.ui");

            // Fill template values
            cmd.set("#FileList[" + i + "].Text", fileName);

            // Bind click event
            events.addEventBinding(
                    CustomUIEventBindingType.Activating,
                    "#FileList[" + i + "]",
                    new EventData().append("Action", "load").append("FileName", fileName).append("@Code", "#EditorField.Value")
            );
        }

    }

    @Override
    public void handleDataEvent(Ref<EntityStore> ref,
                                Store<EntityStore> store,
                                EditorEventData data) {

        Player player = store.getComponent(ref, Player.getComponentType());
        if (player == null) return;

        switch (data.action) {

            case "save":
                FileManager.FileWriteRequest(
                        playerRef.getUuid(),
                        data.fileName,
                        data.code
                );
                player.getPageManager().setPage(ref, store, Page.None);
                break;

            case "load":
                System.out.println("Loading file: " + data.fileName);
                String code = FileManager.FileReadRequest(
                        playerRef.getUuid(),
                        data.fileName
                );
                if (code == null)
                {
                    code = "";
                }
                UICommandBuilder update = new UICommandBuilder();
                update.set("#EditorField.Value", code);
                update.set("#FileNameInput.Value", data.fileName);

                sendUpdate(update);

                break;
        }
    }

}
