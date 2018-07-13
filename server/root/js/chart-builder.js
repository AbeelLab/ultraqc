/**
 * Method to make the metric graphs containing all the rows in the rows param. A title with the title from.
 * the title param will be shown above the graphs.
 *
 * @param rows The rows
 * @param averageData The average data to draw the mean and range area
 * @param title The title of the graph
 * @param status The status
 */
function makeGraphs(rows, averageData, title, status) {
    var container = $("<div class='col-xl-10 offset-1'>").append( $("<h1>").html(title) );
    $("#chartContainer").empty().append(container);

    if (rows.length == 0) { return; }

	$("#graphMenu").empty();
    for (var metric in config.metrics) {		
        // add containers for the charts
        $(container)
			.append( $("<div>").addClass("anchor").attr("id", "chart-anchor-" + metric) )
			.append( $("<div>").addClass("col-xl-12 chartContainer loading").attr("id", "chart-container-" + metric) );

		addMenuItem("#chart-anchor-" + metric, config.metrics[metric].display);
	}
	
	var metric_loop = Object.keys(config.metrics);
	
    // Loop that waits for asynch body to complete before starting the next iteration.
    (function loop() {
        if (metric_loop.length == 0) { return; }

        var metric = metric_loop.shift();

        // wait for graph to be drawn before fetching next
        drawGraphFromAjax(metric, rows, averageData, "chart-container-" + metric, status).then(loop);
    })();
}

/**
 * Metho to draws graph from the data from AJAX requests
 *
 * @param metric The metric to be drawn
 * @param rows The rows
 * @param averageData The average data to draw the mean and range area
 * @param element The element
 * @param status The status
 * @returns {Promise} The graph
 */
function drawGraphFromAjax(metric, rows, averageData, element, status) {
    return new Promise(function(resolve, reject) {
        // check if the page is scrolled far enough down to load this element yet
        if (isAlmostInView($("#" + element))) {
            ajax();
        } else {
            // if not, check whenever the window scrolls and load it when needed
            $(window).on("scroll.pending-ajax-" + element, function() {
                if (isAlmostInView($("#" + element))) {
                    $(window).off("scroll.pending-ajax-" + element);
                    ajax();
                }
            });
        }

        function ajax() {
            if (!status.isActive()) { return; }
            $.ajax({
                dataType: "JSON",
                method: "POST",
                url: "api/select/" + metric,
                data: JSON.stringify(rows),
                success : function(data) {
                    // stop execution if a new dataset was requested
                    if (!status.isActive()) { return; }

					if (averageData) {
						if(config.metrics[metric].type !== "bar") {
							data.series.push(averageData.histMeans[metric]);
							data.series.push(averageData.histRanges[metric]);
						} else {
							data.categories.unshift("Historic Average");
							data.series[0].data.unshift({y: averageData.histMeans["avg_gc_content"].data[0].y, color: "green"});
						}						
					} else {
						console.log("WARNING: averageData is not defined");
					}

                    addChart(element, metric, data, function() {
                        $("#" + element).removeClass("loading");
                        resolve();
                    });
                },
                error: function(err) {
                    console.log(err);
                    reject();
                }
            });
        }
    });
}

/**
 * Method to add a chart to a given div.
 *
 * @param target The ID of the target div
 * @param metric The name of the metric, will be used to fetch the config file
 * @param data The data to draw in the chart
 * @param callback Called upon completion of chart drawing
 */
function addChart(target, metric, data, callback) {
    var counts = data.counts;
    var bars = generateBarHtml(counts, data.series.map(el => el.name));
    var chartElement = target + "_CHART";
	var target = $("#" + target);

    target
        .empty()
        .append(
			$("<h3>")
				.append( 
					$("<button type='button' class='btn btn-outline-primary btn-help'>")
						.html("?")
						.attr("title", config.metrics[metric].tooltip)
						.tooltip()
				)
				.append( " " + config.metrics[metric].display )
			);
		
	if(config.metrics[metric].type !== "bar") {
		target.append(bars);
	}
	
	target.append($("<div>").attr("id", chartElement));
	
    var chart = linechart(chartElement, data.series, config.charts[metric], data.categories, callback);
	
	if(config.metrics[metric].type === "bar") {
		var min = 0.95 * Math.min.apply(Math, data.series[0].data.map(el => el.y || el));
		
		chart.yAxis[0].setExtremes(min);
		chart.showResetZoom();
	}

    bindBarHandlers(bars, chart);
}

/**
 * Method to bind handlers to enable clicking on the bar to hide/show data.
 *
 * @param el The element
 * @param chart The chart
 */
