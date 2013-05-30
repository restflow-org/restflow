#!/bin/sh
# the next line restarts using tclsh \
exec tclsh "$0" "$@"

global env

puts $env(PHENIX_RELEASE_TAG)
