<?php

define('ROOTDIR', dirname(dirname(dirname(dirname(__FILE__)))));

setlocale(LC_TIME, 'da_DK.UTF-8');
date_default_timezone_set("Europe/Copenhagen");
//error_reporting(E_ALL | E_STRICT);

if(extension_loaded('xhprof')) {
    include_once 'xhprof_lib/utils/xhprof_lib.php';
    include_once 'xhprof_lib/utils/xhprof_runs.php';
    xhprof_enable(XHPROF_FLAGS_CPU + XHPROF_FLAGS_MEMORY);
}

require_once ROOTDIR . '/src/main/php/Graphity/Analytics/config/init.php';
$request = new Graphity\Request();

try {
    $router = new Graphity\Router(include(SRCDIR . '/HeltNormalt/config/routes.php'));
    $router->matchResource($request)->process();
} catch(Exception $e) {
    // catch all uncaught exceptions
    $view = new HeltNormalt\View\ExceptionView($e, $request);
    $view->display();
    echo $view->getBuffer();
}

if(extension_loaded('xhprof')) {
    $namespace = 'analytics';
    $data = xhprof_disable();
    $xhprof = new XHProfRuns_Default();
    $runId = $xhprof->save_run($data, $namespace);
}
