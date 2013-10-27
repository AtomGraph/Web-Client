/**
 *  UriBuilder class for easier composition of URIs.
 *  
 *  Example usage:
 *  
 *  UriBuilder.newInstance().host("www.seporaitis.net").path("index.php/2010/01/20/sis-zurnalas-uzsidaro/").fragment("comments").build();
 *  
 *  Will return:
 *  
 *  "http://www.seporaitis.net/index.php/2010/01/20/sis-zurnalas-uzsidaro/#comments"
 *  
 *  Based on this interface: http://jsr311.java.net/nonav/javadoc/javax/ws/rs/core/UriBuilder.html
 *  
 *  Note that this implementation ignores regular expressions inside { }.
 *  
 *  @author Julius Seporaitis <julius@seporaitis.net>
 */

window.UriBuilder = UriBuilder = function() {
    this.schemeName = null;
    this.hostName = null;
    this.portNum = null;
    this.listOfSegments = [];
    this.listOfQueryParams = [];
    this.fragmentValue = null;
};

/**
 *  Static regexps, used internally.
 */
UriBuilder._REGEX_SCHEMA = /(\w+):\/\//i;
UriBuilder._REGEX_PORT = /:(\d+)[\/]?/i;
UriBuilder._REGEX_HOST = /\w+:\/\/([\w\.\-\d]+)/i;
UriBuilder._REGEX_VARIABLE = /\{[a-z]+[:\s]*([\\a-z]+(\{[0-9,]+\}){0,1}[-]?)*\}/i;

/**
 * Set uri scheme
 * 
 * @param string value
 * @return {UriBuilder}
 */
UriBuilder.prototype.scheme = function(value) {
    this.schemeName = value;
    return this;
};

/**
 * Set hostname
 * 
 * @param string value
 * @returns {UriBuilder}
 */
UriBuilder.prototype.host = function(value) {
    if(UriBuilder.hasSchema(value)) {
        var scheme = UriBuilder._REGEX_SCHEMA.exec(value)[1];
        this.scheme(scheme);
        value = value.substring(scheme.length + 3);
    }
    this.hostName = value;
    return this;
};

/**
 * Set port
 * 
 * @param string value
 * @returns {UriBuilder}
 */
UriBuilder.prototype.port = function(value) {
    this.portNum = value;
    return this;
};

/**
 * Append path 
 * 
 * @param string value
 * @returns {UriBuilder}
 */
UriBuilder.prototype.path = function(value) {
    var regex = /\//ig;
    
    var list = value.split(regex);
    for(key in list) {
        this.segment(list[key]);
    }
    
    return this;
};

/**
 * Append a single path segment
 * 
 * @param string value
 * @returns {UriBuilder}
 */
UriBuilder.prototype.segment = function(value) {
    if(value.length == 0) {
        return this;
    }
    this.listOfSegments.push(value);
    return this;
};

/**
 * Add a query name=value pair
 * 
 * @param string name
 * @param string value
 * @returns {UriBuilder}
 */
UriBuilder.prototype.queryParam = function(name, value) {
    this.listOfQueryParams.push({'name': name, 'value': value});
    return this;
};

/**
 * Add browser fragment
 * 
 * @param string value
 * @returns {UriBuilder}
 */
UriBuilder.prototype.fragment = function(value) {
    this.fragmentValue = value;
    return this;
};

/**
 * Build URI from created template
 * 
 * Example usage:
 * 
 * UriBuilder.newInstance().host("coolwebsite.com").path("/{lang}/news/{id}").build("en", 1234);
 * 
 * Will return
 * 
 * "http://coolwebsite.com/en/news/1234"
 * 
 * @returns {String}
 */
