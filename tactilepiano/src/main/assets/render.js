

const lastPlayed = "lastPlayed";

var svg = d3.select("body")
.append("svg")
.attr("width", width)
.attr("height", height)
.attr("id","svg");


var mainGroup = svg.append("g")
.attr("id","mainGroup");

var tones = ["c","d","e","f","g","a","b","c5"];
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
.on("click", onMouseOverKey);
//.attr(lastPlayed, Date.now())

function onMouseOverKey(tone){
    //var el = d3.select(this);
    //console.log(el.attr(lastPlayed));
    //if (Date.now() - el.attr(lastPlayed) > 150){
    Android.playTone(tone);
    //}
    //el.attr(lastPlayed, Date.now());
}
