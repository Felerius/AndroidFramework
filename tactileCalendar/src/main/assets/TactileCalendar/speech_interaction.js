//event creation

var eventStartTime;
var eventEndTime;
var eventStartTimeFormatted;
var eventEndTimeFormatted;

var eventNm;
var fsm = new machina.Fsm({
 initialState: 'appStarted',
  states: {
   appStarted: {

     print: function() {
       Android.startSVGTransmitter(true);

     },
     options: function(){
       this.emit('speech', "Do you want to print your calendar now then say 'print'. Just don't forget to check that your Linepod is turned on, has paper inserted and that it's lid is closed.");

     }
   },

   printing: {
    options: function(){
      this.emit('speech', "Our Linepod is printing your calendar right now. This might take a little");

    },
    printed: function(){
      this.transition("idle");
    }

   },


   idle: {
     newEvent: function() {
       this.emit('speech', 'Creating new event. You can always say back to go one step back and cancel to cancel the event creation. Let us first get a good event name. How should your event be named?');
        this.transition('settingNewEventName');
      },
      options: function(){
        this.emit("speech", "Your only available speech command is to start the event-creation-wizard by saying 'new event'")
      }
    },

    settingNewEventName: {
     cancel: function() {
       this.emit('speech', 'Canceling Event creation');
        this.transition('idle');
      },
      eventNameSet: function(eventName) {
       eventNm = eventName;
       this.emit('speech', 'Alright, I received ' + eventName + ' as event name. Please select the start time by hovering over the desired time-slot on the paper and saying select');
        this.transition('selectingStartTime');
      },
     options: function(){
       this.emit("speech", "You are now in the event-creation-wizard. Please tell me how you would like to name your event. If you want to cancel the event-creation just say cancel.")
     }
    },

    selectingStartTime: {
      selectingTime: function (startTime){
          eventStartTime = formatAMPM(startTime);
          eventStartTimeFormatted = startTime;
          this.emit('speech', 'Ok, the start time of the event is ' + eventStartTimeFormatted + '. When should the event end? Please tap on the end time and say select');
          this.transition('selectingEndTime');
       },
       cancel: function() {
          this.emit('speech', 'Canceling Event creation');
           this.transition('idle');
       },
       back: function() {
           this.emit('speech', 'Going back. Please tell me your new event name');
           this.transition('settingNewEventName');
       },
       options: function(){
         this.emit("speech", "You are currently in the event-creation-wizard of your event " + eventNm
          + ". If you want to change the event name just say 'back', otherwise say select while you hover over the desired start time of your event on the paper."
          + " Of course you can also cancel the event-creation by saying cancel.")
       }

    },

    selectingEndTime: {
      selectingTime: function (endTime){
          eventEndTime = formatAMPM(endTime);
          eventEndTimeFormatted = endTime;
          this.emit('speech', 'Your event ends ' + eventEndTimeFormatted + '. Say "create" if you want to create the event ' + eventNm + ', starting on ' + eventStartTimeFormatted + ' and ending on ' + eventEndTimeFormatted + ', now.');
          this.transition('creatingEvent');
        },
      cancel: function() {
         this.emit('speech', 'Canceling Event creation');
         this.transition('idle');
      },
      back: function() {
          this.emit('speech', 'Going back. Please select a new start time by hovering over the desired time-slot on the paper and saying select');
          this.transition('selectingStartTime');
      },
      options: function(){
        this.emit("speech", "You are currently in the event-creation-wizard of your event " + eventNm + " which starts at " + eventStartTimeFormatted
         + ". If you are not happy with the start time of the event you can select a new time after saying back. If you want to specify the end-time of your event say 'select' while you hover over the desired end time on the paper."
         + " As always you can also cancel the event-creation by saying cancel.") //maybe better: If I received a wrong start time of your event just say back
      }
    },


    creatingEvent: {
       createEvent: function (){
          this.emit('speech', 'Created event ' + eventNm + ' from ' + eventStartTimeFormatted + ' to ' + eventEndTimeFormatted);
          //create event in Android
          Android.createEvent(eventNm, eventStartTime, eventEndTime);
          this.transition('idle');

        },
        back: function() {
            this.emit('speech', 'Going back. Please select a new end time by hovering over the desired time-slot on the paper and saying select');
            this.transition('selectingEndTime');
        },
        options: function(){
          this.emit("speech", "You are currently in the event-creation-wizard of your event " + eventNm + " which starts at " + eventStartTimeFormatted + " and ends at " + eventEndTimeFormatted + ". If you are not happy with the end time of the event you can select a new time after saying back. Otherwise, if you want to create the event now just say 'create'. As always you can also cancel the event-creation by saying cancel.");
        }
    }
  }
});


fsm.on('speech', function(text) {
    Android.speak(text);
});