function bindBarHandlers(el, chart) {
    el.find(".progress-bar").on("click", function() {
        var t = $(this);

        if (t.hasClass("unselected")) { $(this).removeClass("unselected"); }
        else { $(this).addClass("unselected"); }

        var enabled = el.find(".progress-bar").not(".unselected").map((i, el) => $(el).attr("quality")).toArray();
        barfn = q => enabled.indexOf(q.quality) >= 0;
        updateChart();
    }).addClass("clickable");

    var filter = el.find(".filter").on("input change click", function() {
        var input = toRegex($(this).val());
        filterfn = el => input.test(el.name.toLowerCase());

        updateChart();
    });

    // two functions for showing charts, both default to returning true (showing all)
    var filterfn = () => true;
    var barfn = () => true;

    // redraw the chart after updating the visibility for each sample
    var updateChart = function() {
        showIf(chart, filterfn, barfn);
        chart.redraw();
    }

    el.find(".filter-clear").on("click", () => filter.val("").trigger("change"));
}

/**
 * Method to show ONLY the series of the given chart for which both fna and fnb return true.
 * when called with the sample's options.
 *
 * @param chart The chart
 * @param fna Function A
 * @param fnb Function B
 * @returns {boolean} Boolean if it should be shown
 */
function showIf(chart, fna, fnb) {
    return chart.series.forEach(el => el.setVisible(el.options.hisLine || fna(el.options) && fnb(el.options), false));
}

/**
 * Method to generate the bars.
 *
 * @param counts The counts
 * @param names The names
 * @returns {Packet|MediaStream|Response|MediaStreamTrack|Request|*} The bar
 */
function generateBarHtml(counts, names) {
    var bars = $("#bar_template").clone();
    bars.removeAttr("id");
    bars.removeAttr("style");

    var total = counts.GOD + counts.BAD + counts.AVG;
    var widths = {
        AVG: Math.round(100*counts.AVG/total),
        BAD: Math.round(100*counts.BAD/total)
    }
    widths.GOD = 100 - (widths.BAD + widths.AVG);

    bars.find(".progress-bar").each(function(i, el) {
        var q = $(el).attr("quality");
        $(el).css("width", widths[q] + "%").text(counts[q]);
    });

    return bars;
}

/**
 * Chart initialisations. For information on chart types, axes, legend, colours etc, see the
 * documentation at: https://www.highcharts.com/docs
 * API reference at: https://api.highcharts.com/highcharts/
 * Highcharts purchase options and license agreement are found here: https://shop.highsoft.com/highcharts
 *
 * @param div The div to draw the line in
 * @param data The data to be drawn
 * @param config The config for the settings
 * @param callback Callback function
 * @returns {*|K.Chart} The chart
 */
function linechart(div, data, config, categories, callback) {
    var chart = Highcharts.chart(div, mergeDeep({
        chart: {
            zoomType: 'xy',
            events: { load: callback, click: clickHandler }
        },
        tooltip: {
            crosshairs: [true, true],
            formatter: function(a, b, c) {
				// if x is a string, its a bar chart (probably)
				if (isNaN(this.x)) {
					return "<b>" + this.x + "</b>"
						+"<br/>(" + this.y + ")</div>" ;					
				} else {
					return "<b>" + this.series.name + "</b>"
						+"<br/>(" + this.x + ", " + this.y + ")</div>" ;
				}
				
            }
        },
        legend: { enabled: false},
		xAxis: { categories: categories },
        boost: { enabled: true },
        title: { text: '' },
        plotOptions: {
            series: {
                marker: { enabled: false, symbol: "url(img/dot.svg)" },
                events: { click: clickHandler }
            }
        },

        series: data
    }, config));

    // destroy the chart when its element is removed
    $("#" + div).on("destroyed", function() {
        chart.destroy();
    });

    // functions to toggle names in the list of filters
    function toggleVal(el, str) { el.val() == str ? el.val("") : el.val(str) }
    function toggleValMultiple(el, str) { return el.val(el.val().split(";").clean().toggle(str).join(";")); }
    function clickHandler(event) {
        var clicked = event.point || event.toElement.point;
        if (!clicked) { return; }

        var filter = $("#" + div).parents(".chartContainer").find(".filter");
        if (event.ctrlKey) {
            toggleValMultiple(filter, "'" + clicked.series.name + "'");

            $("body").off("keyup.ctrlOnGraph").on("keyup.ctrlOnGraph", function(e) {
                console.log("released")
                if (e.key == "Control") { filter.trigger("change"); }
            });
        } else {
            toggleVal(filter,  "'" + clicked.series.name + "'");
            filter.trigger("change");
        }
    }
    return chart;
}