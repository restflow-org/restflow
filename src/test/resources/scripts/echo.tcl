#!/usr/bin/tclsh
global env


proc outputEnv {} {
    global env

    puts "env: "
    foreach var [array names env] {
        puts "  $var: |"
        puts "    $env($var)"
    }
}

proc outputArgs {} {
    global argv

    puts "args:"
    foreach arg $argv {
        puts "  - $arg"
    }

}

if {[catch {
    outputEnv
    outputArgs

    puts "echo: $env(value)"
} errMsg] } {
    puts "error: |"
    puts "  $errMsg"
}

