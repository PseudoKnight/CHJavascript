package com.chjavascript.core;

import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.annotations.api;
import com.laytonsmith.core.MSLog;
import com.laytonsmith.core.MSVersion;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.constructs.CArray;
import com.laytonsmith.core.constructs.CString;
import com.laytonsmith.core.constructs.Construct;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.exceptions.CRE.CREPluginInternalException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.functions.AbstractFunction;
import com.laytonsmith.core.natives.interfaces.Mixed;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;

/**
 *
 */
public class Functions {

	private static ScriptEngine engine;
	private static final String NO_JAVASCRIPT_MESSAGE = "No javascript engine seems to be installed on this system. The "
		 + "CHJavascript extension will not work.";

	private static ScriptEngine getEngine(){
		if(engine == null){
			ScriptEngineManager factory = new ScriptEngineManager();
			engine = factory.getEngineByName("JavaScript");
			if (engine == null) {
				MSLog.GetLogger().e(MSLog.Tags.EXTENSIONS, NO_JAVASCRIPT_MESSAGE, Target.UNKNOWN);
			}
		}
		return engine;
	}

	@api
	public static class javascript extends AbstractFunction {


		public Class[] thrown() {
			return new Class[]{CREPluginInternalException.class};
		}

		public boolean isRestricted() {
			return true;
		}

		public Boolean runAsync() {
			return null;
		}

		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			getEngine();
			String script = args[0].val();
			CArray env = new CArray(t);
			CArray toReturn = new CArray(t);
			if (args.length > 1) {
				env = Static.getArray(args[1], t);
			}
			if (args.length > 2) {
				toReturn = Static.getArray(args[2], t);
			}
			CArray ret = CArray.GetAssociativeArray(t);
			Bindings b = engine.createBindings();
			try {
				for(String key : env.stringKeySet()){
					b.put(key, Construct.GetPOJO(env.get(key, t)));
				}
				engine.setBindings(b, ScriptContext.GLOBAL_SCOPE);
				engine.eval(script);
			} catch(Exception e){
				throw new CREPluginInternalException(e.getMessage(), t, e);
			}
			for(Mixed key : toReturn.keySet()){
				String k = toReturn.get(key, t).val();
				Object var = b.get(k);
				ret.set(k, Construct.GetConstruct(var), t);
			}
			return ret;
		}

		public String getName() {
			return "javascript";
		}

		public Integer[] numArgs() {
			return new Integer[]{1, 2, 3};
		}

		public String docs() {
			return "array {script, [environment, [toReturn]]} Runs a javascript script. The script can set variables beforehand with the environment"
				+ " variable, which should be an associative array mapping variable names to values. Arrays are not directly supported,"
				+ " as everything is simply passed in as a string. Values can be returned from the script, by giving a list of named values"
				+ " to toReturn, which will cause those values to be returned as a part of the associative array returned.";
		}

		public Version since() {
			return MSVersion.V3_3_1;
		}

	}

	@api
	public static class javascript_info extends AbstractFunction {

		public Class[] thrown() {
			return new Class[]{};
		}

		public boolean isRestricted() {
			return true;
		}

		public Boolean runAsync() {
			return null;
		}

		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			CArray ret = new CArray(t);
			ScriptEngine e = getEngine();
			ScriptEngineFactory f = e.getFactory();
			ret.set("name", new CString(f.getEngineName(), t), t);
			ret.set("version", new CString(f.getEngineVersion(), t), t);
			ret.set("language_name", new CString(f.getLanguageName(), t), t);
			ret.set("language_version", new CString(f.getLanguageVersion(), t), t);
			return ret;
		}

		public String getName() {
			return "javascript_info";
		}

		public Integer[] numArgs() {
			return new Integer[]{0};
		}

		public String docs() {
			return "array {} Returns various information about the system's installed javascript engine.";
		}

		public Version since() {
			return MSVersion.V3_3_1;
		}

	}
}
