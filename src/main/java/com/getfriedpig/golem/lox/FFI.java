package com.getfriedpig.golem.lox;

import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.teleport.Teleport;

import java.util.List;

public class FFI {
	final Environment globals;
	FFI(Environment globals) {
		this.globals = globals;
		Init();
	}

	void Init() {
		initializeGlobalFunctions();
		initializeGlobalVariables();
	}

	void initializeGlobalFunctions() {
		globals.define("clock", new LoxCallable() {
			@Override
			public int arity() { return 0; }
			
			@Override
			public Object call(Interpreter interpreter, List<Object> arguments) {
				return (double)System.currentTimeMillis() / 1000.0;
			}
			
			@Override
			public String toString() { return "<native fn>"; }
		});
		
		globals.define("pow", new LoxCallable() {
			@Override
			public int arity() { return 2; }
			
			@Override
			public Object call(Interpreter interpreter, List<Object> arguments) {
				if (arguments.size() != 2)
					return 0;
				
				if(arguments.get(0) instanceof Double && arguments.get(1) instanceof Double)
					return Math.pow((Double)arguments.get(0),(Double)arguments.get(1));
				
				return 0;
			}
			
			@Override
			public String toString() { return "<native fn>"; }
		});

		globals.define("tp", new LoxCallable() {
			@Override
			public int arity() { return 0; }

			@Override
			public Object call(Interpreter interpreter, List<Object> arguments) {
				if (interpreter.context == null || interpreter.context.player == null)
					return 0;

				TransformComponent transform = interpreter.context.store.getComponent(interpreter.context.ref, TransformComponent.getComponentType());
                if(transform == null) return 0;
                Vector3d initialPosition = transform.getPosition();
				initialPosition.add(0, 1, 0);
				Teleport teleport = Teleport.createForPlayer(
						initialPosition,
						transform.getRotation()
				);
				interpreter.context.store.addComponent(interpreter.context.ref, Teleport.getComponentType(), teleport);
				return 0;
			}

			@Override
			public String toString() { return "<native fn>"; }
		});

		globals.define("print", new LoxCallable() {
			@Override
			public int arity() { return 1; }

			@Override
			public Object call(Interpreter interpreter, List<Object> arguments) {
				PrintCatcher.Print(arguments.get(0).toString());
				return null;
			}
		});

		//globals.define("")
	}
	
	void initializeGlobalVariables() {
		globals.define("pi", Math.PI);
	}
}
