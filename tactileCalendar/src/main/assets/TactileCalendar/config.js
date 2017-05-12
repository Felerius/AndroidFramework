numberOfDays = 5

width = 1200;
height = 600;
paperWidth = 200;
paperHeight = 270;

svgVersionNr = 0

timeMin = new Date() //use Date() for today
timeMax = new Date(timeMin)
timeMax.setDate(timeMax.getDate() + numberOfDays)
linepodNS = "http://hpi.de/baudisch/linepod";

var cursorX;
var cursorY;
