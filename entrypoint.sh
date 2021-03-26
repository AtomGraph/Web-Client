#!/bin/bash

### Signal handlers ###

function handle_signal {
    case $1 in
      TERM|INT|EXIT)
        if [ -n "$CMD_PID" ]; then
          kill "$CMD_PID" &>/dev/null
          sleep 1
        fi
        
        echo "Exiting ..." >&2
        exit 0
        ;;
      *)
        echo "Terminating abnormally" >&2
        exit 1
        ;;
    esac
}

function ignore_signal {
  log "Caught signal $1 - ignored" >&2
}

trap "handle_signal TERM" "TERM"
trap "handle_signal INT" "INT"
trap "ignore_signal HUP" "HUP"

### Sleeper function ###

# $1 process PID
function wait_to_finish {
    while true; do
        sleep 1 &
        PID=$!

        if ! wait $PID ; then
            kill $PID &>/dev/null
        fi

        if ! ps -p "$1" > /dev/null ; then # process not running anymore
           break; # exit while loop
        fi
    done
}

### Arguments ###

# context variables are used in $CATALINA_HOME/conf/Catalina/localhost/ROOT.xml

if [ -n "$STYLESHEET" ] ; then
    STYLESHEET_PARAM="--stringparam ac:stylesheet $STYLESHEET "
fi
if [ -n "$RESOLVING_UNCACHED" ] ; then
    RESOLVING_UNCACHED_PARAM="--stringparam ac:resolvingUncached $RESOLVING_UNCACHED "
fi
if [ -n "$SITEMAP_RULES" ] ; then
    SITEMAP_RULES_PARAM="--stringparam ac:sitemapRules $SITEMAP_RULES "
fi

### Execution ###

# $CATALINA_HOME must be the WORKDIR at this point

transform="xsltproc \
  --output conf/Catalina/localhost/ROOT.xml \
  $STYLESHEET_PARAM \
  $RESOLVING_UNCACHED_PARAM \
  $SITEMAP_RULES_PARAM \
  conf/Catalina/localhost/context.xsl \
  conf/Catalina/localhost/ROOT.xml"

eval "$transform"

# run Tomcat process in the background

if [ -z "$JPDA_ADDRESS" ] ; then
    catalina.sh run &
else
    catalina.sh jpda run &
fi

CMD_PID=$!
wait_to_finish $CMD_PID