#!/bin/bash

# start without client - server
time ./HelloWorld.yaml

# start the RestFlow server
`./start-daemon.bash`;

# start workflow with client server using sha-bang
time ./HelloWorld-2.yaml

# use RestFlow script to launch workflow
time ./RestFlow -c -f `pwd`/HelloWorld-2.yaml

time ./HelloWorld-2.yaml

# shutdown server
./RestFlow -c --server-stop


