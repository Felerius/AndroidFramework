

function getEventsFromAndroid(){

  console.log("requesting events from google api");
  var request = JSON.parse(Android.getGoogleCalendarEvents());
  console.log(request["events"].length)

  var events = request["events"];
  var newEvents = [];
  for (var i = 0; i < events.length; i++) {
        var event = JSON.parse(events[i]);
        console.log("-------------");
        console.log(event.summary);
        console.log("id: ");
        console.log(event.id);
        console.log("start: ");
        console.log(event.start.dateTime);
        console.log("end: ");
        console.log(event.end.dateTime);
        console.log("-------------");
        newEvents.push(event);
      }
      renderEvents(newEvents);
}

getEventsFromAndroid();