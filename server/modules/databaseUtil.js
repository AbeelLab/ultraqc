module.exports = function(config) {
	var exports = {};
	
	exports.addRange = addRange;
	exports.addFilters = addFilters;
	exports.buildHistoric = buildHistoric;
	exports.buildFilter = buildFilter;
	exports.buildQuery = buildQuery;
	exports.buildSelector = buildSelector;
	exports.buildQueryListOfMetrics = buildQueryListOfMetrics;
	exports.buildQueryParts = buildQueryParts;
	exports.buildQueryPartFromRange = buildQueryPartFromRange;
	exports.countQuality = countQuality;
	exports.ensureCallback = ensureCallback;
	exports.merge = merge;
	exports.getQuality = getQuality;
	exports.afterRange = afterRange;
	exports.beforeRange = beforeRange;
	exports.inRange = inRange;
	exports.groupBy = groupBy;
	
	/**
	 * Method to only select the good samples.
     * @param base The base
     * @param metric The metric
     * @returns {*} The good samples.
     */
	function addRange(base, metric) {
		return base.replace("#RANGE#", 
			buildQueryPartFromRange("GOD")({metric: metric, range: config.metrics[metric].range})[1]
		);
	}
	
	
	/**
	 * Method to add optional filters to the historic data queries.
	 */
	function addFilters(base, params) {
		var inputs = [];
		var seqTech = "";
		if (params.hasOwnProperty('sequencingTechnology')) {
			seqTech = "AND SequencingTechnology = ?";
			inputs.push(params.sequencingTechnology);
		}
		
		var fromDate = "";
		var toDate = "";
		if (params.hasOwnProperty('date')) {
			if (params.date.hasOwnProperty('from')) {
				fromDate = "AND Date >= ?";
				inputs.push(params.date.from);
			}
			if (params.date.hasOwnProperty('to')) {
				toDate = "AND Date <= ?";
				inputs.push(params.date.to);
			}
		}
		
		return [
			base.replace("#FILTERS#",[seqTech, fromDate, toDate].join(" ")),
			inputs
		];
	}
	
	/**
	 * Method that build the selector for the historic queries.
	 */
	function buildHistoric(base, metric, params) {
		var temp = addRange(base, metric);
		return addFilters(temp, params);
	}
	
	/**
	 * Method to build a filter for the samples table.
	 * @param filter Filter object containing regular expressions
	 * @returns {*} The filter in string form.
	 */
	function buildFilter(filter) {
		const allowedKeys = ["SampleName", "Coverage", "Alignment", "Duplication", "score"];
		
		var result1 = "";
		var inputs1 = [];
		
		var result2 = "";
		var inputs2 = [];
		
		if (filter) {
			for (var key in filter) {
				if (key === "avg" || key === "bad") {
					if (result2 === "") {
					result2 = "WHERE " + key + " REGEXP ?";
					} else {
						result2 += " AND " + key + " REGEXP ?";
					}
					inputs2.push(filter[key]);
				} else if (allowedKeys.indexOf(key) >= 0){
					result1 += " AND " + key + " REGEXP ?";
					inputs1.push(filter[key]);
				}
			}
		}
		return [result1, inputs1, result2, inputs2];
	}

	
	/**
	 * Method to build a query.
     * @param base The input, for instance: [{date: 2018, spec: "abc"}, {date: 2017, spec: "def"}, {date: 2016}]
     * @param rows The rows
     * @returns {{query: *, params: *}} The output, for instance: (DATE = ? AND spec = ?) OR (DATE = ? AND spec = ?) OR (Date = ?)
     */
	function buildQuery(base, rows) {
		var selector = "(" + rows.map(el => "ID = ?").join(" OR ") + ")";
		
		var query = base.replace("#", selector);
		return {query: query, params: rows};
	}
	
	/**
	 * Method to build a selector for a particular quality range.
	 *
	 * @param quality The quality
	 * @returns {boolean} The selector
	 */
	function buildSelector(quality) {
		var sep = " OR ";
		if (quality == "GOD") { sep = " AND "; }
		
		var queryParts =  "(" + buildQueryParts(quality).map(el => el[1]).join(sep) + ")";
		
		if (quality == "AVG") { queryParts += ( " AND NOT( " + buildSelector("BAD") + " )"); }
		
		return queryParts;
	}

	/**
	 * Method to create the query part to list which metrics belong to the give quality, for use in SELECT.
	 *
	 * @param quality The quality
	 * @returns {string} The string
	 */
	function buildQueryListOfMetrics(quality) {
		var parts = buildQueryParts(quality);
		
		var ifs = parts.map(el => "IF(" + el[1] + ", CONCAT('" + el[0] + "', ';'), '')");

		return 'CONCAT(' + ifs.join(',') + ')';
	}
	
	
	
	/**
	 * Method to build a list of metric range selectors (e.g. (gc_content > 0.5 AND gc_content < 0.8) OR ... ).
	 *
	 * @param quality The quality
	 * @returns {*[][]} The query
	 */
	function buildQueryParts(quality) {
		var metricArr = [];
		for (var metric in config.metrics) {
			metricArr.push({metric: metric, range: config.metrics[metric].range});
		}

		return metricArr.map(buildQueryPartFromRange(quality));
	}

	/**
	 * Method to build a single query part (e.g. (gc_content > 0.5 AND gc_content < 0.8)) from a given object
	 * that has a metric and range field, corresponding to the metric name and the given range
	 * respectively. The range must be in the ["QUALITY", [lower, upper], "QUALITY"] format.
	 *
	 * @param quality The quality
	 * @returns {function(*): *[]} The query
	 */
	function buildQueryPartFromRange(quality) {
		return function(obj) {
			var parts;
			if (quality == obj.range[0]) { parts = "`" + obj.metric + "` < " + obj.range[1][0]; }
			if (quality == "AVG") {
				parts = "`" + obj.metric + "` >= " + obj.range[1][0] + " AND `" + obj.metric + "` <= " + obj.range[1][1]; 
			}
			if (quality == obj.range[2]) { parts = "`" + obj.metric + "` > " + obj.range[1][1]; }
			
			return [obj.metric, "(" + parts + ")"];
		}
	}


	/**
	 * Method to count the amount of each quality present in the data.
	 * @param data The data
	 * @param metric The metric
	 * @returns {{GOD: number, AVG: number, BAD: number}} The amount of each quality in the data
	 */
	function countQuality(data, metric) {
		var quality = {GOD: 0, AVG: 0, BAD: 0};

		data.forEach(el => quality[getQuality(el.value, metric)]++);
		
		return quality;
	}

	/**
	 * Method to ensure the callback is valid.
	 *
	 * @param callback The callback function
	 * @param stat The status code that should be returned
	 * @param msg The message that should be returned
	 * @param err The error if one was thrown
	 * @returns {*} Nothing
	 */
	function ensureCallback(callback, stat, msg, err) {

		if (stat - stat % 100 != 200) {	console.log(stat + ": " + msg); }
		if (err) { console.log(err); }

		if (typeof callback === "function") {
			return callback(stat, msg);
		}
		return;
	}

	/**
	 * Method to map a list of objects to a list of arrays, then merge all those
	 * arrays into a single array.
	 *
	 * @param data The list of data to map and merge
	 * @param mapper The mapper function to use
	 * @returns {boolean[] | *[]} The new array
	 */
	function merge(data, mapper) {
		var mapped = data.map(el => mapper(el));
		return [].concat.apply([], mapped);
	}

	
	/**
	 * Method to return the quality of a given classification value, depending on the metric's (specified byte
	 * the metric param) quality ranges.
	 *
	 * @param classification The classification
	 * @param metric The metric
	 * @returns {*} The quality
	 */
	function getQuality(classification, metric) {
		var range = config.metrics[metric].range;
		if (beforeRange(range[1], classification)) { return range[0]; }
		if (inRange(range[1], classification)) { return "AVG"; }
		if (afterRange(range[1], classification)) { return range[2]; }
		return "ERR";
	}

	/**
	 * Method to check if a value is after a range.
	 * @param range The range
	 * @param value The value
	 * @returns {boolean} Boolean if it is after the range
	 */
	function afterRange(range, value) {
		return value > range[1];
	}

	/**
	 * Method to check if a value is before a range.
	 * @param range The range
	 * @param value The value
	 * @returns {boolean} Boolean if it is before the range
	 */
	function beforeRange(range, value) {
		return value < range[0];
	}

	/**
	 * Method to check if the value is between the two values of range.
	 *
	 * @param range The range
	 * @param value The value
	 * @returns {boolean} Boolean if it is in range
	 */
	function inRange(range, value) {
		return value >= range[0] && value <= range[1];
	}


	// Source: https://stackoverflow.com/a/34890276/8464233
	function groupBy(xs, key) {
	  return xs.reduce(function(rv, x) {
		(rv[x[key]] = rv[x[key]] || []).push(x);
		return rv;
	  }, {});
	};
		
	return exports;
}