var toToggle = document.getElementsByClassName("toToggle \d*");
for (var i=0; i < toToggle.length; i++){
    toToggle[i].addEventListener('onclick',toggleInfo(toToggle.className.split(' ')[1]), false);
}
function toggleInfo(id){
    var list = document.getElementsByClassName("Info")
    for (var i=0; i < list.length;i++){
        list[i].style.display='none'
    }
    document.getElementById(id).style.display='block'
}