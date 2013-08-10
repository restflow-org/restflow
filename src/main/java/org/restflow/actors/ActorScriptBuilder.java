package org.restflow.actors;

import java.io.IOException;

public interface ActorScriptBuilder {
	ActorScriptBuilder appendBlankLine();
	ActorScriptBuilder appendChangeDirectory(String path);
	ActorScriptBuilder append(String text);
	ActorScriptBuilder appendCode(String code);
	ActorScriptBuilder appendComment(String text);
	ActorScriptBuilder appendInputControlFunctions();
	ActorScriptBuilder appendOutputControlFunctions();
	ActorScriptBuilder appendLiteralAssignment(String name, Object value, String type, boolean mutable, boolean nullable) throws Exception;
	ActorScriptBuilder appendPrintStringStatement(String string);
	ActorScriptBuilder appendSeparator();
	ActorScriptBuilder appendSerializationBeginStatement();
	ActorScriptBuilder appendSerializationEndStatement();
	ActorScriptBuilder appendVariableSerializationStatement(String name, String type);
	ActorScriptBuilder appendOutputVariableSerializationStatement(String name, String type);
	ActorScriptBuilder appendNonNullStringVariableSerializationPrintStatement(String name);
	void appendScriptHeader(ActorScriptBuilder script, String scriptType) throws IOException;
	ActorScriptBuilder appendScriptExitCommend();
}
