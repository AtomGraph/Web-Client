#!/usr/bin/php
<?php

define('ROOTDIR', dirname(dirname(__FILE__)));
define('DS', DIRECTORY_SEPARATOR);
define('PS', PATH_SEPARATOR);

require_once ROOTDIR . DS . "lib" . DS . "graphity" . DS . "src" . DS . "main" . DS . "php" . DS . "Graphity" . DS . "Loader.php";

$loader = new \Graphity\Loader('HeltNormalt', ROOTDIR . DS . "src" . DS . "main" . DS . "php");
$loader->register();

include_once ROOTDIR . DS . "lib" . DS . "graphity" . DS . "bin" . DS . "route_mapper.php";

