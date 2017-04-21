weekDays = ["Monday","Tuesday","Wednesday","Thursday","Friday","Saturday","Sunday"]
hourMarks = ["12AM", "1AM", "2AM", "3AM", "4AM", "5AM", "6AM", "7AM", "8AM", "9AM", "10AM", "11AM",
			 "12PM", "1PM", "2PM", "3PM", "4PM", "5PM", "6PM", "7PM", "8PM", "9PM", "10PM", "11PM",]

var lastDay
var talkingSpeed = 1.5

var commands = {
	'create event *summary from here': startEventCreation,
	'to here': endEventCreation,
	'test' : function(){console.log("test");}
};
annyang.addCommands(commands);
annyang.start();

function timeInText(date) {
  var text = ""
  var hours = date.getHours()
  var minutes = date.getMinutes()
  if(hours == 0) {
    text += "12 "
  }
  else {
    text += (hours%12).toString() + " "
  }

  if(minutes != 0) {
	  text += minutes.toString() + " "
  }

  if(hours < 12) {
    text += "AM" 
  }
  else {
    text += "PM"
  }
  return text
}

function textFor(event) {
	var text = event.summary + 
			   " from " + 
			   timeInText(new Date(event.start.dateTime)) +
			   " to " + 
			   timeInText(new Date(event.end.dateTime)) 

	return text
}

function speak(text) {
    Android.speak(text);
	/*var msg = new SpeechSynthesisUtterance(text);
	msg.rate = talkingSpeed;
	window.speechSynthesis.speak(msg);
	console.log("Speaking...");*/
}

function cancelSpeech() {
	window.speechSynthesis.cancel();
}

function handleMouseOverEvent(e, i) {
  	speak(textFor(e));
	console.log("mouse over event " + e);
}

function handleMouseOverDay(e, i) {
	var day = weekDays[e.getDay()]
	if(day != lastDay) {
		lastDay = day
		speak(weekDays[e.getDay()])
		console.log("mouse over day " + e);
	}
}

function handleMouseOverHourMark(e, i) {
  	speak(hourMarks[e])
		console.log("mouse over mark " + e);
}


function handleSpeechInput(summary, startTime, endTime) {
	console.log("summary")
	console.log(summary)
	console.log("startTime")
	console.log(startTime)
	console.log("endTime")
	console.log(endTime)
}

var curEvent = {}

function getCursorTime() {
	console.log("cursorX:")
	console.log(cursorX)
	console.log("cursorY:")
	console.log(cursorY)
	cursorY -= 20
	var cursorDay = new Date(dayScale.invert(cursorX)*8.64e7)
	cursorDay.setHours(0,0,0,0)
	var cursorDate = new Date(cursorDay.getTime()+timeScale.invert(cursorY)*60000)
	console.log(cursorDate.toISOString())

	return cursorDate
}

function roundMinutes(date) {
	date.setHours(date.getHours() + Math.round(date.getMinutes()/60));
	date.setMinutes(0);
	return date;
}

function startEventCreation(summary) {
	console.log("Create event: ")
	console.log(summary)
	var startTime = getCursorTime()
	curEvent['summary'] = summary
	curEvent['start'] = {}
	curEvent['start']['dateTime'] = roundMinutes(startTime).toISOString()
	curEvent['start']['timeZone'] = 'Europe/Berlin'
}

function endEventCreation() {
	console.log("end event: ")
	var endTime = getCursorTime()
	curEvent['end'] = {}
	curEvent['end']['dateTime'] = roundMinutes(endTime).toISOString()
	curEvent['end']['timeZone'] = 'Europe/Berlin'
	console.log(curEvent)
	if(curEvent.hasOwnProperty("start")) {
	  	createEvent(curEvent)
	}
	else {
		console.log("No starttime specified")	
	}
	curEvent = {}
}

function handleSpeech(speechInput){
    speechInput = speechInput.toLowerCase();
    console.log("received " + speechInput);
    var patt = new RegExp("create event * from here");
    var pattFromHere = /create[\s]*event/i
    var pattToHere = /to[\s]*here/i
    var pattTest = new RegExp("test");
    if (pattFromHere.test(speechInput)){
        var substring = speechInput.substring(speechInput.indexOf("event") + 5);
        console.log(substring);
        startEventCreation(substring);
    } else if(pattToHere.test(speechInput)){
        endEventCreation();
    } else if(pattTest.test(speechInput)){
        console.log(speechInput.match("Test"));
    } else {
        console.log(speechInput);
    }
}