UriBuilder.prototype.build = function() {
    var map = (function(args) {
        this.args = Array.prototype.slice.call(args);
        
        this.hasNext = function() {
            return (this.args.length > 0);
        };
        
        this.next = function() {
            return this.args.shift();
        };
        
        this.get = function(key) {
            if(hasNext()) {
                return this.next();
            }
            
            return null;
        };
        
        return this;
    }(arguments));
    
    template = function(value) {
        var key = "none";
        
        var newValue = value;
        var matches = null;
        while((matches = newValue.match(UriBuilder._REGEX_VARIABLE)) != null) {
          newValue = newValue.replace(matches[0], map.get(key));
        }

        if(newValue == null) {
            return value;
        }
        return newValue;
    };
    
    var URI = "";
    if(this.schemeName != null) {
        URI += this.schemeName + "://";
    }
    if(this.hostName != null) {
        if(URI.length == 0) {
            URI += "http://";
        }
        URI += template(this.hostName);
    }
    
    if(this.portNum != null) {
        URI += ":" + this.portNum.toString();
    }
    
    if(URI.length > 0) {
        if(this.listOfSegments.length > 0) {
            if(this.listOfSegments[0] != "") {
                URI += "/";
            } else {
                this.listOfSegments.shift();
                URI += "/";
            }
        } else {
            URI += "/";
        }
    }
    
    for(var key in this.listOfSegments) {
        URI += encodeURIComponent(template(this.listOfSegments[key])) + ((key < this.listOfSegments.length - 1) ? "/" : "");
    }
    
    if(this.listOfQueryParams.length > 0) {
        URI += "?";
        for(var key in this.listOfQueryParams) {
            URI += encodeURIComponent(template(this.listOfQueryParams[key].name)) + "=" + encodeURIComponent(template(this.listOfQueryParams[key].value)) + ((key < this.listOfQueryParams.length - 1) ? "&" : ""); 
        }
    }
    
    if(this.fragmentValue != null) {
        URI += "#" + encodeURIComponent(template(this.fragmentValue));
    }
    
    return URI;
};

/**
 * Build URI from created template using parameter map.
 * 
 * Example usage:
 * 
 * UriBuilder.newInstance().host("coolwebsite.com").path("/{lang}/news/{id}").buildFromMap({'lang': "en", 'id': 1234});
 * 
 * Will return
 * 
 * "http://coolwebsite.com/en/news/1234"
 * 
 * @param object map
 * @returns {String}
 */
UriBuilder.prototype.buildFromMap = function(map) {
    
    template = function(value) {
        var startPos = value.indexOf("{");
        if(startPos == -1) {
            return value;
        }
        
        var endPos = value.lastIndexOf("}");
        if(endPos == -1) {
            return value;
        }
        var key = value.substring(startPos + 1, endPos);
        if(key.indexOf(":") > 0) {
            key = key.substring(0, key.indexOf(":"));
        }
        
        var newValue = map[key];
        
        if(newValue == null) {
            return value;
        }
        return (value.substring(0, startPos) + newValue + value.substring(endPos + 1));
    };
    
    var URI = "";
    if(this.schemeName != null) {
        URI += this.schemeName + "://";
    }
    if(this.hostName != null) {
        if(URI.length == 0) {
            URI += "http://";
        }
        URI += template(this.hostName);
    }
    
    if(this.portNum != null) {
        URI += ":" + this.portNum.toString();
    }
    
    if(URI.length > 0 && this.listOfSegments[0] != "/") {
        URI += "/";
    }
    
    for(var key in this.listOfSegments) {
        URI += encodeURIComponent(template(this.listOfSegments[key])) + ((key < this.listOfSegments.length - 1) ? "/" : ""); ;
    }
    
    if(this.listOfQueryParams.length > 0) {
        URI += "?";
        for(var key in this.listOfQueryParams) {
            URI += encodeURIComponent(template(this.listOfQueryParams[key].name)) + "=" + encodeURIComponent(template(this.listOfQueryParams[key].value)) + ((key < this.listOfQueryParams.length - 1) ? "&" : ""); 
        }
    }
    
    if(this.fragmentValue != null) {
        URI += "#" + encodeURIComponent(template(this.fragmentValue));
    }
    
    return URI;
};

/**
 * Create an UriBuilder representing relative URI initialized from URI path.
 * 
 * @param string path
 * @returns {UriBuilder}
 */
