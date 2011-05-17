$(function() {

            $("a.maplink").click(function(event) {
                        console.log("foo");
                        event.preventDefault();

                        var link = $(this);
                        var params = getQueryParams(link.attr("href"));
                        console.log(params);
                        $('#kart').html(params['location'] + ", " + params['address'] + "<br/>" + "<img src=\"http://maps.google.com/maps/api/staticmap?center=" + encodeURIComponent(params['address']) + "&zoom=14&size=590x540&maptype=roadmap&markers=color:yellow%7C" + encodeURIComponent(params['address']) + "&sensor=false\" alt=\"Her skal vi møtes!\"/>");

                        $('#kart').dialog({
                                    modal: true,
                                    resizable: false,
                                    width : 625,
                                    height : 625
                                });
                    });

            $("a.participantList").click(function(event) {
                        event.preventDefault();

                        var participants = $(this).parent().parent().find("div.participants").html();
                        $('#participants').html(participants);
                        $('#participants').dialog({
                                    maxHeight : 600,
                                    modal: true
                                });
                    });

            $("a.attend").click(function(event) {

                        event.preventDefault();
                        var url = $(this).attr("href");
                        var submitUrl = url.split("?")[0];
                        var params = getQueryParams(url);

                        console.log(submitUrl);
                        console.log(params);
                        //need to set id and title since this popup is used for several events.
                        $('#eventId').val(params['eventId']);
                        $('#eventTitle').html(params['eventTitle']);

                        $('#registrering').dialog({
                                    modal: true,
                                    width : 420,
                                    buttons: {
                                        'Meld meg på!': function() {
                                            $.post(submitUrl, {eventId : $('#eventId').val(), randomId : $('#randomId').val(), code : $('#code').val(), name:  $('#name').val(), email: $('#email').val(), howMany: $('#howMany').val()}, function(data) {
                                                        if (data['errors'].length > 0) {
                                                            for (var i = 0; i < data['errors'].length; i++) {
                                                                $('#' + data['errors'][i].key).addClass('ui-state-error');
                                                            }
                                                        } else {
                                                            $('#registrering').dialog('close');
                                                        }
                                                    });

                                        }
                                    }
                                });


                    });
        });


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