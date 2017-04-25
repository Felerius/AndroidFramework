
var svg = d3.select("body")
.append("svg")
.attr("width", width)
.attr("height", height)
.attr("id","svg");


var mainGroup = svg.append("g")
.attr("id","mainGroup");

var tones = ["c","d","e","f","g","a","b"];
var specialTones = ["cis","dis","fis","gis","ais"];
var i = 0;
mainGroup.selectAll(".keys")
.data(tones)
.enter().append("rect")
.attr("class","keys")
.attr("x", function(){
    var x = i*keyWidth;
    i+=1;
    return x;
}).attr("y",50)
.attr("width", keyWidth)
.attr("height",keyHeight)
.attr("stroke","black")
.attr("fill","white")
.on("mouseover", onMouseOverKey);

printSVG();

function onMouseOverKey(tone){
    Android.playTone(tone)
}

function getSVG(){

	var svg = document.getElementById("svg");
	//get svg source.
	var serializer = new XMLSerializer();
	var source = serializer.serializeToString(svg);

	//add name spaces.
	if(!source.match(/^<svg[^>]+xmlns="http\:\/\/www\.w3\.org\/2000\/svg"/)){
	    source = source.replace(/^<svg/, '<svg xmlns="http://www.w3.org/2000/svg"');
	}

	//add xml declaration
	source = '<?xml version="1.0" standalone="no"?>\r\n' + source;
	console.log(source);
    return source;
}

function getSVGDiff(){

	var svg = document.getElementById("svg");
	//get svg source.
    traverseDOM(svg);
    svgVersionNr+=1;
	var serializer = new XMLSerializer();

	var source = serializer.serializeToString(svg);

	//add name spaces.
	if(!source.match(/^<svg[^>]+xmlns="http\:\/\/www\.w3\.org\/2000\/svg"/)){
	    source = source.replace(/^<svg/, '<svg xmlns="http://www.w3.org/2000/svg"');
	}

	//add xml declaration
	source = '<?xml version="1.0" standalone="no"?>\r\n' + source;
	console.log(source);
    return source;
}

function traverseDOM(root){
    if (root.getAttribute('version')==null){
        root.setAttribute('version', svgVersionNr);
    }
    var children = root.childNodes;
    for (var i =0; i <children.length; i++){
        traverseDOM(children[i]);
    }
}

function printSVG(){

    Android.sendSVGToLaserPlotter(getSVGDiff(), svgVersionNr);
}