UriBuilder.fromPath = function(path) {
    var builder = new UriBuilder();
    
    var pathPos = 0; // path.indexOf("/") == 0 ? 1 : 0;
    var queryPos = path.indexOf("?");
    var fragmentPos = path.indexOf("#");
    
    var pathEnd = path.length;
    if(fragmentPos > 0) {
        pathEnd = fragmentPos;
    }
    if(queryPos > 0) {
        pathEnd = queryPos;
    }
    
    var justQuery = null;
    if(queryPos > 0) {
        var queryEnd = path.length;
        if(fragmentPos > 0) {
            queryEnd = fragmentPos;
        }
        justQuery = path.substring(queryPos + 1, queryEnd);
    }
    
    var justFragment = null;
    var fragmentEnd = path.length;
    if(fragmentPos > 0) {
        justFragment = path.substring(fragmentPos + 1, fragmentEnd);
    }
    
    var justPath = path.substring(pathPos, pathEnd);
    if(justPath[justPath.length - 1] == "/") {
        justPath = justPath.substring(justPath.length - 1);
    }
    
    builder.listOfSegments = justPath.split(/\//i);
    
    if(justQuery != null) {
        var listOfNVPs = justQuery.split(/&/i);
        for(var key in listOfNVPs) {
            var pair = listOfNVPs[key].split(/=/i);
            builder.queryParam(pair[0], pair[1]);
        }
    }
    
    if(justFragment != null) {
        builder.fragment(justFragment);
    }
    
    return builder;
};

/**
 * Create a new instance from an existing URI.
 * 
 * @param string uri
 * @returns {UriBuilder}
 */
UriBuilder.fromUri = function(uri) {
    var builder = new UriBuilder();
    var str = "";
    
    if(UriBuilder.hasSchema(uri)) {
        var scheme = UriBuilder._REGEX_SCHEMA.exec(uri)[1];
        if(scheme == "http" || scheme == "https") { // this should be smarter
            var domain = UriBuilder._REGEX_HOST.exec(uri);
            if(domain != null) {
                domain = domain[1];
                var port = null;
                if(UriBuilder.hasPort(uri)) {
                    port = UriBuilder._REGEX_PORT.exec(uri)[1];
                }
            }
            builder.scheme(scheme).host(domain).port(port);
            str = scheme + "://" + domain + (port == null ? "/" : (":" + port));
        }
    }
    
    var path = uri.substring(str.length);
    
    var pathPos = 0;
    var queryPos = path.indexOf("?");
    var fragmentPos = path.indexOf("#");
    
    var pathEnd = path.length;
    if(fragmentPos > 0) {
        pathEnd = fragmentPos;
    }
    if(queryPos > 0) {
        pathEnd = queryPos;
    }
    
    var justQuery = null;
    if(queryPos > 0) {
        var queryEnd = path.length;
        if(fragmentPos > 0) {
            queryEnd = fragmentPos;
        }
        justQuery = path.substring(queryPos + 1, queryEnd);
    }
    
    var justFragment = null;
    var fragmentEnd = path.length;
    if(fragmentPos > 0) {
        justFragment = path.substring(fragmentPos + 1, fragmentEnd);
    }
    
    var justPath = path.substring(pathPos, pathEnd);
    if(justPath[0] == "/") {
        justPath = justPath.substring(1);
    }
    if(justPath[justPath.length - 1] == "/") {
        justPath = justPath.substring(0, justPath.length - 1); // fix by Martynas :)
    }
    builder.path(justPath);
    
    if(justQuery != null) {
        var listOfNVPs = justQuery.split(/&/i);
        for(var key in listOfNVPs) {
            var pair = listOfNVPs[key].split(/=/i);
            builder.queryParam(pair[0], pair[1]);
        }
    }
    
    if(justFragment != null) {
        builder.fragment(justFragment);
    }
    
    return builder;
};

/**
 * Create new instance
 * @returns {UriBuilder}
 */
UriBuilder.newInstance = function() {
    return new UriBuilder();
};

/**
 * Check if uri has schema.
 * 
 * @param uri
 * @returns {Boolean}
 */
UriBuilder.hasSchema = function(uri) {
    return UriBuilder._REGEX_SCHEMA.test(uri);
};

/**
 * Check if uri has port.
 * 
 * @param uri
 * @returns {Boolean}
 */
UriBuilder.hasPort = function(uri) {
    return UriBuilder._REGEX_PORT.test(uri);
};
