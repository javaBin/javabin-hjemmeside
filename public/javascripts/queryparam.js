var getQueryParams = function(url) {
    var result = {};
    var params = (url.split('?')[1] || '').split('&');
    for (var param in params) {
        if (params.hasOwnProperty(param)) {
            paramParts = params[param].split('=');
            result[paramParts[0]] = decodeURIComponent(paramParts[1] || "");
        }
    }
    return result;
}
