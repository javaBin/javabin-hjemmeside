if (typeof console == "undefined") {
  window.console = {
    log: function () {}
  };
}

$(function() {

  $(".eventActions .map").click(function(event) {
    event.preventDefault();

    var link = $(this);
    var params = getQueryParams(link.attr("href"));
    $('#kart').html(params['location'] + ", " + params['address'] + "<br/>" + 
                    "<img src=\"http://maps.google.com/maps/api/staticmap?center=" + encodeURIComponent(params['address']) + 
    "&zoom=14&size=590x540&maptype=roadmap&markers=color:yellow%7C" + encodeURIComponent(params['address']) + "&sensor=false\" alt=\"Her skal vi møtes!\"/>");

    $('#kart').dialog({
      modal     : true,
      resizable : false,
      width     : 625,
      height    : 625
    });
  });

  $(".eventActions .participants").click(function(event) {
    event.preventDefault();

    var participants = $(this).parent().parent().find("div.participants").html();
    $('#participants').html(participants);
    $('#participants').dialog({
      maxHeight : 600,
      maxWidth: 600,
      modal     : true
    });
  });

  $(".eventActions .attend").click(function(event) {

    event.preventDefault();
    var url = $(this).attr("href");
    var submitUrl = url.split("?")[0];
    var params = getQueryParams(url);

    $('#eventId').val(params['eventId']);
    $('#eventTitle').html(params['eventTitle']);


    $('#registrering').dialog({
      modal : true,
      width : 420,
      buttons: {
        'Meld meg på!': function() {
          $.post(submitUrl, {
            eventId  : $('#eventId').val(),
            randomId : $('#randomId').val(),
            code     : $('#code').val(),
            name     : $('#name').val(),
            email    : $('#email').val(),
            howMany  : $('#howMany').val()}, function(data) {
              if (data['errors'].length > 0) {
                for (var i = 0; i < data['errors'].length; i++) {
                  $('#' + data['errors'][i].key).addClass('ui-state-error');
                }
              } else {
                //Her burde vi egentlig legge til div'en i dommen ikke reloaded
                location.reload(true);
              }
            });
        }
      }
    });
  });

  $('a.speaker').click(function(event) {
    event.preventDefault();
    var link = $(this);
    var params = getQueryParams(link.attr("href"));
    var id = "";
    var gravatar = "";
    var name = "";
    if(params['lectureholderId'] != undefined) {
      id = params['lectureholderId'];
      gravatar = params['gravatarId'];
      name = params['name'];
    }

    $('#lectureholderId').val(id);
    $('#fullName').val(name);
    $('#gravatarId').val(gravatar);
    $('#lectureholder').dialog({
      modal: true,
      resizable: false,
      width : 400,
      buttons : {
        'Opprett': function() {
          $('#lectureholderForm').submit();
        }
      }
    });
  });

  $('a.newspeaker').click(function(event) {
    event.preventDefault();

    var url = $(this).attr("href");
    var submitUrl = url.split("?")[0];
    var params = getQueryParams(url);

    $('#addLectureholder').dialog({
      modal: true,
      buttons: {
        'Legg til foreleser': function() {
          $.post(submitUrl, {
            eventId               : params['eventId'] ,
            lectureholderId       : $('#lectureHolderId').val(),
            lectureholderName     : $('#lectureholderAC').val(),
            lectureholderGravatar : $('#gravatarId').val()}, function(data){
              //her burde vi egentlig lage en ny lectureholder i dom
              location.reload(true);
            });
            $( this ).dialog( 'close' );
        }
      }
    });
  });

  $("a.rmspeaker").click(function(event) {
    event.preventDefault();
    var url = $(this).attr("href");
    var submitUrl = url.split("?")[0];
    var params = getQueryParams(url);

    console.log(params);
    var id = params['lectureholderId'];
    $.post(submitUrl, { eventId: params['eventId'], lectureholderId: params['lectureholderId']}, function(data){
      $('#' + id).toggle();});
  });


  $("a.addparticipant").click(function(event){
    event.preventDefault();
    var url = $(this).attr("href");
    var submitUrl = url.split("?")[0];
    var params = getQueryParams(url);


    $('#addParticipant').dialog({
      modal: true,
      resizable: false,
      buttons: {
        'Legg til deltager': function() {
          $.post(submitUrl, {
            eventId          : params['eventId'],
            participantName  : $('#participantName').val(),
            participantEmail : $('#participantEmail').val()}, function(data){
                location.reload(true);
                //her burde vi fikse dom og ikke reloade
            });
            $( this ).dialog( 'close' );
        }
      }
    });
  });

  $("a.cuevent").click(function(event){
    event.preventDefault();
    $('#eventForm').submit();
  });

  $("a.sendmail").click(function(event) {
    event.preventDefault();
    var url = $(this).attr("href");
    var submitUrl = url.split("?")[0];
    var params = getQueryParams(url);

    if(confirm('Er du sikker på at du vil du sende ut mail til alle disse menneskene?')){
      $.post(subtmitUrl, {eventId : params['eventId']}, function(data){alert('Mail er sendt.')});
    }
  });

  $("a.rmparticipant").click(function(event) {
    event.preventDefault();
    var url = $(this).attr("href");
    var submitUrl = url.split("?")[0];
    var params = getQueryParams(url);
    if(confirm('Er du sikker på at du vil fjerne deltageren?')){
      $.post(submitUrl, {eventId : params['eventId'], participantId: params['participantId']}, function(data){
        $('#par' + params['participantId']).toggle();
        $('#parx' + params['participantId']).toggle();
      });
    }
  });

});
