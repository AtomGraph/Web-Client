var resourcesXML = null;
var propertiesXML = null;

function loadXML(uri)
{
    var jqXHR = $.ajax({url: uri, async: false,
        headers: { 'Accept': 'application/rdf+xml' }
      });
    return jqXHR.responseXML;		    
}

function loadResourcesXML(event, query)
{
    var searchUri = UriBuilder.fromUri(baseUri).
        segment('resources').
        segment('labelled').
        queryParam('query', query).
        build();

    $.ajax({url: searchUri, headers: { 'Accept': 'application/rdf+xml' } }).
    done(function(data, textStatus, jqXHR)
    {
        resourcesXML = jqXHR.responseXML;
        onresourceTypeaheadCallback(event);
    } ).
    fail(function(jqXHR, textStatus, errorThrown)
    {
        alert(errorThrown);
    });
}

function loadPropertiesXML(event, query)
{
    var searchUri = UriBuilder.fromUri(baseUri).
        segment('properties').
        segment('labelled').
        queryParam('query', query).
        build();

    $.ajax({url: searchUri, headers: { 'Accept': 'application/rdf+xml' } , cache: false }).
    done(function(data, textStatus, jqXHR)
    {
        propertiesXML = jqXHR.responseXML;
        onpropertyTypeaheadCallback(event);
    } ).
    fail(function(jqXHR, textStatus, errorThrown)
    {
        alert(errorThrown);
    });
}