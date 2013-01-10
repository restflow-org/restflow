
proc exec_background_pipeline {command {show_stdout 1}} {

	global exec_pipeline_busy
	global exec_pipeline_stdout
	global exec_pipeline_stderr
	
	set exec_pipeline_stdout ""
	set exec_pipeline_stderr ""
	fconfigure stdout -buffering none
	set fd [open "| $command" r]
	fconfigure $fd -blocking 0 -buffering line
	fileevent $fd readable "_handle_exec_pipeline_readable $fd $show_stdout"
	
	set exec_pipeline_busy 1
	
	vwait exec_pipeline_busy
}

proc _handle_exec_pipeline_readable {fd show_stdout} {

	global exec_pipeline_busy
	global exec_pipeline_stdout
	global exec_pipeline_stderr
	
	if ([eof $fd]) {
		
		# put stream from process into blocking mode so that closing it will
		# throw an exception if there was anything written to stderr
		fconfigure $fd -blocking 1
		
		catch {close $fd} exec_pipeline_stderr
		
		set exec_pipeline_busy 0
		
		return
	}

	# get the latest output from the process
	set output [read $fd]
	
	# save for later access
	append exec_pipeline_stdout $output

	# echo output to screen
	if {$show_stdout && $output != ""} {
		puts -nonewline $output
	}
}


proc print_stdout { message } {
	if { $message != "" } {
		puts ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>  STANDARD OUTPUT  >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>"
		puts [string trim $message]
		puts "<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<"
	}
}

proc print_stderr { message } {
	if { $message != "" } {
		puts ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>  STANDARD ERROR  >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>"
		puts [string trim $message]
		puts "<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<"
	}
}


