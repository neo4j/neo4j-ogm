/**
 * JavaScript for navigation in multi-page editions of Neo4j documentation.
 */

function isElementInViewport (el) {
    if (typeof jQuery === "function" && el instanceof jQuery) {
        el = el[0];
    }
    var rect = el.getBoundingClientRect();
    return (
        rect.top >= 0 &&
        rect.left >= 0 &&
        rect.bottom <= (window.innerHeight || document.documentElement.clientHeight) && /*or $(window).height() */
        rect.right <= (window.innerWidth || document.documentElement.clientWidth) /*or $(window).width() */
    );
}

$(document).ready(function() {
    var $title = $(
            'h1,h2,h3,h4'
            ).first();
    var $navtitle = $('.nav-title');
    var visible = isElementInViewport($title);
    if (visible) {
        $navtitle.hide();
    }
    $navtitle.removeClass('hidden');

    function showHide(nowVisible) {
        if ($(window).width() >= 768 && visible !== nowVisible) {
            $navtitle.fadeToggle();
            visible = !visible;
        }
    }
    var timeoutId = null;
    addEventListener("scroll", function() {
        if (timeoutId) clearTimeout(timeoutId);
            timeoutId = setTimeout(showHide, 200, isElementInViewport($title));
    }, true);

    setNavIconColor();
});

function setNavIconColor() {
    var color = null;
    $('.nav-previous > a, .nav-next > a').hover(function (){
        $me = $(this);
        $me.children('span.fa').css('border-color', $me.css('color'));
    }, function(){
        $(this).children('span.fa').css('border-color', "");
    });
}
