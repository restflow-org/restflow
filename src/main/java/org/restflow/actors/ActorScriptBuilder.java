package org.restflow.actors;

public interface ActorScriptBuilder {
	ActorScriptBuilder appendBlankLine();
	ActorScriptBuilder appendChangeDirectory(String path);
	ActorScriptBuilder appendCode(String code);
	ActorScriptBuilder appendComment(String text);
	ActorScriptBuilder appendInputControlFunctions();
	ActorScriptBuilder appendOutputControlFunctions();
	ActorScriptBuilder appendLiteralAssignment(String name, Object value, String type, boolean mutable, boolean nullable) throws Exception;
	ActorScriptBuilder appendPrintStringStatement(String string);
	ActorScriptBuilder appendSeparator();
	ActorScriptBuilder appendVariableYamlPrintStatement(String name, String type);
	ActorScriptBuilder appendNonNullStringYamlPrintStatement(String name);
}
