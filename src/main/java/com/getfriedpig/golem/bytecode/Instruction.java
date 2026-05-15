package com.getfriedpig.golem.bytecode;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Represents a single bytecode instruction
 */
public class Instruction {
    public String op;
    public JsonElement value;        // For PUSH_CONST
    public String name;              // For LOAD_VAR, STORE_VAR
    public Integer index;            // For LOAD_ARG, LOAD_LOCAL
    public Integer target;           // For JMP, JMP_IF, JMP_NOT
    public Integer function;         // For CALL
    public Integer args;             // For CALL, NATIVE_CALL
    public String method;            // For NATIVE_CALL
    public String namespace;         // For NATIVE_CALL (e.g., "Entity", "Player")
    public String className;         // For NATIVE_CALL (e.g., "EntityUtils", "PlayerManager")
    public String property;          // For OBJECT_GET, OBJECT_SET, PROP_GET, PROP_SET
    public String id;                // For CHECKPOINT
    public Integer size;             // For ARRAY_NEW
    public Integer classRef;         // For CLASS_DEF, NEW (class id)
    public Integer methodRef;        // For METHOD_CALL, STATIC_CALL, SUPER_CALL (method id)
    public String visibility;        // For properties and methods (public | private)
    public Boolean isStatic;         // For METHOD_CALL, STATIC_CALL
    public Boolean isGetter;         // For properties and method declarations
    public Boolean isSetter;         // For properties and method declarations

    public static Instruction fromJson(JsonObject json) {
        Instruction instr = new Instruction();
        instr.op = json.get("op").getAsString();
        
        if (json.has("value")) instr.value = json.get("value");
        if (json.has("name")) instr.name = json.get("name").getAsString();
        if (json.has("index")) instr.index = json.get("index").getAsInt();
        if (json.has("target")) instr.target = json.get("target").getAsInt();
        if (json.has("function")) instr.function = json.get("function").getAsInt();
        if (json.has("args")) instr.args = json.get("args").getAsInt();
        if (json.has("method")) instr.method = json.get("method").getAsString();
        if (json.has("namespace")) instr.namespace = json.get("namespace").getAsString();
        if (json.has("className")) instr.className = json.get("className").getAsString();
        if (json.has("property")) instr.property = json.get("property").getAsString();
        if (json.has("id")) instr.id = json.get("id").getAsString();
        if (json.has("size")) instr.size = json.get("size").getAsInt();
        if (json.has("classRef")) instr.classRef = json.get("classRef").getAsInt();
        if (json.has("methodRef")) instr.methodRef = json.get("methodRef").getAsInt();
        if (json.has("visibility")) instr.visibility = json.get("visibility").getAsString();
        if (json.has("isStatic")) instr.isStatic = json.get("isStatic").getAsBoolean();
        if (json.has("isGetter")) instr.isGetter = json.get("isGetter").getAsBoolean();
        if (json.has("isSetter")) instr.isSetter = json.get("isSetter").getAsBoolean();
        
        return instr;
    }

    @Override
    public String toString() {
        return "Instruction{" +
                "op='" + op + '\'' +
                (value != null ? ", value=" + value : "") +
                (name != null ? ", name='" + name + '\'' : "") +
                (target != null ? ", target=" + target : "") +
                '}';
    }
}
