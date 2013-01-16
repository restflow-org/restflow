
set TARGET_DIR 					target
set RESTFLOW_STANDALONE_JAR 	${TARGET_DIR}/RestFlow-0.4-jar-with-dependencies.jar
set RESTFLOW_JAR				${TARGET_DIR}/RestFlow-0.4.jar
set RESTFLOW_TESTS_DIR			src/test/java
set RESTFLOW_TESTS_DIR_DEPTH	[llength [split $RESTFLOW_TESTS_DIR "/"]]
set JUNIT_MAIN_CLASS			junit.textui.TestRunner 
set TCL_FILE_PATH_SEPARATOR 	"/"

set MAIN_CLASSES ${TARGET_DIR}/classes
set TEST_CLASSES ${TARGET_DIR}/test-classes
set JAR_DEPENDENCY_DIR ${TARGET_DIR}/dependency

source exec_pipeline.tcl

set show_test_stdout 0

proc help {} {
	puts ""
	puts "Command                          Description"
	puts "----------------------------     ---------------------------------------------------"
	puts "ant <target>                     Runs the specified ant target defined in build.xml."
	puts ""
	puts "output enable                    Enable display of output from successful tests."
	puts ""
	puts "output disable                   Disable display of output from successful tests."
	puts ""
	puts "restflow <args>                  Run restflow with the given arguments."
	puts ""
	puts "run <path to workflow>           Run the specified workflow using restflow."
	puts "                                 Equivalent to 'restflow -f'."
	puts ""
	puts "test class <classname>           Run the JUnit tests in the specified class."
	puts "                                 Classname must be fully qualified."
	puts ""
	puts "test package <package name>      Run the JUnit tests in all classes in the" 
	puts "                                 specified package.  Does not test subpackages."
	puts ""
	puts "test dir <directory path>        Run the JUnit tests in all classes in the" 
	puts "                                 specified directory and its subdirectories."
	puts ""
	puts "use ant                          Specifies that the classes built by ant and the"
	puts "                                 dependencies resolved through ant should be used"
	puts "                                 when running or testing RestFlow."
	puts ""
	puts "use jar                          Specifies that the latest RestFlow jar, the test clases"
	puts "                                 compiled by ant, and the dependencies resolved through"
	puts "                                 ant should be used when running or testing RestFlow."
	puts ""
	puts "use standalonejar                Specifies that the latest self-contained RestFlow jar"
	puts "                                 and test classes compiled by ant should be used when"
	puts "                                 running or testing RestFlow."
	puts ""
	
}

