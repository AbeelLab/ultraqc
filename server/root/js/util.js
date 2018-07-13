/**
 * Method for a simple object check.
 * source: https://stackoverflow.com/a/34749873
 *
 * @param item The item
 * @returns {*|boolean} Boolean if it is an object
 */
function isObject(item) {
    return (item && typeof item === 'object' && !Array.isArray(item));
}

/**
 * Method to increment selection values.
 */
function increment(obj, key, quality, amount) {
	if (!obj[key]) { obj[key] = {god: 0, avg: 0, bad: 0}; }
	
	obj[key][quality] += amount;
}

/**
 * Smoothly scroll to a given element.
 *
 * @param selector The element's jQuery selector
 */
function scrollTo(selector) {
	var elements = $(selector);
	if (elements.length == 0) return;
	
	$('html, body').animate({
		scrollTop: elements.offset().top - 10
	}, 500);
}

/**
 * Method to add a menu item to the sidebar menu.
 *
 * @param target The target anchor ID
 * @param averageData The display name in the sidebar menu
 */
function addMenuItem(target, title) {
	var item = $("#templateGraphMenuItem").clone().removeAttr("id");
	item.find(".nav-link").attr("href", target);
	item.find(".nav-title").html(title);
	
	$("#graphMenu").append(item.show());
}

/**
 * Method to deep merge two objects.
 * source: https://stackoverflow.com/a/34749873
 *
 * @param target The target
 * @param source The source
 * @returns {*} The merged object
 */
function mergeDeep(target, source) {
    if (isObject(target) && isObject(source)) {
        for (const key in source) {
            if (isObject(source[key])) {
                if (!target[key]) Object.assign(target, { [key]: {} });
                mergeDeep(target[key], source[key]);
            } else {
                Object.assign(target, { [key]: source[key] });
            }
        }
    }

    return target;
}

/**
 * Class to keep track of the status of a particular action. Status.stop() can be called at the
 * callsite to indicate to the running function that it must stop execution. Mainly useful for slow
 * async functions, such as AJAX requests.
 **/
class Status {
	constructor() {
		this.status = true;
	}
	
	// set the status to false to indicate execution must be stopped
	stop() {
		this.status = false;
	}
	
	// poll the current status
	isActive() {
		return this.status;
	}
}

/**
 * Method to convert string to regex by escpating all regex characters, and then replacing
 * the semicolons and asterisks to allow for wildcards and OR conditions.
 *
 * @param str The string to be converted
 * @returns {RegExp} The regex expression
 */
function toRegex(str) {
	return new RegExp(
		str
			.replace(/[\-\[\]\/\{\}\(\)\+\?\.\\\^\$\|]/g, "\\$&")
			.split(";")
			.replace(/^['](.*)[']$/, "^$1$")
			.replace(/^["](.*)["]$/, "^$1$")
			.join("|")
			.split("*").join(".")
			.toLowerCase()
	);
		
}

/**
 * Method to call replace on each element in the array.
 * @param frm The element to be replaced
 * @param to The new value
 * @returns {Array} The updated array
 */
Array.prototype.replace = function(frm, to) {
	this.forEach((el, i) => this[i] = el.replace(frm, to))
	return this;
}

/**
 * Method to toggle a value in an array -- add the element if it is missing, but remove the
 * element if it is already there.
 *
 * @param newval The new value
 * @returns {Array} The updated array
 */
Array.prototype.toggle = function(newval) {
	var index = this.indexOf(newval);
	
	if (index == -1) {
		this.push(newval)
		return this;
	} else { 
		this.splice(index, 1) 
		return this;
	}
}

/**
 * Method to remove empty elements from an array.
 * @returns {Array} The updated array
 */
Array.prototype.clean = function() {
	for (var i = 0; i < this.length; i++) {
		if (this[i] == "") {         
			this.splice(i, 1);
			i--;
		}
	}
	return this;
};

/**
 * Method to add jQuery event for destroyed elements ("destroyed").
 * @type {{remove: $.event.special.destroyed.remove}}
 */
$.event.special.destroyed = {
	remove: function(o) {
		if (o.handler) {
			o.handler()
		}
	}
}

/**
 * Method to check if an element is almost in view, determined by being half of the window
 * height from the bottom of the page. Will also return true is the element is out of
 * view above the page instead of below.
 *
 * @param el The element
 * @returns {boolean} Boolean if an element is almost in view.
 */
function isAlmostInView(el) {
	if (!el.offset()) { return true; }
	return $(document).scrollTop() + $(window).height()*1.5 > el.offset().top
}


/**
 * Method to download a text file.
 * source: https://stackoverflow.com/a/18197341/8464233
 *
 * @param filename The name of the file to download
 * @param text the contents of the file
 */
function download(filename, text) {
  var el = $("<a>");
  el.text("ax")
	.attr('href', 'data:text/plain;charset=utf-8,' + encodeURIComponent(text))
	.attr('download', filename).hide();

  $("body").append(el);
  el[0].click();
  el.remove();

}