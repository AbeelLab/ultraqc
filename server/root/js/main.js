/**
 * This function is called when the page is loaded.
 */
$(document).ready(function() {
	// initialize variables
	var status, averageData, reload, currentQuality;
	
	// store selections of samples
	var selections = {}; 
	var sampleTableData = {samples: {}, selected: {}};
	
	
	// load the main table when the page is ready
	$('#table').bootstrapTable({
		url: "api/select",
		responseHandler: function(res) {
			// add additional fields from client-stored information
			res.rows.forEach(row => {
				row.key = row.Species + row.SequencingTechnology + row.Date;
				row.selectedSamples = sampleTableData.samples[row.key];
				row.selectedQuality = sampleTableData.selected[row.key];
			});
			return res;
		}, 
		onPostBody: () => {
			$(".tooltip").remove();
			$('#table [title]').tooltip();
		}
	});

	// (re)load the historic data area and line.
	function loadHistoricData() {
		var obj = serializeDateForm();
		
		if (reload) { clearTimeout(reload); }
		reload = setTimeout(function() {
			// ajax call to get the average data
			$.ajax({
				dataType: "json",
				method: "POST",
				url: "api/historic",
				data: JSON.stringify(obj),
				success : function(data) {	
					for(var metric in data.histMeans) {
						data.histMeans[metric].data.sort(sort);
						data.histRanges[metric].data.sort(sort);
					}
					averageData = data;
					callMakeGraphs(selections);
				}, error: console.log
			});
		}, 750);
		
	}
	
	/**
	 * Method to sort on x object.
	 */
	function sort(a, b) {
		return a.x - b.x;
	}
	
	// download a sample selection
	function downloadSelection(type) {
		return function() {
			var title = Object.keys(selections).length + "-samples.";
			$.post("api/download", JSON.stringify(Object.keys(selections)), (data) => {
				if (type == "csv") {
					var csvRows = data.map(el => [el.ID, el.Name, el.Species, el.Date].join(";"));
					csvRows.unshift(Object.keys(data[0]).join(";"));
					
					download(title + type, csvRows.join("\r\n"));
				} else {
					download(title + type, JSON.stringify(data));
				}
			}, "JSON");
		}
	}

	
	// call the makeGraphs function
	function callMakeGraphs(ids) {
		var rows = $("#sampleTable").bootstrapTable("getData").filter(row => ids[row.ID] == true);
		var idArr = Object.keys(ids);
		var title = (idArr.length > 0) ? idArr.length + " samples" : "";
		setSelectedSamples(idArr.length);
		
		if (status) { status.stop(); } 
		status = new Status(); 
		
		makeGraphs(idArr, averageData, title, status);
	}

	function onBadgeClick(quality) {
		// convert row array to list of ids
		function ids(rows) {
			if (rows.constructor === Array) {
				return rows.map(el => el.ID);
			} else {
				return [rows.ID];
			}
		}
		
		// handle building of table on badge click
		return function() {
			// destroy old table and clear selections
			$('#sampleTable').bootstrapTable("destroy").off().hide();
			$("#sampleTableContainer").addClass("loading");
			var initLoadingTime = new Date().getTime();
			
			scrollTo("#sampleTableAnchor");
			
			
			var row = $("#table").bootstrapTable("getData")[$(this).attr("row-index")*1];
			var setKey = row.key;

			// set selection
			$(".quality_dist.selected").removeClass("selected");
			$(this).addClass("selected");

			sampleTableData.selected = {};
			sampleTableData.selected[setKey] = quality;
		
			// build table
			currentQuality = quality;
			if (!averageData) { loadHistoricData() }

			$('#sampleTable')
				.on("check.bs.table check-all.bs.table", function(event, rows) {
					ids(rows).forEach(el => {
						if (!selections[el]) {
							selections[el] = true;
							increment(sampleTableData.samples, setKey, quality, 1);
						}
					});
					
					$('#table').bootstrapTable('getData').forEach((el, index) => {
						if (el.key == setKey) {
							$('#table tr:nth-child(' + (index + 1) + ') td:last').html(selectedSampleFormatter(sampleTableData.samples[setKey]));
						}
					});
					
					
					callMakeGraphs(selections);
				})
				.on("uncheck.bs.table uncheck-all.bs.table", function(event, rows) {
					ids(rows).forEach(el => {
						delete selections[el];
						increment(sampleTableData.samples, setKey, quality, -1);
					});
					
					$('#table').bootstrapTable('getData').forEach((el, index) => {
						if (el.key == setKey) {
							$('#table tr:nth-child(' + (index + 1) + ') td:last').html(selectedSampleFormatter(sampleTableData.samples[setKey]));
						}
					});
					
					callMakeGraphs(selections);
				})
				.bootstrapTable({ 
					url: "api/select/" + quality + "/" + row.Date + "/" + row.Species,
					responseHandler: function(res) {
						res.rows.forEach(row => row.checkbox = selections[row.ID] == true);
						return res;
					},
					onPostBody: () => {
						$(".tooltip").remove();
						$('#sampleTable [title]').tooltip()
						
						// show loading icon for at least 500 ms
						var diffTime = initLoadingTime - new Date().getTime();
						setTimeout(() => {
							$("#sampleTable").show();
							$("#sampleTableContainer").removeClass("loading");
						}, 500 - diffTime);
					}
				});
		}
	}
	
	// bind on click handlers to various elements
	$("body").on("click", "[row-index].bg-warning", onBadgeClick("avg"));
	$("body").on("click", "[row-index].bg-danger", onBadgeClick("bad"));	
	$("body").on("click", "[row-index].bg-success", onBadgeClick("god"));
	
	$("body").on("click", "#download-json-btn", downloadSelection("json"));
	$("body").on("click", "#download-csv-btn", downloadSelection("csv"));
	
	$("body").on("click", "span[badge-metric].badge", function() {
		// if the row of the clicked badge is not selected, add it to the selection
		var checkbox = $(this).parents("tr").find("input[type=checkbox]");
		if (!checkbox.prop("checked")) { checkbox.click(); }
		
		scrollTo("#chart-anchor-" + $(this).attr("badge-metric"));
	});	
	
	$("#clear-btn").on("click", function() {
		selections = {};
		sampleTableData = {samples: {}, selected: {}};
		callMakeGraphs({});
		$('#sampleTable').bootstrapTable("refresh");
		$('#table tr td:last').empty()
	});
	$("#hist_form").on("input, change", "input, select", loadHistoricData);
	
	// initialise tooltips and metric information
	$.each(config.metrics, function(metric, obj) {
       $("[data-field='" + metric + "']").attr("title", obj.tooltip);
    });
	$(".btn-help[title]").tooltip();
	
	// smooth scrolling
	$(document).on('click', 'a[href^="#"]', function (event) {
		event.preventDefault();
		
		scrollTo($.attr(this, 'href'));
	});
});



/**
 * Method to serialize the historic data form into an object.
 */
function serializeDateForm() {
	var obj = {};
	if (x = $("#hist_species").val()) { obj.species = x; }
	if (x = $("#hist_seqtec").val()) { obj.SequencingTechnology = x; }
	if (x = $("#hist_mult").val()) { obj.deviationMultiplier = x; }
	
	var date = {};
	if (x = $("#hist_date_from").val()) { date.from = x; }
	if (x = $("#hist_date_to").val()) { date.to = x; }
	
	if (Object.keys(date).length) { obj.date = date; }
	

	return obj;
}

/**
  * Set the sample selection and update the relevant UI elements.
  */
function setSelectedSamples(amount) {
	$("#selectedSamples").text(amount);
	
	$("#download-buttons button, #clear-btn").prop("disabled", amount == 0);
}