proc use {arg} {

	global RESTFLOW_STANDALONE_JAR
	global RESTFLOW_JAR
    global MAIN_CLASSES
    global TEST_CLASSES
    global JAR_DEPENDENCY_DIR
	global restflow_command
	global restflow_classpath
	
	switch $arg {
	
		ant {
			set restflow_classpath 	[make_search_path [list $MAIN_CLASSES $TEST_CLASSES ${JAR_DEPENDENCY_DIR}/*]]
			set restflow_command 	[concat java -cp $restflow_classpath org.restflow.RestFlow]
			puts "Using ant-built classes for running and testing RestFlow"
		}
		
		jar {
			set restflow_classpath 	[make_search_path [list $TEST_CLASSES $RESTFLOW_JAR] ]
			set restflow_command 	[concat java -cp $restflow_classpath org.restflow.RestFlow]
			puts "Using RestFlow jar for running and testing RestFlow"
		}
		
		standalonejar {
			set restflow_classpath 	[make_search_path [list $TEST_CLASSES $RESTFLOW_STANDALONE_JAR]]
			set restflow_command 	[concat java -cp $restflow_classpath org.restflow.RestFlow]
			puts "Using self-contained RestFlow jar for running and testing RestFlow"
		}
		
		default { 
			puts "Invalid argument: $arg" 
		}
	}
}

proc output {arg} {

	global show_test_stdout
	
	switch $arg {
		enable  	{ set show_test_stdout 1 } 
		disable 	{ set show_test_stdout 0 }
		default		{ puts "Invalid argument: $arg" }
	}
}

proc reset_test_totals {} {
	global total_runs
	global total_failures
	global total_errors
	global total_summary
	set total_runs		0
	set total_failures	0
	set total_errors	0
	set total_summary	""
}

proc print_test_totals {} {

	global total_runs
	global total_failures
	global total_errors
	global total_summary

	set grand_total_line \
		[format "                                         Tests run:%4d,   Failures:%4d,   Errors:%4d" $total_runs $total_failures $total_errors]
	
	puts        "==========================================================================================="
	puts $grand_total_line 
}


if {[string compare $tcl_platform(platform) windows] == 0} {
	set OS_FILE_PATH_SEPARATOR "\\"
	set OS_SEARCH_PATH_SEPARATOR ";"
} else {
	set OS_FILE_PATH_SEPARATOR "/"
	set OS_SEARCH_PATH_SEPARATOR ":"
}

proc restflow {args} {
	global restflow_command
	set command [concat $restflow_command $args]
    puts $command
	exec_background_pipeline $command
}

proc run {workflow_path args} {
	eval restflow -base RESTFLOW_TESTRUNS_DIR -f $workflow_path $args
}

set spaces "                                                "
proc pad {s length} {
	global spaces
	set padding [string range $spaces 0 [expr $length - [string length $s]]]
	append s $padding
	return $s
}

proc _test_class {fully_qualified_test_class show_junit_stdout} {
	
	global JUNIT_MAIN_CLASS
	global exec_pipeline_stdout
	global exec_pipeline_stderr
	global restflow_classpath
	global total_runs
	global total_failures
	global total_errors
	global total_summary
	
	set unqualified_test_class [lindex [split $fully_qualified_test_class "."] end]
	
	if {$show_junit_stdout} {
		puts "------------------- $fully_qualified_test_class -------------------"
	} else {
		puts -nonewline [pad $unqualified_test_class 40]
	}
	
	# build the command for running junit on the test class
	#set classpath [make_search_path [list classes src/main/resources lib/runtime/*]]
	set command [list java -cp $restflow_classpath $JUNIT_MAIN_CLASS $fully_qualified_test_class]

	# run the command
	exec_background_pipeline $command $show_junit_stdout

	set runs 	 0
	set failures 0
	set errors 	 0
	set summary_line "NO JUNIT SUMMARY LINE FOUND"

	# check junit output for success line
	set success_line_pattern {OK \(([0-9]+) test[s]?\)}
	set success_line_found [regexp $success_line_pattern $exec_pipeline_stdout success_line runs]
	
	if { $success_line_found == 0} {
		set failure_line_pattern {Tests run: ([0-9]+),  Failures: ([0-9]+),  Errors: ([0-9]+)}	
		set failure_line_found [regexp $failure_line_pattern $exec_pipeline_stdout discard runs failures errors]
	}			
	
	set total_runs 		[expr $total_runs 	  + $runs]
	set total_failures 	[expr $total_failures + $failures]
	set total_errors 	[expr $total_errors   + $errors]
	
	set summary_line [format "Tests run:%4d,   Failures:%4d,   Errors:%4d" $runs $failures $errors]
	
	# print junit output for failed tests or a test summary if test stdout is not being displayed
	if {$show_junit_stdout} {
		print_stderr $exec_pipeline_stderr
		if {$failures == 0 && $errors == 0 } {	
			puts $summary_line
		}
	}
	
	if {$show_junit_stdout == "0"} {
		puts $summary_line
		if { $failures > 0 || $errors > 0 } {
			print_stdout $exec_pipeline_stdout
			print_stderr $exec_pipeline_stderr
		}
	}
	
	# insert a blank line between tests if displaying stdout from tests
	if {$show_junit_stdout} {
		puts ""
	}
}

proc _test_dir {test_directory {recursive 1}} {

	global RESTFLOW_TESTS_DIR_DEPTH
	global TCL_FILE_PATH_SEPARATOR
	global show_test_stdout
	
	# get list of class source files in the directory
	set test_files_list [glob -nocomplain -directory ${test_directory} Test*.java]
	
	# operate on each test source file
	foreach test_file_path $test_files_list {
		
		# parse the file path components
		set test_path_elements 		[split $test_file_path $TCL_FILE_PATH_SEPARATOR]
		set package_path_elements 	[lrange $test_path_elements $RESTFLOW_TESTS_DIR_DEPTH [expr [llength $test_path_elements] -2]]
		set test_file_name 			[lrange $test_path_elements end end]
		set class_name 				[lindex [split $test_file_name "."] 0]
		
		# build the fully qualified class name corresponding to the test source file
		set qualified_class_name 	[make_package_path [concat $package_path_elements $class_name]]
	
		# test the class
		_test_class $qualified_class_name $show_test_stdout
	}
	
	if {$recursive} {
		set dir_list [glob -nocomplain -directory ${test_directory} -type d *]
		foreach dir $dir_list {
			_test_dir $dir
		}
	}
}

proc _test_workflow {workflow_dir} {

	global TCL_FILE_PATH_SEPARATOR
	set workflow_dir_elements 	[split $workflow_dir $TCL_FILE_PATH_SEPARATOR]
	set workflow_dir_name 		[lrange $workflow_dir_elements end end]
	set workflow_file_name 		${workflow_dir_name}.yaml
	set workflow_path			[make_file_path [concat $workflow_dir_elements $workflow_file_name]]
	puts $workflow_path
	run $workflow_path
}


proc test { type target } {

	global RESTFLOW_TESTS_DIR
	global show_test_stdout

	reset_test_totals		

	switch $type {
	
		class {
			eval _test_class $target $show_test_stdout
		}

		package {
			set package_parts [split $target "."]
			set package_path [make_file_path [concat src test java $package_parts]]
			eval _test_dir $package_path 0
			print_test_totals
		}

		dir {
			# prepend relative path to tests directory if directory does not start with it
			if {[regexp {^src/test/java} $target] == "0"} {
				set target [make_file_path [list $RESTFLOW_TESTS_DIR $target]]
			}

			eval _test_dir $target 1
			print_test_totals
		}
		
		workflow {
		
			if {[regexp {^src/test/resources} $target] == "0"} {
				set target [make_file_path [list src/test/resources $target]]
			}

			eval _test_workflow $target
		}
	}
}

proc make_path { path_parts path_separator} {
	set first 1
	set path ""
	foreach part $path_parts {
		if {$first == 0} { 
			append path $path_separator
		} else {
			set first 0
		}
		append path $part
	}
	return $path
}

proc make_package_path { path_parts } {
	return [make_path $path_parts "."]
}

proc make_file_path { path_parts } {
	global TCL_FILE_PATH_SEPARATOR
	return [make_path $path_parts $TCL_FILE_PATH_SEPARATOR]
}

proc make_search_path { path_parts } {
	global OS_SEARCH_PATH_SEPARATOR
	return [make_path $path_parts $OS_SEARCH_PATH_SEPARATOR]
}


proc handle_stdin_readable {} {

	if ([eof stdin]) {
		exit
	}

	# get the latest input from the process
	set input_command [read stdin]
	
	catch {uplevel #0 $input_command} error_message
	
	if {$error_message != ""} {
		puts $error_message
	}
	
	print_prompt
}

proc print_prompt {} {
	flush stdout
	puts -nonewline "testbench> "
	flush stdout
}

proc ant {args} {
	set command "ant $args"
	exec_background_pipeline $command 1
}

proc reload {} {
    uplevel source testbench.tcl
}

#test class org.restflow.nodes.TestMergeNodeWorkflows
#test class org.restflow.actors.TestSubworkflowBuilder
#test package org.restflow.actors
#test dir src/test/java/org/restflow

#run src/test/resources/org/restflow/RestFlow/hello1.yaml
#run src/test/resources/org/restflow/RestFlow/hello1.yaml -w HelloWorld
#run src/test/resources/org/restflow/RestFlow/hello1.yaml

use ant

fconfigure stdin -buffering line -blocking 0
fconfigure stdout -buffering line -blocking 0
fileevent stdin readable handle_stdin_readable
print_prompt
vwait forever

