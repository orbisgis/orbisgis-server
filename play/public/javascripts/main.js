function toggleInfo(id){
    var list = document.getElementsByClassName("Info");
    for (var i=0;i<list.length;i++){
        list[i].style.display="none";
    }
    document.getElementById(id).style.display="block";
};

function toggle(divtag){
    if(document.getElementById(divtag).style.display=='none'){
        document.getElementById(divtag).style.display='block';
    } else {
        document.getElementById(divtag).style.display='none';
    }
}

var toToggle = document.getElementsByClassName("toToggle");
var name;
for (var j=0;j<toToggle.length;j++)
{
    (function(id){
        id = toToggle[j].className.split(' ')[1];
        toToggle[j].addEventListener("click",function(){toggleInfo(id)}, false);
    }(name))
}
if(document.getElementById("imgtoggle")!=null){
    document.getElementById("imgtoggle").addEventListener("click",function(){toggle("addtoggle")},false);
}

$(function(){
        $("button.removePopup").click(function(){
            $(this).parent().children("form").children("div.message").css("display","inline");
        });

        $("button.removePopup2").click(function(){
            $(this).parent().css("display","none");
        });
});