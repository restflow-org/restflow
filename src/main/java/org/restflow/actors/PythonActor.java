package org.restflow.actors;

public class PythonActor extends AugmentedScriptActor {

	@Override
	public ActorScriptBuilder getNewScriptBuilder() {
		return new PythonActor.ScriptBuilder();
	}
		
	@Override
	public synchronized String getScriptRunCommand() {
		return "python -";
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

		public ScriptBuilder appendLiteralAssignment(String name, Object value, String type) throws Exception {
			if (value == null) {
				_assignNullLiteral(name);
			} else if (type == null) {
				_assignStringLiteral(name, value);
			} else if (type.equals("String")) {
				_assignStringLiteral(name, value);
			} else if (type.equals("Boolean")) {
				_assignBooleanLiteral(name, value, type);
			} else if (type.equals("Integer")) {
				_assignNumberLiteral(name, value, type);
			} else {
				_assignOtherLiteral(name, value);
			}
			return this;
		}
		
		private ScriptBuilder _assignStringLiteral(String name, Object value) {
			_script.append(		name	)
				   .append( 	"="		)
				   .append( 	"'"		)
				   .append( 	value	)
				   .append( 	"'"		)
				   .append(		EOL		);
			return this;
		}

		private ScriptBuilder _assignBooleanLiteral(String name, Object value, String type) throws Exception {
			
			Boolean b = null;
			if (value instanceof Boolean) {
				b = ((Boolean)value == true);
			} else if (value instanceof Number) {
				b = (((Number)value).intValue() != 0);
			} else {
				throw new Exception("Error assigning value to python " + type + " variable '" + name + "': " + value);
			}
			_script.append(		name		)
				   .append( 	"="			)
				   .append( 	b ? 1 : 0	)
				   .append(		EOL			);
			return this;
		}

		private ScriptBuilder _assignNumberLiteral(String name, Object value, String type) throws Exception {
			if (! (value instanceof Number)) {
				throw new Exception("Error assigning value to python " + type + " variable '" + name + "': " + value);
			}
			_script.append(		name	)
				   .append( 	"="		)
				   .append( 	value	)
				   .append(		EOL		);
			return this;
		}

		private ScriptBuilder _assignOtherLiteral(String name, Object value) {
			_script.append(		name	)
				   .append( 	"="		)
				   .append( 	value	)
				   .append(		EOL		);
			return this;
		}

		private ScriptBuilder _assignNullLiteral(String name) {
			_script.append(		name	)
				   .append( 	"=None"		)
				   .append(		EOL		);
			return this;
		}
		
		public ScriptBuilder appendChangeDirectory(String path) {
			_script.append(		"os.chdir('"	)
				   .append( 	path			)
				   .append(		"')"			)
				   .append(		EOL				);
			return this;
		}

		public ScriptBuilder appendPrintStringStatement(String string) {
			_script.append(		"print '"	)
				   .append( 	string		)
				   .append(		"'"			)
				   .append(		EOL			);
			return this;
		}

		public ScriptBuilder appendVariableYamlPrintStatement(String name, String type) {
			if (type == null || type.equals("String")) {
				_appendStringVariableYamlPrinter(name);
			} else if (type.equals("Boolean")) {
				_appendBooleanVariableYamlPrinter(name);
			} else {
				_appendNonstringVariableYamlPrinter(name);
			}
			return this;
		}

		private ScriptBuilder _appendNonstringVariableYamlPrinter(String name) {
			_script.append(		"print '"		)
				   .append(		name			)
				   .append( 	": ', "			)
				   .append(		"'null' if "	)
				   .append(		name			)
				   .append(		"==None else "	)
				   .append(		name			)
				   .append(		EOL				);	
			return this;
		}

		private ScriptBuilder _appendStringVariableYamlPrinter(String name) {
			_script.append(		"print '"		)
				   .append(		name			)
				   .append( 	": ', "			)
				   .append(		"'null' if "	)
				   .append(		name			)
				   .append(		"==None else "	)
				   .append( 	"'\"%s\"' % "	)
				   .append(		name			)
				   .append(		EOL				);
			return this;
		}

		public ActorScriptBuilder appendNonNullStringYamlPrintStatement(String name) {
			_script.append(		"print '"		)
				   .append(		name			)
				   .append( 	": \"%s\"' % "	)
				   .append(		name			)
				   .append(		EOL				);
			return this;
		}

		
		private ScriptBuilder _appendBooleanVariableYamlPrinter(String name) {
			_script.append(		"print '"			)
				   .append(		name				)
				   .append( 	": ', \"true\" if "	)
				   .append(		name				)
				   .append( 	" else \"false\""	)
				   .append(		EOL					);	
			return this;
		}

		public ScriptBuilder appendInputControlFunctions() {

			appendComment("define functions for enabling and disabling actor inputs");
			appendCode( "def enableInput(input)      :   global enabledInputs;    enabledInputs   += ' ' + input"  );
			appendCode( "def disableInput(input)     :   global disabledInputs;   disabledInputs  += ' ' + input"  );
			appendBlankLine();

			appendComment("initialize input control variables");
			appendCode( "enabledInputs   = ''" );
			appendCode( "disabledInputs  = ''" );

			return this;
		}

		public ScriptBuilder appendOutputControlFunctions() {
			
			appendComment("define functions for enabling and disabling actor outputs");
			appendCode( "def enableOutput(output)    :   global enabledOutputs;   enabledOutputs  += ' ' + output" );
			appendCode( "def disableOutput(output)   :   global disabledOutputs;  disabledOutputs += ' ' + output" );
			appendBlankLine();

			appendComment("initialize output control variables");
			appendCode( "enabledOutputs  = ''" );
			appendCode( "disabledOutputs = ''" );

			return this;
		}

		
		public String toString() {
			return _script.toString();
		}
	}
}