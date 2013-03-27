package org.restflow.actors;

public class AugmentedTclActor extends AugmentedScriptActor {

	@Override
	public ActorScriptBuilder getNewScriptBuilder() {
		return new AugmentedTclActor.ScriptBuilder();
	}
		
	@Override
	public synchronized String getScriptRunCommand() {
		return "C:\\Tcl\\bin\\tclsh85.exe";
	}

	public static class ScriptBuilder implements ActorScriptBuilder {

		private StringBuilder _script = new StringBuilder();
		private final static String EOL = System.getProperty("line.separator");

		public ActorScriptBuilder appendCode(String code) {
			_script.append(		code	)
				   .append(		EOL		);
			return this;
		}

		public ScriptBuilder appendSeparator() {
			_script.append(		"######################################################################################"	)
				   .append(		EOL																							);
			return this;
		}

		public ScriptBuilder appendBlankLine() {
			_script.append(	EOL	);
			return this;
		}

		
		public ScriptBuilder appendComment(String text) {
			_script.append(		"# "	)
				   .append(		text	)
			   	   .append(		EOL		);
			return this;
		}

		public ActorScriptBuilder appendLiteralAssignment(String name,
				Object value, String type, boolean mutable, boolean nullable) throws Exception {
			_script.append(		"set "	)
				   .append(		name	)
				   .append( 	" "		)
				   .append( 	"\""	)
				   .append( 	value	)
				   .append( 	"\""	)
				   .append(		EOL		);
			return this;
		}
		
		public ScriptBuilder appendChangeDirectory(String path) {
			_script.append(		"cd "	)
				   .append( 	path	)
				   .append(		EOL		);
			return this;
		}

		public ScriptBuilder appendPrintStringStatement(String string) {
			_script.append(		"puts \""	)
				   .append( 	string		)
				   .append(		"\""			)
				   .append(		EOL			);
			return this;
		}

		public ScriptBuilder appendVariableYamlPrintStatement(String name, String type) {
			_script.append(		"puts \""	)
				   .append(		name		)
				   .append( 	": "		)
				   .append( 	"\\\"[set "		)
				   .append(		name		)
				   .append( 	"]\\\"\""		)
				   .append(		EOL			);
			return this;
		}

		@Override
		public ActorScriptBuilder appendNonNullStringYamlPrintStatement(
				String name) {
			return appendVariableYamlPrintStatement(name, null);
		}

		public ScriptBuilder appendInputControlFunctions() {

			appendComment("define functions for enabling and disabling actor inputs");
			appendCode(   "proc enableInput  {input} {global enabledInputs; append enabledInputs  \" $input\" }" );
			appendCode(   "proc disableInput {input} {global disabledInputs; append disabledInputs \" $input\" }"  );
			appendBlankLine();

			appendComment("initialize input control variables");
			appendCode( "set enabledInputs  {}" );
			appendCode( "set disabledInputs {}" );

			return this;
		}

		public ScriptBuilder appendOutputControlFunctions() {
			
			appendComment("define functions for enabling and disabling actor outputs");
			appendCode(   "proc enableOutput  {output} {global enabledOutputs; append enabledOutputs  \" $output\" }" );
			appendCode(   "proc disableOutput {output} {global disabledOutputs; append disabledOutputs \" $output\" }"  );
			appendBlankLine();

			appendComment("initialize output control variables");
			appendCode( "set enabledOutputs  {}" );
			appendCode( "set disabledOutputs {}" );
			
			return this;
		}
		
		public String toString() {
			return _script.toString();
		}
	}
}
