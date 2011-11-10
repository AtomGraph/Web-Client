<?php

namespace Graphity\SemanticReports;

use Graphity\Rdf as Rdf;
use Graphity\Response;
use Graphity\Resource;
use Graphity\Sparql as Sparql;
use Graphity\View\XSLTView;

use HeltNormalt\Repository\Dydra\DydraClient;

class View extends XSLTView
{
    const SPARQL_BASE = '/webapp/WEB-INF/sparql/';
    const TEXT_HTML = "text/html";
    const APPLICATION_XML = "application/xml";
    const APPLICATION_XHTML = "application/xhtml+xml";

    public function __construct(Resource $resource)
    {
        parent::__construct($resource);

        // http://hixie.ch/advocacy/xhtml though, we have to do this :(
        $this->setContentType(self::TEXT_HTML);
        $this->setHeader("Vary", "Accept");

        if (strstr(get_class($this->getResource()), "List")) $this->getTransformer()->setParameter("", "view", Model\HeltNormalt::NS . "ListView");
        else
            $this->getTransformer()->setParameter("", "view", Model\Graphity::NS . "ReadView");

        $this->getTransformer()->setParameter("", "uri", $this->getResource()->getURI());
        $this->getTransformer()->setParameter("", "base-uri", $this->getResource()->getBaseURI());
        $this->getTransformer()->setParameter("", "php-os", php_uname("s"));
        $this->getTransformer()->setParameter("", "total-item-count", $this->getCount());
        $this->getTransformer()->setParameter("", "offset", $this->getResource()->getForm()->getOffset());
        $this->getTransformer()->setParameter("", "limit", $this->getResource()->getForm()->getLimit());
        $this->getTransformer()->setParameter("", "order-by", $this->getResource()->getForm()->getOrderBy());
        $this->getTransformer()->setParameter("", "desc-default", true);
        $this->getTransformer()->setParameter("", "desc", $this->getResource()->getForm()->getDesc());
        $this->getTransformer()->setParameter("", "duration", $this->getResource()->getForm()->getDuration());

        $this->getDocument()->loadXML($this->getRDFXML());
    }

    public final function getQueryString($relativePath)
    {
        return $this->getResource()->getQueryString($relativePath);
    }

    protected function getStyleSheetPath()
    {
        return ROOTDIR . "/src/main/webapp/WEB-INF/xsl/SemanticReports.xsl";
    }

    // TO-DO: move this to HeltNormaltResource::describe()
    protected final function getRDFXML()
    {
        if((($cachedRdfXml = $this->getDataCache()->load(strtoupper($this->getRequest()->getMethod()) . "|" . $this->getRequest()->getRequestURI())) === false) || ($this->getDataCache()->load("maintenance.force_miss") == true)) {
            $cachedRdfXml = $this->getResource()->describe();
            $this->getDataCache()->save($cachedRdfXml, strtoupper($this->getRequest()->getMethod()) . "|" . $this->getRequest()->getRequestURI(), 3600);
        }
        return $cachedRdfXml;
    }

    public final function display()
    {
        $xpath = new \DOMXPath($this->getDocument());
        $xpath->registerNamespace('dct', Model\DCTerms::NS); 

        $lastModified = new \DateTime();
        // this whole workaround is needed to imitate what in XPath 2.0 should work simply as max(//dct:issued)
        // improve if possible!
        if(($dates = $xpath->evaluate("//dct:issued")) !== false) {
            $latest = null;
            foreach ($dates as $date)
            {
                $current = new \DateTime($date->textContent);
                if ($latest == null) $latest = $current;
                if ($current > $latest) $latest = $current;
            }
            if ($latest !== null) $lastModified = $latest;
        }

        $lastModified->setTimezone(new \DateTimeZone('GMT'));

        $this->setHeader("Cache-Control", "max-age=600, s-maxage=1800, must-revalidate");
        $this->setHeader("Last-Modified", $lastModified->format("D, d M Y H:i:s") . " GMT");
        // Expires commented out in favor of Cache-Control
        //$this->setHeader("Expires", id(new \DateTime("now", new \DateTimeZone("GMT")))->add(new \DateInterval("PT1H"))->format("D, d M Y H:i:s") . " GMT");

        if($this->getRequest()->getHeader("HTTP_IF_MODIFIED_SINCE") != null) {
            $modifiedSince = new \DateTime($this->getRequest()->getHeader("HTTP_IF_MODIFIED_SINCE"), new \DateTimeZone('GMT'));
            if($modifiedSince->format("YmdHis") >= $lastModified->format("YmdHis")) {
                $this->setHeader("Content-Length", 0);
                $this->setStatus(Response::SC_NOT_MODIFIED);
                return;
            }
        }

        $this->write($this->transform());
    }
    
    /**
     * Returns router
     * 
     * @return Router
     */
    public function getRouter()
    {
        return $this->getResource()->getRouter();
    }

    /**
     * Returns data cache
     *
     * @return DataCache
     */
    public function getDataCache()
    {
        return $this->getResource()->getDataCache();
    }

    public function getCount()
    {
        return $this->getResource()->getRepository()->count(Sparql\Query::newInstance()
            ->setQuery($this->getQueryString("count.rq"))
            ->setVariable('type', new Rdf\Resource($this->getResource()->getOntClass())));
    }

}
