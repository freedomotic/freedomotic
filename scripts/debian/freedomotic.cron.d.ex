#
# Regular cron jobs for the freedomotic package
#
0 4	* * *	root	[ -x /usr/bin/freedomotic_maintenance ] && /usr/bin/freedomotic_maintenance
