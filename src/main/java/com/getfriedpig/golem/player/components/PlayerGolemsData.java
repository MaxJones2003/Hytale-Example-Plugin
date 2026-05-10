package com.getfriedpig.golem.player.components;

import com.getfriedpig.golem.GolemPlugin;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;
import java.util.stream.Collectors;


public class PlayerGolemsData implements Component<EntityStore> {
    private ArrayList<UUID> golems = new ArrayList<>();

    public static final BuilderCodec<PlayerGolemsData> CODEC = BuilderCodec
            .builder(PlayerGolemsData.class, PlayerGolemsData::new)
            .append(new KeyedCodec<>("Golems", Codec.STRING_ARRAY),
                    PlayerGolemsData::golemSetter,
                    PlayerGolemsData::golemGetter)
            .add()
            .build();


    public String[] golemGetter() {
        return golems.stream()
                .map(UUID::toString)
                .toArray(String[]::new);
    }

    public void golemSetter(String[] ids) {
        golems = Arrays.stream(ids)
                .map(UUID::fromString)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public void addGolem(UUID id) {
        golems.add(id);
    }

    public UUID getGolem(int index) {
        if (index >= golems.size()) {
            return null;
        }
        return golems.get(index);
    }

    public int getGolemCount() {
        return golems.size();
    }

    public void removeGolem(UUID id) {
        golems.remove(id);
    }


    @Override
    public Component<EntityStore> clone() {
        PlayerGolemsData c = new PlayerGolemsData();
        c.golems = new ArrayList<>(this.golems);

        return c;
    }

    public static ComponentType<EntityStore, PlayerGolemsData> getComponentType() {
        return GolemPlugin.playerGolemsData;
    }
}
