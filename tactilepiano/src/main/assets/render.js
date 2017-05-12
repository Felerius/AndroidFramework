
var linepodNS = "http://hpi.de/baudisch/linepod";
const lastPlayed = "linepod:lastPlayed";

var hoveredTone = "";
var removedItems = {};

var svg = d3.select("body")
.append("svg")
.attr("width", width)
.attr("height", height)
.attr("id","svg");
//.on("click", playTone);

d3.namespaces.linepod = linepodNS;

var defs = svg.append("defs")

defs.append("pattern")
	.attr("id", "keyPlateau")
	.attr("x", 0)
	.attr("y", 0)
	.attr("width", keyWidth)
	.attr("height", 2)
	.attr("patternUnits", "userSpaceOnUse")
	.append("line")
		.attr("x1", 0)
		.attr("y1", 0)
		.attr("x2", 20)
		.attr("y2", 0)
		.attr("stroke", "black");

defs.append("pattern")
	.attr("id", "playTonePlateau")
	.attr("x", 0)
	.attr("y", 0)
	.attr("width", 100)
	.attr("height", 10)
	.attr("patternUnits", "userSpaceOnUse")
	.append("line")
		.attr("x1", 0)
		.attr("y1", 0)
		.attr("x2", 20)
		.attr("y2", 0)
		.attr("stroke", "black");





/*var mainGroup = svg.append("g")
.attr("id","mainGroup");
*/
var tones = ["c","d","e","f","g","a","h","q","f","t"];
var blackTones = ["cis","dis","fis","gis","ais"];
var i = 0;
svg.selectAll(".keys")
.data(tones).
enter().append("rect")
.attr("class","keys")
.attr("y", function(){
    var x = i*keyWidth + i * keyOffset;
    i=i+1;
    return x;
}).attr("x",50)
.attrs(normalKeyAttrs)
.on("mouseover", onMouseOverKey)
.attr(lastPlayed, Date.now());
/*
*/


svg.selectAll(".blackKeys")
.data(blackTones).
enter().append("circle")
.attr("class","blackKeys")
.attr("cy", function(d){
    var x = 0;
    var i = 0;
    console.log("data " + d);
    switch(d){

    	case "cis": i = 1; break;
		case "dis": i = 2; break;
    	case "fis": i = 4; break;
    	case "gis": i = 5; break;
    	case "ais": i = 6; break;


    }
    x = i * keyWidth + (i-1) * keyOffset + blackKeyOffset;
    return x;
}).attr("cx",30)
.attr("r",10)
.on("mouseover", onMouseOverKey)
.attr(lastPlayed, Date.now());

svg.append("rect")
.attr("x", 100)
.attr("y",0)
.on("click",playTone)
.attrs(playToneAttrs);



function onMouseOverKey(tone){
	hoveredTone = tone;
	/*var el = d3.select(this);
    console.log(el.attr(lastPlayed));
	el.attr(lastPlayed, Date.now());
    if (Date.now() - el.attr(lastPlayed) > 1500){
    	Android.playTone(tone)
	}*/
}

function onMouseOverBlackKey(tone){

    hoveredTone = tone;
	/*var el = d3.select(this);
	el.attr(lastPlayed, Date.now());
    if (Date.now() - el.attr(lastPlayed) > 1500){
    	//Android.playTone(tone)
	}*/
}

function playTone(){
    Android.playTone(hoveredTone)

}
