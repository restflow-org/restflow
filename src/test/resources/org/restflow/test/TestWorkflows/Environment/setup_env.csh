#!/bin/csh -f

# Set script dir to this script location
setenv WEBICE_SCRIPT_DIR `dirname $0`

# Depending on machine architecture load proper environment
switch (`uname -m`)

case alpha:
#Obsolete, we do not have alphas any more

	# Setup basic env
	source /etc/csh.cshrc

	# Unload any previous environment
	module purge

	module load ccp4_6/6.0.2-1
	module load ipmosflm/7.0.4
	module load labelit/1.000rc4

	# best module
	module load best/3.1

	set raddose_path=/usr/local/bin/raddose
	set path=($raddose_path $path)
 
	breaksw

case ia64:
	# Enable module command
	# source /home/sw/rhel4/Modules/default/init/tcsh

	# Unload any previous environment
	module purge

	# Load modules
        # Note that the loading of the "null" module is pure magic!
        # It is need for the moment to fix some weirdness with module.
        module load null
	module load ccp4/6.1.3
	module load ipmosflm/7.0.6
	# module load labelit/1.1.7
        module load raddose/20080103
	# Hopefulley the x86 executables run properly in emualtion mode.
	set best_path=/home/sw/rhel3/best/best_v3.1
	setenv besthome "$best_path"
	set path=($best_path $path)

	breaksw

case i686:
	# Enable module command
	source /home/sw/rhel4/Modules/default/init/tcsh

	# Unload any previous environment
	module purge

	# Load modules
	module load ccp4/6.1.3-bin
	module load ipmosflm/7.0.6
	module load labelit/1.1.7
        module load raddose/20080103

	set best_path=/home/sw/rhel3/best/best_v3.1
	setenv besthome "$best_path"
	set path= ($best_path $path)

	breaksw

case x86_64:
	# Enable module command
	set test_module = `alias module`
	if ( "$test_module" == "" && -f /etc/profile.d/modules.csh ) then
	  source /etc/profile.d/modules.csh
	endif
	unset test_module

	# Unload any previous environment
	module purge

	# Load modules
        # Note that the loading of the "null" module is pure magic!
        # It is needed for the moment to fix some weirdness with module.
        module load null
	module load ccp4/6.1.13
	module load ipmosflm/7.0.6
	#module load labelit/1.1.7
        # The latest version of labelit in under the phenix distribution
	module load phenix/1.6.4-486
        module load raddose/20080103
	module load best/3.1

	breaksw

default:

	echo "ERROR: Unknown architechture, can not setup environment."

endsw
env
