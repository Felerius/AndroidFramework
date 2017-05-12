//event creation

var eventStartTime;
var eventEndTime;

var eventNm;
var fsm = new machina.Fsm({
 initialState: 'idle',
  states: {
   idle: {
     //this.emit('speech', 'You can feel your calendar by hovering over the paper')
     newEvent: function() {
       this.emit('speech', 'Creating new event. You can always say back to go one step back and cancel to cancel the event creation. Let us first get a good event name. How should your event be named?');
        this.transition('settingNewEventName');
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
      }
    },

    selectingStartTime: {
      selectingTime: function (startTime){
          eventStartTime = startTime;
          this.emit('speech', 'Ok, the start time of the event is ' + startTime + '. When should the event end? Please tap on the end time and say select');
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

    },

    selectingEndTime: {
      selectingTime: function (endTime){
          this.emit('speech', 'Your event ends ' + endTime + '. Say "create" if you want to create the event ' + eventNm + ', starting on ' + eventStartTime + ' and ending on ' + endTime + ', now.');
          this.transition('creatingEvent');
        },
      cancel: function() {
         this.emit('speech', 'Canceling Event creation');
         this.transition('idle');
      },
      back: function() {
          this.emit('speech', 'Going back. Please select a new start time by hovering over the desired time-slot on the paper and saying select');
          this.transition('selectingStartTime');
      }
    },


    creatingEvent: {
       createEvent: function (){
              this.emit('speech', 'Created event ' + eventNm + ' from ' + eventStartTime + ' to ' + eventEndTime);
              //create event in Android
              Android.createEvent(eventNm, eventStartTime, eventEndTime);
              this.transition('idle');
            }
        },
        back: function() {
            this.emit('speech', 'Going back. Please select a new end time by hovering over the desired time-slot on the paper and saying select');
            this.transition('selectingEndTime');
        }

  }
});

fsm.on('speech', function(text) {
    Android.speak(text);
});

