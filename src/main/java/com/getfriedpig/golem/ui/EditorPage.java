package com.getfriedpig.golem.ui;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public class EditorPage extends InteractiveCustomUIPage<EditorPage.EditorEventData> {

    public static class EditorEventData {
        public String action;
        public String code;

        public static final BuilderCodec<EditorEventData> CODEC =
                BuilderCodec.builder(EditorEventData.class, EditorEventData::new)
                        .append(new KeyedCodec<>("Action", Codec.STRING),
                                (EditorEventData o, String v) -> o.action = v,
                                (EditorEventData o) -> o.action)
                        .add()
                        .append(new KeyedCodec<>("@Code", Codec.STRING),
                                (EditorEventData o, String v) -> o.code = v,
                                (EditorEventData o) -> o.code)
                        .add()
                        .build();
    }

    public EditorPage(PlayerRef playerRef) {
        super(playerRef, CustomPageLifetime.CanDismissOrCloseThroughInteraction, EditorEventData.CODEC);
    }

    @Override
    public void build(
            Ref<EntityStore> ref,
            UICommandBuilder commandBuilder,
            UIEventBuilder eventBuilder,
            Store<EntityStore> store
    ) {
        commandBuilder.append("Pages/TextEditorPage.ui");

        eventBuilder.addEventBinding(
                CustomUIEventBindingType.Activating,
                "#Save",
                new EventData()
                        .append("Action", "Save")
                        .append("@Code", "#CodeEditor.Value")
        );
    }

    @Override
    public void handleDataEvent(
            Ref<EntityStore> ref,
            Store<EntityStore> store,
            EditorEventData data
    ) {
        if ("Save".equals(data.action)) {
            String code = data.code;

            // TODO: Save logic here
        }

        //Player player = store.getComponent(ref, Player.getComponentType());
        //player.getPageManager().setPage(ref, store, Page.None);
    }
}
