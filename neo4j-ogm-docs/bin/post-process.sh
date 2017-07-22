#!/bin/bash
file="$1"
sedfile="$2"
sed -i.bak -f $sedfile $file
