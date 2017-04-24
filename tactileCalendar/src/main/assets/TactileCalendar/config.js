numberOfDays = 5

width = 1200;
height = 600;

svgVersionNr = 0

timeMin = new Date() //use Date() for today
timeMax = new Date(timeMin)
timeMax.setDate(timeMax.getDate() + numberOfDays)
