var map; // global variable; needs to be accessible later

function initialize()
{
    var mapOptions =
    {
        center: new google.maps.LatLng(0, 0),
        zoom: 1
    };
    map = new google.maps.Map(document.getElementById("map-canvas"), mapOptions);
}

google.maps.event.addDomListener(window, 'load', initialize);