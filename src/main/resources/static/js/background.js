window.onload = function() {
    var width = window.innerWidth || document.documentElement.clientWidth || document.body.clientWidth;
    var height = window.innerHeight || document.documentElement.clientHeight || document.body.clientHeight;

    var div = document.querySelector('.text-light');
    div.style.backgroundImage = "url('https://picsum.photos/" + width + "/" + height + "')";
}
