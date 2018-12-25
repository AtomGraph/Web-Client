#!/bin/bash

# set Context variables (which are used in $CATALINA_HOME/conf/Catalina/localhost/ROOT.xml)

if [ ! -z "$STYLESHEET" ] ; then
    STYLESHEET_PARAM="--stringparam ac:stylesheet $STYLESHEET "
fi
if [ ! -z "$RESOLVING_UNCACHED" ] ; then
    RESOLVING_UNCACHED_PARAM="--stringparam ac:resolvingUncached $RESOLVING_UNCACHED "
fi
if [ ! -z "$SITEMAP_RULES" ] ; then
    SITEMAP_RULES_PARAM="--stringparam ac:sitemapRules $SITEMAP_RULES "
fi

# $CATALINA_HOME must be the WORKDIR at this point

transform="xsltproc \
  --output conf/Catalina/localhost/ROOT.xml \
  $STYLESHEET_PARAM \
  $RESOLVING_UNCACHED_PARAM \
  $SITEMAP_RULES_PARAM \
  conf/Catalina/localhost/context.xsl \
  conf/Catalina/localhost/ROOT.xml"

eval $transform

# run Tomcat

if [ -z "$JPDA_ADDRESS" ] ; then
    catalina.sh run
else
    catalina.sh jpda run
fi